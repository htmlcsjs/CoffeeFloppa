package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class SearchMatCommand implements ICommand {

    private static JSONObject materialData;

    @Override
    public String getName() {
        return "material";
    }

    @Override
    public String execute(Message message) {
        if (materialData == null) {
            try {
                materialData = (JSONObject) new JSONParser().parse(new FileReader("materials.json"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String materialName = message.getContent().split(" ")[1].toLowerCase();
        JSONArray materialList = (JSONArray) materialData.get("materials");
        for (Object obj: materialList) {
            Map<?, ?> materialMap = (Map<?, ?>) obj;
            if (((String) materialMap.get("unlocalized_name")).split("\\.")[1].equalsIgnoreCase(materialName)) {
                StringBuilder returnMsg = new StringBuilder();
                returnMsg.append(String.format("Name: %s\n", ((String) materialMap.get("unlocalized_name")).split("\\.")[1]));
                returnMsg.append(String.format("Colour: #%x\n", (long) materialMap.get("colour")));
                returnMsg.append(String.format("ID: %d\n", (long) materialMap.get("id")));
                Map<String, Object> propertiesMap = (Map<String, Object>) materialMap.get("properties");
                for (String propertyName: propertiesMap.keySet()) {
                    returnMsg.append(propertyName.substring(0, 1).toUpperCase())
                            .append(String.join(" ", propertyName.substring(1).split("\\_"))).append(": ").
                            append(propertiesMap.get(propertyName).toString())
                            .append("\n");
                }
                List<Map<String, Object>> componentsList = (List<Map<String, Object>>) materialMap.get("components");
                if (componentsList.isEmpty()) {
                    returnMsg.append("This is elemental ").append(((String) materialMap.get("unlocalized_name")).split("\\.")[1]);
                } else {
                    returnMsg.append("Composed of: \n");
                    for (Map<String, Object> component: componentsList) {
                        returnMsg.append(" - ")
                                .append(component.get("amount"))
                                .append(" * ")
                                .append(((String) component.get("name")).split("\\.")[1])
                                .append("\n");
                    }
                }
                return returnMsg.toString();
            }
        }
        return String.format("Sorry, the material %s was not found.", materialName);
    }
}
