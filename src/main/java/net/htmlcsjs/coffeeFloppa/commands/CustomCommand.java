package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;

import java.util.List;

public class CustomCommand implements ICommand {
    private final String name;
    private final List<String> responses;

    public CustomCommand(String name, List<String> responses) {
        this.name = name;
        this.responses = responses;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String execute(Message message) {
        int index = 0;
        if (responses.size() > 1) {
            index = Math.abs(CoffeeFloppa.randomGen.nextInt()) % responses.size();
        }
        String msg =  responses.get(index);
        FloppaLogger.logger.info(msg);
        return msg;
    }
}
