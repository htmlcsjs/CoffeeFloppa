package xyz.htmlcsjs.coffeeFloppa.helpers;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Color;
import xyz.htmlcsjs.coffeeFloppa.FloppaLogger;
import xyz.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CommandUtil {

    // Pattern for recognizing a URL, based off RFC 3986, "taken" from https://stackoverflow.com/questions/5713558/
    public static final Pattern urlPattern = Pattern.compile("(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    public static final Pattern ghIssuePattern = Pattern.compile(".*?(\\w+/\\w+)#(\\d+).*?", Pattern.MULTILINE);

    public static boolean getAllowedToRun(Message message) {
        Set<Snowflake> userRoleIDs;
        try {
            Snowflake guildID = message.getGuild().block().getId();
            userRoleIDs = (message.getAuthor().get().asMember(guildID).block().getRoleIds());
        } catch (NullPointerException ignored) {
            return false;
        }
        for (String str : FloppaTomlConfig.adminRoles) {
            if (userRoleIDs.contains(Snowflake.of(str))) {
                return true;
            }
        }
        for (String str : FloppaTomlConfig.adminUsers) {
            if (message.getAuthor().get().getId().equals(Snowflake.of(str))) {
                return true;
            }
        }
        FloppaLogger.logger.warn(message.getAuthor().get().getTag() + " is being very naughty");
        return false;
    }

    public static String getStackTraceToString(Throwable e, int limit) {
        StringBuilder stackTrace = new StringBuilder();
        int i = 0;
        for (StackTraceElement ste : e.getStackTrace()) {
            stackTrace.append(ste).append("\n");
            if (i > limit) {
                break;
            }
            i++;
        }
        return stackTrace.toString();
    }

    public static String getStackTraceToString(Throwable e) {
        return getStackTraceToString(e, e.getStackTrace().length);
    }

    @NotNull
    public static String getHexValueFromColour(Color colour) {
        StringBuilder rawValue = new StringBuilder(Integer.toHexString(colour.getRGB())).reverse();
        if (rawValue.length() < 6) {
            rawValue.append("0".repeat(6 - rawValue.length()));
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

    public static String trimString(String text, int length, String end) {
        if (text.length() <= length) {
            return text;
        } else {
            return text.substring(0, length) + end;
        }
    }

    public static String trimString(String text, int length) {
        return trimString(text, length, "");
    }
}
