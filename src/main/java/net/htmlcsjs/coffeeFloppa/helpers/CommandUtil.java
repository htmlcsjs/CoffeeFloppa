package net.htmlcsjs.coffeeFloppa.helpers;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.PartialMember;
import discord4j.rest.util.Color;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CommandUtil {

    // Pattern for recognizing a URL, based off RFC 3986, "taken" from https://stackoverflow.com/questions/5713558/
    public static final Pattern urlPattern = Pattern.compile("(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

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

    public static String getAttachment(Message message) throws IOException, IllegalArgumentException {
        URL url;
        if (message.getAttachments().size() > 0) {
            url = new URL(message.getAttachments().get(0).getUrl());
        } else {
            Matcher urlMatcher = urlPattern.matcher(message.getContent());
            String urlStr = "";
            while (urlMatcher.find()) {
                urlStr = message.getContent().substring(urlMatcher.start(), urlMatcher.end());
            }
            if (urlStr.equals("")) {
                throw new IllegalArgumentException("Could not find URL in message contents");
            }
            url = new URL(urlStr);
        }
        return new BufferedReader(new InputStreamReader(url.openStream())).lines().collect(Collectors.joining("\n"));
    }

    @NotNull
    public static String getHexValueFromColour(Color colour) {
        StringBuilder rawValue = new StringBuilder(Integer.toHexString(colour.getRGB())).reverse();
        if (rawValue.length() < 6) {
            rawValue.append("0".repeat(6-rawValue.length()));
        }
        return rawValue.reverse().append("#").reverse().toString();
    }


    public static String getAttachment(Message message) throws IOException, IllegalArgumentException {
        URL url;
        if (message.getAttachments().size() > 0) {
            url = new URL(message.getAttachments().get(0).getUrl());
        } else {
            Matcher urlMatcher = urlPattern.matcher(message.getContent());
            String urlStr = "";
            while (urlMatcher.find()) {
                urlStr = message.getContent().substring(urlMatcher.start(), urlMatcher.end());
            }
            if (urlStr.equals("")) {
                throw new IllegalArgumentException("Could not find URL in message contents");
            }
            url = new URL(urlStr);
        }
        return new BufferedReader(new InputStreamReader(url.openStream())).lines().collect(Collectors.joining("\n"));
    }
}
