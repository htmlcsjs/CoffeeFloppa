package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICommand {

    /**
     * @return The name/handle of the command
     */
    @NotNull
    String getName();


    /**
     * To be executed when the command is called
     * @param message The Message to respond to
     * @return the String to be sent to the user
     */
    @Nullable
    String execute(Message message);

    /**
     * Short amount of info to describe this command
     * @return the information about the command as a String
     */
    default String helpInfo() {
        Class<? extends ICommand> clazz = this.getClass();
        return clazz.getSimpleName();
    }
}
