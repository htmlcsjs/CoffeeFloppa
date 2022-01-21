package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.MessageHandler;

public class HelpCommand implements ICommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String execute(Message message) {
        StringBuilder returnStringBuilder = new StringBuilder();
        for (ICommand command: MessageHandler.getCommands().values()) {
            returnStringBuilder.append(CoffeeFloppa.prefix).append(command.getName()).append(", ");
        }
        return returnStringBuilder.toString();
    }
}
