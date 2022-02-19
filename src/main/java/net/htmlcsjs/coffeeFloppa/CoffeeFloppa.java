package net.htmlcsjs.coffeeFloppa;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import net.htmlcsjs.coffeeFloppa.commands.*;
import net.htmlcsjs.coffeeFloppa.helpers.ExecHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import reactor.core.publisher.Mono;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CoffeeFloppa {
    public static Random randomGen;
    public static DiscordClient client;
    public static char prefix;
    public static Map<String, String> adminRolesByGuild;
    public static Snowflake admin;

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
        //MessageHandler.addCommand(new StoikCommand()); TODO

        try {
            if ((boolean) jsonData.get("evalEnabled")) {
                MessageHandler.addCommand(new EvalCommand());
            }
        } catch (Exception ignored) {}
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
            ExecHelper.initTextProcessingLists();
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
            writer.write(jsonData.toJSONString());
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
}
