package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.rest.util.AllowedMentions;
import net.htmlcsjs.coffeeFloppa.helpers.MaterialCommandsHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;


import java.util.Map;

public class SearchMatCommand implements ICommand {

    @Override
    public @NotNull String getName() {
        return "material";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        String materialName = message.getContent().split(" ")[1].toLowerCase();
        JSONArray materialList = (JSONArray) MaterialCommandsHelper.getMaterialData().get("materials");
        for (Object obj: materialList) {
            Map<String, Object> materialMap = (Map<String, Object>) obj;
            if (((String) materialMap.get("unlocalized_name")).split("\\.")[1].equalsIgnoreCase(materialName)) {
                message.getChannel().flatMap(channel -> channel.createMessage()
                        .withEmbeds(MaterialCommandsHelper.parseMaterialEmbed(materialMap))
                        .withMessageReference(message.getId())
                        .withAllowedMentions(AllowedMentions.suppressEveryone())).subscribe();
                return null;
            }
        }
        return String.format("Sorry, the material %s was not found.", materialName);
    }

    @Override
    public String helpInfo() {
        return "Searches and displays internal material name";
    }
}
