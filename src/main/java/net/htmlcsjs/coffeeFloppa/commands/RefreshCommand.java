package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;

public class RefreshCommand implements ICommand {
    @Override
    public String getName() {
        return "refreshConfig";
    }

    @Override
    public String execute(Message message) {
        if (CommandUtil.getAllowedToRun(message)) {
            CoffeeFloppa.refreshConfig();
            return "Config Successfully updated";
        } else {
            return "Config unable to be updated";
        }
    }
}
