package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class VersionCommand implements ICommand {
    /**
     * @return The name/handle of the command
     */
    @Override
    public @NotNull String getName() {
        return "version";
    }

    /**
     * To be executed when the command is called
     *
     * @param message The Message to respond to
     * @return the String to be sent to the user
     */
    @Nullable
    @Override
    public String execute(Message message) {
        Snowflake guildId = message.getGuildId().isPresent() ? message.getGuildId().get() : Snowflake.of("701354865217110096");
        String botNickname = "this bot";
        try {
            Optional<String> optionalNick = CoffeeFloppa.client.getSelfMember(guildId).block().nick().get();
            String username = CoffeeFloppa.client.getSelf().block().username();
            botNickname = optionalNick.orElse(username);
        } catch (Exception ignored) {}
        return String.format("%s is running version `%s`, built with commit `%s`", botNickname, CoffeeFloppa.version, CoffeeFloppa.gitRef);
    }
}
