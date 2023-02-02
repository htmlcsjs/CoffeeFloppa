package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.UserGuildData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;

import java.util.List;

public class GuildCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "guilds";
    }

    @Override
    public @Nullable String execute(Message message) {
        StringBuilder output = new StringBuilder();
        List<UserGuildData> dataList = CoffeeFloppa.client.getGuilds().collectList().block();
        if (dataList == null) {
            return "could not fetch Guild infomation";
        }
        for (UserGuildData guildData : dataList) {
            output.append(String.format("%s: `%s`\n", guildData.name(), guildData.id()));
        }
        return output.toString();
    }

    @Override
    public String helpInfo() {
        return "shows what guilds the bot is in";
    }
}
