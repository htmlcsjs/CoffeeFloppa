package net.htmlcsjs.coffeeFloppa;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

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
            Mono<Void> handleCommand = gateway.on(MessageCreateEvent.class, MessageHandler::normal).then();

            // Reaction Handling
            Mono<Void> handleReactionAddition = gateway.on(ReactionAddEvent.class, ReactionHandler::addition).then();
            Mono<Void> handleReactionDeletion = gateway.on(ReactionRemoveEvent.class, ReactionHandler::deletion).then();

            // we do a little combining
            return printOnLogin
                    .and(handleCommand)
                    .and(handleReactionAddition)
                    .and(handleReactionDeletion)
                    .doOnError(CoffeeFloppa::handleException);
        });

        login.block();
    }

    public static void handleException(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .addField("Error Type", String.format("`%s`", throwable.getClass().getName()), true)
                .addField("Message", String.format("`%s`", throwable.getLocalizedMessage()), true);

        if (throwable.getStackTrace().length > 0) {
            try {
                Class<?> commandClass = Class.forName(throwable.getStackTrace()[0].getClassName());
                if (Arrays.asList(commandClass.getInterfaces()).contains(ICommand.class)) {
                    Object name = commandClass.getMethod("getName").invoke(commandClass.getConstructor().newInstance());
                    builder.addField("Command",  "`" + FloppaTomlConfig.prefix + name + "`", true);
                } else {
                    throw new RuntimeException("moger");
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException | RuntimeException | InstantiationException ignored) {
                builder.addField("Erroring Class", "`" + throwable.getStackTrace()[0].getClassName() + "`", true);
            }
        }
        builder.addField("Trace", "```java\n" +
                Arrays.stream(throwable.getStackTrace()).limit(10).map(stackTraceElement ->
                        "╠ " + stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber())
                        .collect(Collectors.joining("\n")) + "```", false);
        client.getChannelById(Snowflake.of(FloppaTomlConfig.errorChannel))
                .createMessage(builder.build().asRequest()).subscribe();
        FloppaLogger.logger.error(stringWriter.toString());
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
        MessageHandler.addCommand(new RoleSelectorAdminCommand());
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
            ReactionHandler.initRoleSelectionDataList();

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
