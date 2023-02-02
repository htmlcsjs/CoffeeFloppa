package xyz.htmlcsjs.coffeeFloppa;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.simple.JSONObject;
import xyz.htmlcsjs.coffeeFloppa.handlers.OtherHandler;
import xyz.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import xyz.htmlcsjs.coffeeFloppa.toml.TomlAnnotationProcessor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class CoffeeFloppa {
    public static Random randomGen;
    public static JDA client;
    public static SelfUser self;
    public static final String deletionEmote = "\uD83D\uDDD1️";
    public static final String version = "@VERSION@";
    public static final String gitRef = "@GIT_VER@";

    private static JSONObject jsonData;

    @SuppressWarnings("unused")
    public void run() throws IOException {
        // Init stuff
        FloppaLogger.init();
        FloppaLogger.logger.info("Staring initial config sync");
        TomlAnnotationProcessor.loadConfigs();
        TomlAnnotationProcessor.saveConfigs();
        FloppaLogger.logger.info("initial Configs synced!");
        syncConfigs();
//        RestAction.setDefaultFailure(CoffeeFloppa::handleException);


        client = JDABuilder.createDefault(Files.readString(Path.of("token")).strip())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new OtherHandler())
                .build();
        randomGen = new Random();

        // Load lang for materials
        /* try (FileReader reader = new FileReader("materials.lang")) {
            MaterialCommandsHelper.loadMaterials(new BufferedReader((reader)));
        } catch (Exception e) {
            FloppaLogger.logger.error(CommandUtil.getStackTraceToString(e));
        }*/

    }

/*    public static void handleException(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .addField("Error Type", String.format("`%s`", throwable.getClass().getName()), true)
                .addField("Message", MessageHandler.getCurrentMessageURL(), true);

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
        builder.description("**Trace**\n```java\n" + throwable.getLocalizedMessage() + "\n" +
                Arrays.stream(throwable.getStackTrace()).limit(10).map(stackTraceElement ->
                        "╟" + stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber())
                        .collect(Collectors.joining("\n")) + "```");
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
        MessageHandler.addCommand(new WikiCommand());
        MessageHandler.addCommand(new GuildCommand());
        MessageHandler.addCommand(new RawCommand());
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
    }*/

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
        syncConfigs();
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
                    case '{', '[' -> {
                        returnBuilder.append(c).append("\n").append(String.format("%" + (indent += indent_width) + "s", ""));
                        continue;
                    }
                    case '}', ']' -> {
                        returnBuilder.append("\n").append((indent -= indent_width) > 0 ? String.format("%" + indent + "s", "") : "").append(c);
                        continue;
                    }
                    case ':' -> {
                        returnBuilder.append(c).append(" ");
                        continue;
                    }
                    case ',' -> {
                        returnBuilder.append(c).append("\n").append(indent > 0 ? String.format("%" + indent + "s", "") : "");
                        continue;
                    }
                    default -> {
                        if (Character.isWhitespace(c)) continue;
                    }
                }
            }

            returnBuilder.append(c);
        }
        return returnBuilder.toString();

    }

}
