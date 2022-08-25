package net.htmlcsjs.coffeeFloppa.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateMono;
import discord4j.discordjson.json.EmojiData;
import discord4j.rest.util.AllowedMentions;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.commands.ICommand;
import net.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageHandler {

    private static final Map<String, ICommand> commands = new HashMap<>();

    private static final Map<String, ICommand> searchCommands = new HashMap<>();

    public static Mono<Object> normal(MessageCreateEvent event) {
        Message message = event.getMessage();
        if (message == null) {
            return Mono.empty();
        }
        String msgContent = message.getContent();
        Mono<Object> amongVal = Mono.empty();

        // If the first char is the prefix
        if (msgContent.length() > 0 && msgContent.charAt(0) == FloppaTomlConfig.prefix.charAt(0) && !message.getAuthor().get().isBot() && !message.mentionsEveryone()) {
            try {
                // get the command
                String commandCall = msgContent.toLowerCase().split(" ")[0].replace(FloppaTomlConfig.prefix, " ").strip();
                ICommand command = commands.get(commandCall);

                // we do a little bit of executing
                Mono<Object> builtMessage = sendMessage(message, command);
                if (builtMessage != null) amongVal = builtMessage;
            } catch (IndexOutOfBoundsException ignored) {}
        } else {
            for (String key : searchCommands.keySet()) {
                String prefix = key.split(" ")[0];
                String terminator = key.split(" ")[1];
                if (msgContent.contains(prefix) && msgContent.contains(terminator) && msgContent.indexOf(prefix) < msgContent.indexOf(terminator) && !message.getAuthor().get().isBot() && !message.mentionsEveryone()) {
                    ICommand command = searchCommands.get(key);
                    Mono<Object> builtMessage = sendMessage(message, command);
                    if (builtMessage != null) amongVal = builtMessage;
                }
            }
        }

        // flop
        if (String.join("", msgContent.toLowerCase().split(" ")).contains(FloppaTomlConfig.emotePhrase)) {
            EmojiData emojiData = CoffeeFloppa.client.getGuildEmojiById(
                    Snowflake.of(FloppaTomlConfig.emoteGuild),
                    Snowflake.of(FloppaTomlConfig.emoteID)).getData().block();
            if (emojiData != null) {
                message.addReaction(ReactionEmoji.of(emojiData)).subscribe();
                CoffeeFloppa.increaseFlopCount();
            }
        }
        return amongVal;
    }

    @Nullable
    public static Mono<Object> sendMessage(Message message, ICommand command, boolean withReference) {
        if (command != null) {
            message.getChannel().flatMap(MessageChannel::type).subscribe(); // set flop to writing
            String commandMessage = command.execute(message);
            if (commandMessage != null && commandMessage.length() <= 2000) {
                return message.getChannel().flatMap(channel -> {
                    MessageCreateMono msg = channel.createMessage(commandMessage)
                            .withAllowedMentions(AllowedMentions.suppressEveryone());
                    if (withReference) {
                        msg = msg.withMessageReference(message.getId());
                    }
                    return msg;
                });
            } else if (commandMessage != null){
                return message.getChannel().flatMap(channel -> {
                    MessageCreateMono msg = channel.createMessage("Message content too large for msg, falling to an attachment")
                            .withFiles(MessageCreateFields.File.of("msg.txt", new ByteArrayInputStream(commandMessage.getBytes(StandardCharsets.UTF_8))))
                            .withAllowedMentions(AllowedMentions.suppressEveryone());
                    if (withReference) {
                        msg = msg.withMessageReference(message.getId());
                    }
                    return msg;
                });
            }
        }
        return Mono.empty();
    }

    public static Mono<Object> sendMessage(Message message, ICommand command) {
        return sendMessage(message, command, true);
    }


    /**
     * Adds a command to the registry
     * @param command The command to be added to the registry
     */
    public static void addCommand(ICommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    /**
     * Returns the command registry map
     * @return The registry
     */
    public static Map<String, ICommand> getCommands() {
        return commands;
    }


    /**
     * Clears the command registry
     */
    public static void clearCommands() {
        MessageHandler.commands.clear();
    }

    /**
     * Adds a search command to the registry
     * @param searchCommand The search command to be added to the registry
     */
    public static void addSearchCommand(ICommand searchCommand) {
        searchCommands.put(searchCommand.getName().toLowerCase(), searchCommand);
    }

    /**
     * Returns the search command registry map
     * @return The registry
     */
    public static Map<String, ICommand> getSearchCommands() {
        return searchCommands;
    }


    /**
     * Clears the search command registry
     */
    public static void clearSearchCommands() {
        MessageHandler.searchCommands.clear();
    }

}
