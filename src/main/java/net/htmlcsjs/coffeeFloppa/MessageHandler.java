package net.htmlcsjs.coffeeFloppa;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmojiData;
import net.htmlcsjs.coffeeFloppa.commands.ICommand;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class MessageHandler {

    private static final Map<String, ICommand> commands = new HashMap<>();

    private static final Map<String, ICommand> searchCommands = new HashMap<>();

    public static Mono<Object> normal(Message message) {
        String msgContent = message.getContent();

        // If the first char is the prefix
        if (msgContent.charAt(0) == CoffeeFloppa.prefix && !message.getAuthor().get().isBot()) {

            // get the command
            String commandCall = msgContent.toLowerCase().split(" ")[0].replace(String.valueOf(CoffeeFloppa.prefix), " ").strip();
            ICommand command = commands.get(commandCall);

            // we do a little bit of executing
            if (command != null) {
                return message.getChannel().flatMap(channel -> channel.createMessage(command.execute(message)));
            }
        }
        for (String key : searchCommands.keySet()) {
            String prefix = key.split(" ")[0];
            String terminator = key.split(" ")[1];
            if (msgContent.contains(prefix) && msgContent.contains(terminator) && msgContent.indexOf(prefix) < msgContent.indexOf(terminator) && !message.getAuthor().get().isBot()) {
                ICommand command = searchCommands.get(key);
                return message.getChannel().flatMap(channel -> channel.createMessage(command.execute(message)));
            }
        }
        // flop
        if (String.join("", msgContent.toLowerCase().split(" ")).contains("flop")) {
            EmojiData emojiData = CoffeeFloppa.client.getGuildEmojiById(Snowflake.of("664888369087512601"), Snowflake.of("853358698964713523")).getData().block();
            if (emojiData != null) {
                message.addReaction(ReactionEmoji.of(emojiData)).subscribe();
                CoffeeFloppa.increaseFlopCount();
            }
        }
        return Mono.empty();
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
