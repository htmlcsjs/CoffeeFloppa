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

    public static Mono<Object> normal(Message message) {
        // If the first char is the prefix
        if (message.getContent().charAt(0) == CoffeeFloppa.prefix) {

            // get the command
            String commandCall = message.getContent().toLowerCase().split(" ")[0].replace(String.valueOf(CoffeeFloppa.prefix), " ").strip();
            ICommand command = commands.get(commandCall);

            // we do a little bit of executing
            if (command != null) {
                return message.getChannel().flatMap(channel -> channel.createMessage(command.execute(message)));
            }
        }

        // flop
        if (String.join("", message.getContent().toLowerCase().split(" ")).contains("flop")) {
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

}
