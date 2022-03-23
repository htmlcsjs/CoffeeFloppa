package net.htmlcsjs.coffeeFloppa;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import net.htmlcsjs.coffeeFloppa.commands.*;
import net.htmlcsjs.coffeeFloppa.helpers.lua.LuaHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import reactor.core.publisher.Mono;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CoffeeFloppa {
    public static Random randomGen;
    public static DiscordClient client;
    public static char prefix;
    public static Map<String, String> adminRolesByGuild;
    public static Snowflake admin;
    public static Map<String, Object> emoteData;

    private static JSONObject jsonData;

    public static void main(String[] args) throws Exception {
        // Init stuff
        FloppaLogger.init();
        refreshConfig();
        client = DiscordClient.create((String) jsonData.get("token"));
        admin = Snowflake.of((String) jsonData.get("admin"));
        randomGen = new Random();
        prefix = ((String) jsonData.get("prefix")).charAt(0);

        // Generate inital commands
        refreshCommands();

        // Get adminRolesByGuild;
        adminRolesByGuild = (Map<String, String>) jsonData.get("guilds");

        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
            // Login message
            Mono<Void> printOnLogin = gateway.on(ReadyEvent.class, event ->
                    Mono.fromRunnable(() -> {
                        final User self = event.getSelf();
                        FloppaLogger.logger.info("Logged in as " +  self.getUsername() + "#" + self.getDiscriminator());
                    }))
                    .then();

            // Message handling
            Mono<Void> handlePingCommand = gateway.on(MessageCreateEvent.class, event -> {
                Message message = event.getMessage();
                return MessageHandler.normal(message);
            }).doOnError(System.out::println).then();

            // we do a little combining
            return printOnLogin.and(handlePingCommand).doOnError(System.out::println);
        });

        login.block();
    }

    public static void refreshCommands() {
        // Add static commands
        MessageHandler.addCommand(new HelpCommand());
        MessageHandler.addCommand(new AddCmdCommand());
        MessageHandler.addCommand(new RemoveCmdCommand());
        MessageHandler.addCommand(new FlopCountCommand());
        MessageHandler.addCommand(new SearchMatCommand());
        MessageHandler.addCommand(new SearchMatByIdCommand());
        MessageHandler.addCommand(new GithubIssueCommand());
        MessageHandler.addCommand(new AddonCommand());
        MessageHandler.addCommand(new QuestAdminCommand());
        MessageHandler.addCommand(new RefreshCommand());
        MessageHandler.addCommand(new StoikCommand());

        if ((boolean) jsonData.getOrDefault("evalEnabled", false)) {
            MessageHandler.addCommand(new EvalCommand());
        }
        for (String name: (List<String>) jsonData.getOrDefault("quest_books", Collections.EMPTY_LIST)) {
            MessageHandler.addCommand(new QuestbookCommand(name));
        }

        // Add commands from JSON
        for (Object obj : (JSONArray) jsonData.get("commands")) {
            Map<?, ?> commandMap = (Map<?, ?>) obj;
            JSONArray responses = (JSONArray) commandMap.get("responses");
            String call = (String) commandMap.get("call");
            MessageHandler.addCommand(new CustomCommand(call, responses));
        }

        // Add search commands
        MessageHandler.addSearchCommand(new SearchCurseCommand());
    }

    public static void refreshConfig() {
        try {
            jsonData = (JSONObject) new JSONParser().parse(new FileReader("config.json"));
            MessageHandler.clearCommands();
            MessageHandler.clearSearchCommands();
            refreshCommands();
            LuaHelper.initLuaServer();
            Map<String, Object> defaultEmoteData = new HashMap<>();
            defaultEmoteData.put("guild", "664888369087512601");
            defaultEmoteData.put("emote", "853358698964713523");
            defaultEmoteData.put("phrase", "flop");
            if (!jsonData.containsKey("flop_emote_data")) {
                jsonData.put("flop_emote_data", defaultEmoteData);
            }
            emoteData = (Map<String, Object>) jsonData.get("flop_emote_data");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getJsonData() {
        return jsonData;
    }

    public static void updateConfigFile(JSONObject jsonData) {
        try {
            FileWriter writer = new FileWriter("config.json");
            writer.write(formatJSONStr(jsonData.toJSONString(), 4));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshConfig();
    }

    public static void increaseFlopCount() {
        JSONObject newJsonData = CoffeeFloppa.getJsonData();
        long flopCount = (long) newJsonData.get("flop");
        newJsonData.put("flop", ++flopCount);
        updateConfigFile(newJsonData);
    }

    private static String formatJSONStr(String jsonString, int indent_width) {
        StringBuilder returnBuilder = new StringBuilder();
        boolean inQuotes = false;
        int indent = 0;

        for (char c: jsonString.toCharArray()) {
            if (c == '\"') {
                returnBuilder.append(c);
                inQuotes = !inQuotes;
                continue;
            } else if (!inQuotes) {
                switch (c) {
                    case '{':
                    case '[':
                        returnBuilder.append(c).append("\n").append(String.format("%" + (indent += indent_width) + "s", ""));
                        continue;
                    case '}':
                    case ']':
                        returnBuilder.append("\n").append((indent -= indent_width) > 0 ? String.format("%" + indent + "s", "") : "").append(c);
                        continue;
                    case ':':
                        returnBuilder.append(c).append(" ");
                        continue;
                    case ',':
                        returnBuilder.append(c).append("\n").append(indent > 0 ? String.format("%" + indent + "s", "") : "");
                        continue;
                    default:
                        if (Character.isWhitespace(c)) continue;
                }
            }

            returnBuilder.append(c);
        }
        return returnBuilder.toString();
    }

}
