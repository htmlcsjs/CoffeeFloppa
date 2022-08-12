package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.MessageHandler;
import net.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HelpCommand implements ICommand {
    private final int commandsPerPage;

    public HelpCommand(Integer commandsPerPage) {
        this.commandsPerPage = commandsPerPage;
    }

    @Override
    public @NotNull String getName() {
        return "help";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        List<String> args = List.of(message.getContent().split(" "));
        int page = 1;
        List<String> sortedCommandStream = MessageHandler.getCommands()
                .entrySet()
                .stream()
                .map(this::commandProcessor)
                .filter(s -> !(s == null || s.isEmpty()))
                .sorted().toList();
        double pages = Math.ceil(sortedCommandStream.size() / (float) commandsPerPage);

        try {
            page = Integer.parseInt(args.get(1));
            if (page > pages ) {
                page = (int) pages;
            }
        } catch (Exception ignored) {
        }

        return String.format("Page %d of %.0f\n\nCommand prefix is %s\nCommands available:```lua\n", page, pages, FloppaTomlConfig.prefix) +
                    sortedCommandStream.stream()
                        .skip((long) (page - 1) * commandsPerPage)
                        .limit(commandsPerPage)
                        .collect(Collectors.joining()) + "```";
    }

    @Override
    public String helpInfo() {
        return "A command to list other commands";
    }

    protected String commandProcessor(Map.Entry<String, ICommand> entry) {
        if (!(entry.getValue() instanceof CustomCommand)) {
            return String.format("%s%s: %s\n", FloppaTomlConfig.prefix, entry.getKey(), entry.getValue().helpInfo());
        } else {
            return null;
        }
    }
}
