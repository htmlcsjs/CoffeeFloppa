package xyz.htmlcsjs.coffeeFloppa.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import xyz.htmlcsjs.coffeeFloppa.helpers.RoleSelectionData;
import xyz.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa.deletionEmote;
import static xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa.self;

public class ReactionHandler {

    private static final Map<String, RoleSelectionData> roleSelectors = new TreeMap<>();
    public static Mono<?> addition(ReactionAddEvent event) {
        Message message = event.getMessage().block();
        if (message == null || event.getMember().isEmpty() || event.getMember().get().isBot()) {
            return Mono.empty();
        }

        AtomicReference<String> emojiValue = new AtomicReference<>("");
        AtomicBoolean yeet = new AtomicBoolean(false);
        AtomicBoolean delMessage = new AtomicBoolean(false);

        event.getEmoji().asUnicodeEmoji().ifPresent(emoteUnicode -> emojiValue.set(emoteUnicode.getRaw()));
        message.getAuthor().ifPresent(user -> delMessage.set(emojiValue.get().equals(deletionEmote) && user.equals(self)));
        if (delMessage.get()) {
            message.getReferencedMessage().ifPresent(referencedMessage -> yeet.set(referencedMessage.getAuthor().equals(event.getMember())));
            if (yeet.get()) {
                return message.delete();
            }
        }

        if (roleSelectors.containsKey(String.format("%s@%s", message.getId().asString(), message.getChannelId().asString()))) {
            RoleSelectionData rsData = roleSelectors.get(String.format("%s@%s", message.getId().asString(), message.getChannelId().asString()));
            if (message.getGuildId().isEmpty() || rsData.guildID().equals(message.getGuildId().get().asString())) {
                String emojiInfo = "moger";
                if (event.getEmoji().asCustomEmoji().isPresent()) {
                    emojiInfo = event.getEmoji().asCustomEmoji().get().getId().asString();
                } else if (event.getEmoji().asUnicodeEmoji().isPresent()) {
                    emojiInfo = event.getEmoji().asUnicodeEmoji().get().getRaw();
                }
                if (rsData.roleEmoteLinkage().containsKey(emojiInfo)) {
                    Snowflake roleId = Snowflake.of(rsData.roleEmoteLinkage().get(emojiInfo));
                    if (event.getMember().isPresent()) {
                        return event.getMember().get().addRole(roleId);
                    }
                }
            }
        }
        return Mono.empty();
    }

    public static Mono<?> deletion(ReactionRemoveEvent event) {
        Message message = event.getMessage().block();
        if (message == null) {
            return Mono.empty();
        }

        if (roleSelectors.containsKey(String.format("%s@%s", message.getId().asString(), message.getChannelId().asString()))) {
            RoleSelectionData rsData = roleSelectors.get(String.format("%s@%s", message.getId().asString(), message.getChannelId().asString()));
            if (message.getGuildId().isEmpty() || rsData.guildID().equals(message.getGuildId().get().asString())) {
                String emojiInfo = "moger";
                if (event.getEmoji().asCustomEmoji().isPresent()) {
                    emojiInfo = event.getEmoji().asCustomEmoji().get().getId().asString();
                } else if (event.getEmoji().asUnicodeEmoji().isPresent()) {
                    emojiInfo = event.getEmoji().asUnicodeEmoji().get().getRaw();
                }
                if (rsData.roleEmoteLinkage().containsKey(emojiInfo)) {
                    Snowflake roleId = Snowflake.of(rsData.roleEmoteLinkage().get(emojiInfo));
                    Guild guild = event.getGuild().block();
                    if (guild != null) {
                        Member member = guild.getMemberById(event.getUserId()).block();
                        if (member != null) {
                            return member.removeRole(roleId);
                        }
                    }
                }
            }
        }
        return Mono.empty();
    }

    public static void initRoleSelectionDataList() {
        roleSelectors.clear();
        for (String emoteInfo : FloppaTomlConfig.roleSelectors) {
            String[] msgDataSplit = emoteInfo.split("@");
            String messageId = msgDataSplit[0];
            String channelId = msgDataSplit[1];
            String guildId = msgDataSplit[2].substring(0, msgDataSplit[2].indexOf(";"));
            Map<String, String> roleEmoteLinkage = new HashMap<>();
            for (String i : emoteInfo.substring(messageId.length() + channelId.length() + guildId.length() + 3).split(";")) {
                roleEmoteLinkage.put(i.split("#")[1], i.split("#")[0]);
            }
            roleSelectors.put(String.format("%s@%s", messageId, channelId), new RoleSelectionData(messageId, channelId, guildId, roleEmoteLinkage));
        }
    }

    public static Map<String, RoleSelectionData> getRoleSelectors() {
        return roleSelectors;
    }
}
