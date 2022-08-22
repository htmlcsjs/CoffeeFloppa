package net.htmlcsjs.coffeeFloppa.handlers;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static net.htmlcsjs.coffeeFloppa.CoffeeFloppa.deletionEmote;
import static net.htmlcsjs.coffeeFloppa.CoffeeFloppa.self;

public class ReactionHandler {
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
}
