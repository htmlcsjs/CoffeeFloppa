package net.htmlcsjs.coffeeFloppa.helpers;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;

import java.util.Set;

public class CommandUtil {
    public static boolean getAllowedToRun(Message message) {
        Set<Snowflake> userRoleIDs;
        try {
            Snowflake guildID = message.getGuild().block().getId();
            userRoleIDs = (message.getAuthor().get().asMember(guildID).block().getRoleIds());
        } catch (NullPointerException ignored) {
            return false;
        }
        for (String str : CoffeeFloppa.adminRolesByGuild.values()) {
            if (userRoleIDs.contains(Snowflake.of(str))) {
                return true;
            }
        }
        if (message.getAuthor().get().getId().equals(CoffeeFloppa.admin)) {
            return true;
        }
        FloppaLogger.logger.warn(message.getAuthor().get().getTag() + " is being very naughty");
        return false;
    }
}
