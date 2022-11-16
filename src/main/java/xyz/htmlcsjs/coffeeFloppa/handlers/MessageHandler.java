package xyz.htmlcsjs.coffeeFloppa.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateMono;
import discord4j.discordjson.json.EmojiData;
import discord4j.rest.util.AllowedMentions;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import xyz.htmlcsjs.coffeeFloppa.commands.ICommand;
import xyz.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MessageHandler {

    private static final Map<String, ICommand> commands = new HashMap<>();

    private static final Map<String, ICommand> searchCommands = new HashMap<>();

    private static final Map<Snowflake, List<Snowflake>> messagesAndResponses =  new HashMap<>();

    public static Mono<Object> normal(MessageCreateEvent event) {
        Message message = event.getMessage();
        String msgContent = message.getContent();
        return executeMessage(message, msgContent);
    }

    public static Mono<Void> deletion(MessageDeleteEvent event) {
        Snowflake message = event.getMessageId();
        MessageChannel channel = event.getChannel().block();
        if (channel == null) {
            return Mono.empty();
        }
        Mono<Void> mogla = Mono.empty();
        if (messagesAndResponses.containsKey(message)) {
            for (Snowflake sus : messagesAndResponses.get(message)) {
                Message mogMsg = channel.getMessageById(sus).block();
                if (mogMsg != null) {
                    mogla = mogla.and(mogMsg.delete());
                }
            }
            messagesAndResponses.get(message).clear();
        }
        return mogla;
    }

    public static Mono<Object> edited(MessageUpdateEvent event) {
        Message message = event.getMessage().block();
        MessageChannel channel = event.getChannel().block();
        if (message == null || channel == null) {
            return Mono.empty();
        }
        if (messagesAndResponses.containsKey(message.getId())) {
            for (Snowflake sus : messagesAndResponses.get(message.getId())) {
                Message mogMsg = channel.getMessageById(sus).block();
                if (mogMsg == null) {
                    return Mono.empty();
                }
                mogMsg.delete().subscribe();
            }
            messagesAndResponses.get(message.getId()).clear();
        }
        String msgContent = message.getContent();
        return executeMessage(message, msgContent);
    }

    @NotNull
    private static Mono<Object> executeMessage(Message message, String msgContent) {
        Mono<Object> amongVal = Mono.empty();

        // If the first char is the prefix
        if (msgContent.length() > 0 && msgContent.charAt(0) == FloppaTomlConfig.prefix.charAt(0) && !message.getAuthor().get().isBot() && !message.mentionsEveryone()) {
            try {
                // get the command
                String commandCall = msgContent.toLowerCase().split(" ")[0].replace(FloppaTomlConfig.prefix, " ").strip();
                ICommand command = commands.get(commandCall);

                // we do a little bit of executing
                sendMessage(message, command);
            } catch (IndexOutOfBoundsException ignored) {}
        } else {
            for (String key : searchCommands.keySet()) {
                String prefix = key.split(" ")[0];
                String terminator = key.split(" ")[1];
                if (msgContent.contains(prefix) && msgContent.contains(terminator) && msgContent.indexOf(prefix) < msgContent.indexOf(terminator) && !message.getAuthor().get().isBot() && !message.mentionsEveryone()) {
                    ICommand command = searchCommands.get(key);
                    sendMessage(message, command);
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

    public static boolean sendMessage(Message ref, final String msg, boolean withReference) {
        try {
            Mono<Message> messageMono = Mono.empty();
            if (msg != null && msg.length() <= 2000) {
                messageMono = ref.getChannel().flatMap(channel -> {
                    MessageCreateMono mogus = channel.createMessage(msg).withAllowedMentions(AllowedMentions.suppressEveryone());
                    if (withReference) {
                        mogus = mogus.withMessageReference(ref.getId());
                    }
                    return mogus;
                });
            } else if (msg != null) {
                messageMono = ref.getChannel().flatMap(channel -> {
                    MessageCreateMono mogus = channel.createMessage("Message content too large for msg, falling to an attachment")
                            .withFiles(MessageCreateFields.File.of("msg.txt", new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8))))
                            .withAllowedMentions(AllowedMentions.suppressEveryone());
                    if (withReference) {
                        mogus = mogus.withMessageReference(ref.getId());
                    }
                    return mogus;
                });
            }
            return !sendRegisterMessage(ref, messageMono);
        } catch (Exception e) {
            CoffeeFloppa.handleException(e);
        }
        return false;
    }

    public static boolean sendRegisterMessage(Message ref, Mono<Message> messageMono) {
        Message sent = messageMono.doOnError(CoffeeFloppa::handleException).block();
        if (sent == null) {
            return true;
        }
        if (messagesAndResponses.containsKey(ref.getId())) {
            messagesAndResponses.get(ref.getId()).add(sent.getId());
        } else {
            List<Snowflake> multimogus = new ArrayList<>();
            multimogus.add(sent.getId());
            messagesAndResponses.put(ref.getId(), multimogus);
        }
        if (messagesAndResponses.keySet().size() > 100) {
            Optional<Snowflake> firstSnowflake = messagesAndResponses.keySet().stream().sorted().findFirst();
            firstSnowflake.ifPresent(messagesAndResponses::remove);
        }
        return false;
    }

    public static boolean sendMessage(Message message, ICommand command, boolean withReference) {
        if (command != null) {
            message.getChannel().flatMap(MessageChannel::type).subscribe(); // set flop to writing
            try {
                return sendMessage(message, command.execute(message), withReference);
            } catch (Exception e) {
                CoffeeFloppa.handleException(e);
            }
        }
        return false;
    }

    public static boolean sendMessage(Message message, ICommand command) {
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
