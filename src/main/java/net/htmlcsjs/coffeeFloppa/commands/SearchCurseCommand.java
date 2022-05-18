package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchCurseCommand implements ICommand{
    @Override
    public @NotNull String getName() {
        return ">> <<";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        String msgContent = message.getContent();
        return "https://www.curseforge.com/minecraft/mc-mods/search?search=" + String.join("+", msgContent.substring(msgContent.indexOf(">>") + 2, msgContent.indexOf("<<")).split(" "));
    }
}
