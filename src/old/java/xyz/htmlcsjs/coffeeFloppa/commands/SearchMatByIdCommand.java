package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.rest.util.AllowedMentions;
import xyz.htmlcsjs.coffeeFloppa.handlers.MessageHandler;
import xyz.htmlcsjs.coffeeFloppa.helpers.MaterialCommandsHelper;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SearchMatByIdCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "materialID";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        int materialID = Integer.parseInt(message.getContent().split(" ")[1]);
        JSONArray materialList = (JSONArray) MaterialCommandsHelper.getMaterialData().get("materials");
        for (Object obj: materialList) {
            Map<String, Object> materialMap = (Map<String, Object>) obj;
            if ((long) materialMap.get("id") == (materialID)) {
                MessageHandler.sendRegisterMessage(message, message.getChannel().flatMap(channel -> channel.createMessage()
                        .withEmbeds(MaterialCommandsHelper.parseMaterialEmbed(materialMap))
                        .withMessageReference(message.getId())
                        .withAllowedMentions(AllowedMentions.suppressEveryone())));
                return null;
            }
        }
        return String.format("Sorry, the material with the id %d was not found.", materialID);
    }

    @Override
    public String helpInfo() {
        return "Searches and displays material by id";
    }
}
