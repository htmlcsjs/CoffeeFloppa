package net.htmlcsjs.coffeeFloppa.handlers;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.helpers.RoleSelectionData;
import net.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static net.htmlcsjs.coffeeFloppa.CoffeeFloppa.deletionEmote;
import static net.htmlcsjs.coffeeFloppa.CoffeeFloppa.self;

public class ReactionHandler {

    private static Map<String, RoleSelectionData> roleSelectionDataList = new TreeMap<>();
    public static Mono<?> main(ReactionAddEvent event) {
        Message message = event.getMessage().block();
        if (message == null) {
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
        return Mono.empty();
    }

    public static void initRoleSelectionDataList() {
        roleSelectionDataList.clear();
        for (String emoteInfo : FloppaTomlConfig.roleSelectors) {
            String[] msgDataSplit = emoteInfo.split("@");
            String messageId = msgDataSplit[0];
            String channelId = msgDataSplit[1];
            String guildId = msgDataSplit[2].substring(0, msgDataSplit[2].indexOf(";"));
            Map<String, String> roleEmoteLinkage = new HashMap<>();
            for (String i : emoteInfo.substring(messageId.length() + channelId.length() + guildId.length() + 3).split(";")) {
                roleEmoteLinkage.put(i.split("#")[1], i.split("#")[0]);
            }
            roleSelectionDataList.put(messageId, new RoleSelectionData(messageId, channelId, guildId, roleEmoteLinkage));
        }
    }
}
