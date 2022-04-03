package net.htmlcsjs.coffeeFloppa.helpers;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.PartialMember;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
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

    public static JSONObject getMessageJson(Message message) {
        JSONObject messageData = new JSONObject();
        messageData.put("content", message.getContent());
        messageData.put("id", message.getId().asString());
        messageData.put("author", message.getAuthor().get().getId().asString());
        messageData.put("channel", message.getChannelId().asString());
        List<PartialMember> memberMentions = message.getMemberMentions();
        if (memberMentions.isEmpty()) {
            messageData.put("user_to_use", message.getAuthor().get().getId().asString());
            for (String str: message.getContent().split(" ")) {
                try {
                    messageData.put("user_to_use", Snowflake.of(str).asString());
                } catch (NumberFormatException ignored) {}
            }
        } else {
            messageData.put("user_to_use", memberMentions.get(0).getId().asString());
        }
        return messageData;
    }

    public static String getStackTraceToString(Exception e, int limit) {
        StringBuilder stackTrace = new StringBuilder();
        int i = 0;
        for (StackTraceElement ste: e.getStackTrace()) {
            stackTrace.append(ste).append("\n");
            if (i > limit) {
                break;
            }
            i++;
        }
        return stackTrace.toString();
    }

    public static String getStackTraceToString(Exception e) {
        return getStackTraceToString(e, e.getStackTrace().length);
    }
}
