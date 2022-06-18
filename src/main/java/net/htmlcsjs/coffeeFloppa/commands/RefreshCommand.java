package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RefreshCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "refreshConfig";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        if (CommandUtil.getAllowedToRun(message)) {
            CoffeeFloppa.refreshConfig();
            return "Config Successfully updated";
        } else {
            return "Config unable to be updated";
        }
    }

    @Override
    public String helpInfo() {
        return "Mod only command, DO NOT USE";
    }
}
