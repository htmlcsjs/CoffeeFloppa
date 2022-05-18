package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.MessageHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class HelpCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "help";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        return String.format("Command prefix is %c\nCommands avalible:\n", CoffeeFloppa.prefix) +
                MessageHandler.getCommands().keySet().stream().sorted().collect(Collectors.joining(", "));
    }
}
