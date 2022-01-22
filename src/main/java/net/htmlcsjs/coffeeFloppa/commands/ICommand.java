package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;

public interface ICommand {

    /**
     * @return The name/handle of the command
     */
    String getName();


    /**
     * To be executed when the command is called
     * @param message The Message to respond to
     * @return the String to be sent to the user
     */
    String execute(Message message);
}
