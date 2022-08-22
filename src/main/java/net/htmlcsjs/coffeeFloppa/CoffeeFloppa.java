package net.htmlcsjs.coffeeFloppa;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.User;
import net.htmlcsjs.coffeeFloppa.commands.*;
import net.htmlcsjs.coffeeFloppa.handlers.MessageHandler;
import net.htmlcsjs.coffeeFloppa.handlers.ReactionHandler;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import net.htmlcsjs.coffeeFloppa.helpers.MaterialCommandsHelper;
import net.htmlcsjs.coffeeFloppa.helpers.lua.LuaHelper;
import net.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import net.htmlcsjs.coffeeFloppa.toml.TomlAnnotationProcessor;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;

public class CoffeeFloppa {
    public static Random randomGen;
    public static DiscordClient client;
    public static User self;
    public static String deletionEmote = "\uD83D\uDDD1️";
    public static String version = "@VERSION@";
    public static String gitRef = "@GIT_VER@";

    private static JSONObject jsonData;

    public static void main(String[] args) throws IOException {
        // Init stuff
        FloppaLogger.init();
        FloppaLogger.logger.info("Staring initial config sync");
        TomlAnnotationProcessor.loadConfigs();
        TomlAnnotationProcessor.saveConfigs();
        FloppaLogger.logger.info("initial Configs synced!");
        refreshData();


        client = DiscordClient.create(Files.readString(Path.of("token")).strip());
        randomGen = new Random();

        // Generate inital commands
        refreshCommands();

        // Load lang for materials
        try (FileReader reader = new FileReader("materials.lang")) {
            MaterialCommandsHelper.loadMaterials(new BufferedReader((reader)));
        } catch (Exception e) {
            FloppaLogger.logger.error(CommandUtil.getStackTraceToString(e));
        }

        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
            // Login message
            Mono<Void> printOnLogin = gateway.on(ReadyEvent.class, event ->
                    Mono.fromRunnable(() -> {
                        self = event.getSelf();
                        FloppaLogger.logger.info("Logged in as " +  self.getUsername() + "#" + self.getDiscriminator());
                    }))
                    .then();

            // Message handling
            Mono<Void> handleCommand = gateway.on(MessageCreateEvent.class, MessageHandler::normal).doOnError(System.out::println).then();

            Mono<Void> handleReaction = gateway.on(ReactionAddEvent.class, ReactionHandler::main).doOnError(System.out::println).then();

            // we do a little combining
            return printOnLogin.and(handleCommand).and(handleReaction).doOnError(System.out::println);
        });

        login.block();
    }

    public static void refreshCommands() {
        // Add static commands
        MessageHandler.addCommand(new HelpCommand(25));
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
        MessageHandler.addCommand(new VersionCommand());
        MessageHandler.addCommand(new OcCommand());
        MessageHandler.addCommand(new EvalCommand());
        MessageHandler.addCommand(new HelpCommand(75) {
            @Override
            public @NotNull String getName() {
                return "commands";
            }
            @Override
            protected String commandProcessor(Map.Entry<String, ICommand> entry) {
                return String.format("%s%s, ", FloppaTomlConfig.prefix, entry.getKey());
            }
        });

        for (String name : MessageHandler.getCommands().keySet()) {
            Class<? extends ICommand> commandClass = MessageHandler.getCommands().get(name).getClass();
            if (FloppaTomlConfig.disabledCommands.contains(commandClass.getName().replace(commandClass.getPackageName() + ".", ""))) {
                MessageHandler.getCommands().remove(name);
            }
        }

        for (String name : FloppaTomlConfig.questBooks) {
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

    public static void refreshData() {
        try {
            syncConfigs();
            jsonData = (JSONObject) new JSONParser().parse(new FileReader("config.json"));

            MessageHandler.clearCommands();
            MessageHandler.clearSearchCommands();
            refreshCommands();

            LuaHelper.initLuaServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncConfigs() {
        FloppaLogger.logger.info("Staring config sync");
        TomlAnnotationProcessor.saveConfigs();
        TomlAnnotationProcessor.loadConfigs();
        FloppaLogger.logger.info("Configs synced!");
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
        refreshData();
    }

    public static void increaseFlopCount() {
        FloppaTomlConfig.emoteCount++;
        syncConfigs();
    }

    public static String formatJSONStr(String jsonString, int indent_width) {
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
