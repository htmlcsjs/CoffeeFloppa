package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.helpers.MaterialCommandsHelper;
import org.json.simple.JSONArray;

import java.util.Map;

public class SearchMatByIdCommand implements ICommand {
    @Override
    public String getName() {
        return "materialID";
    }

    @Override
    public String execute(Message message) {
        int materialID = Integer.parseInt(message.getContent().split(" ")[1]);
        JSONArray materialList = (JSONArray) MaterialCommandsHelper.getMaterialData().get("materials");
        for (Object obj: materialList) {
            Map<String, Object> materialMap = (Map<String, Object>) obj;
            if ((long) materialMap.get("id") == (materialID)) {
                return MaterialCommandsHelper.parseMaterial(materialMap);
            }
        }
        return String.format("Sorry, the material with the id %d was not found.", materialID);
    }
}
