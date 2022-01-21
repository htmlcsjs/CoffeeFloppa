package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;

public interface ICommand {

    public String getName();

    public String execute(Message message);
}
