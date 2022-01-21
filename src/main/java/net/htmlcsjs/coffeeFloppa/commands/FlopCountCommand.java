package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;

public class FlopCountCommand implements ICommand{
    @Override
    public String getName() {
        return "flopCount";
    }

    @Override
    public String execute(Message message) {
        return String.valueOf((long) CoffeeFloppa.getJsonData().get("flop")) + " flops reacted to since last reset";
    }
}
