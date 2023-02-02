package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MemberData;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class VersionCommand implements ICommand {

    @Override
    public @NotNull String getName() {
        return "version";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        Snowflake guildId = message.getGuildId().isPresent() ? message.getGuildId().get() : Snowflake.of("701354865217110096");
        String botNickname = "this bot";
        MemberData selfMemberData = CoffeeFloppa.client.getSelfMember(guildId).block();
        if (selfMemberData != null) {
            Optional<String> optionalNick = selfMemberData.nick().get();
            String username = CoffeeFloppa.self.getUsername();
            botNickname = optionalNick.orElse(username);
        }
        return String.format("%s is running version `%s`, built with commit <https://github.com/htmlcsjs/CoffeeFloppa/commit/%s>", botNickname, CoffeeFloppa.version, CoffeeFloppa.gitRef);
    }

    @Override
    public String helpInfo() {
        return "Displays the version of CoffeeFloppa this bot is running";
    }
}
