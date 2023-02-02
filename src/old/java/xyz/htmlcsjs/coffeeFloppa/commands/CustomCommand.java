package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateFields;
import discord4j.rest.util.AllowedMentions;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomCommand implements ICommand {
    private final String name;
    private final List<String> responses;

    public CustomCommand(String name, List<String> responses) {
        this.name = name;
        this.responses = responses;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Nullable
    @Override
    public String execute(Message message) {
        int index = 0;
        List<String> messageContent = new ArrayList<>();
        Collections.addAll(messageContent, message.getContent().split(" "));
        messageContent.add("susso");
        if (messageContent.get(1).equalsIgnoreCase("-all")) {
            StringBuilder formatted = new StringBuilder();
            int i = 0;
            for (String response: responses) {
                formatted.append(++i).append(".").append("\n");
                formatted.append(response).append("\n");
            }
            message.getChannel().flatMap(channel -> channel.createMessage(String.format("The responses for %s are:", name))
                    .withFiles(MessageCreateFields.File.of("msg.txt", new ByteArrayInputStream(formatted.toString().getBytes(StandardCharsets.UTF_8))))
                    .withMessageReference(message.getId())
                    .withAllowedMentions(AllowedMentions.suppressEveryone())).subscribe();
            return null;
        } else {
            if (responses.size() > 1) {
                index = Math.abs(CoffeeFloppa.randomGen.nextInt()) % responses.size();
            }
            return responses.get(index);
        }
    }

    @Override
    public String helpInfo() {
        return "A Custom Command";
    }
}
