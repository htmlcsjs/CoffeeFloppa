package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlopCountCommand implements ICommand{
    @Override
    public @NotNull String getName() {
        return "flopCount";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        return String.valueOf((long) CoffeeFloppa.getJsonData().get("flop")) + " flops reacted to since last reset";
    }

    @Override
    public String helpInfo() {
        return "amount of flop reactions reacted";
    }
}
