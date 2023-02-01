package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateFields;
import discord4j.rest.util.AllowedMentions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.htmlcsjs.coffeeFloppa.handlers.MessageHandler;

import java.io.InputStream;

public class RawCommand implements ICommand {
    private static final String formatStr = "Use format `$raw <command name>` or `$raw --class <class path>`";
    @Override
    public @NotNull String getName() {
        return "raw";
    }

    @Override
    public @Nullable String execute(Message message) {
        String command;
        boolean classMode = false;
        try {
            command = message.getContent().split(" ")[1];
            if (command.equals("--class")) {
                classMode = true;
                command = message.getContent().split(" ")[2];
            }
        } catch (Exception ignored) {
            return "No command name passed to command\n" + formatStr;
        }
        Class<?> commandClass;
        if (!classMode) {
            if (command.charAt(0) == '$') {
                command = command.substring(1);
            }
            if (!MessageHandler.getCommands().containsKey(command)) {
                return String.format("`$%s` is not a real command\n%s", command, formatStr);
            }
            commandClass = MessageHandler.getCommands().get(command).getClass();
        } else {
            try {
                commandClass = Class.forName(command);
            } catch (ClassNotFoundException e) {
                return String.format("`%s` is not a real class\n%s", command, formatStr);

            }
        }

        boolean source = commandClass.getPackage().getName().contains("xyz.htmlcsjs.coffeeFloppa");
        String filePath = commandClass.getName().replaceAll("\\.", "/") + (source ? ".java" : ".class");
        InputStream classStream = ClassLoader.getSystemClassLoader().getResourceAsStream(filePath);
        if (classStream == null && source) {
            filePath = commandClass.getName().replaceAll("\\.", "/") + ".class";
            source = false;
            classStream = ClassLoader.getSystemClassLoader().getResourceAsStream(filePath);
        }
        if (classStream == null) {
            return String.format("Couldnt read class/source of %s", commandClass.getSimpleName());
        }

        String finalCommand = command;
        boolean finalSource = source;
        String finalFilePath = filePath;
        InputStream finalClassStream = classStream;
        boolean finalClassMode = classMode;
        MessageHandler.sendRegisterMessage(message, message.getChannel().flatMap(channel -> channel.createMessage()
                .withContent(String.format("%s of `%s%s`", finalSource ? "Source" : "Bytecode", finalClassMode ? "" : "$", finalCommand))
                .withFiles(MessageCreateFields.File.of(finalFilePath.split("/")[finalFilePath.split("/").length - 1], finalClassStream))
                .withMessageReference(message.getId())
                .withAllowedMentions(AllowedMentions.suppressEveryone())));

        return null;
    }
}
