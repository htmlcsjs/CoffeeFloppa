package net.htmlcsjs.coffeeFloppa.helpers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class MaterialCommandsHelper {

    public static JSONObject materialData;

    public static String parseMaterial(Map<String, Object> materialMap) {
        StringBuilder returnMsg = new StringBuilder();
        returnMsg.append(String.format("Name: `%s`\n", ((String) materialMap.get("unlocalized_name")).split("\\.")[1]));
        returnMsg.append(String.format("Colour: `#%x`\n", (long) materialMap.get("colour")));
        returnMsg.append(String.format("ID: `%d`\n", (long) materialMap.get("id")));
        returnMsg.append(String.format("Mass: `%d`\n", (long) materialMap.get("mass")));
        returnMsg.append(String.format("Icon Set: `%s`\n", materialMap.get("icon_set")));
        Map<String, Object> propertiesMap = (Map<String, Object>) materialMap.get("properties");
        for (String propertyName: propertiesMap.keySet()) {
            returnMsg.append(propertyName.substring(0, 1).toUpperCase())
                    .append(String.join(" ", propertyName.substring(1).split("_"))).append(": `")
                    .append(propertiesMap.get(propertyName).toString())
                    .append("`\n");
        }
        List<Map<String, Object>> componentsList = (List<Map<String, Object>>) materialMap.get("components");
        returnMsg.append("Formula: `").append(materialMap.get("formula")).append("`\n");
        if (!componentsList.isEmpty()) {
            returnMsg.append("\nComposed of: ```java\n");
            for (Map<String, Object> component: componentsList) {
                returnMsg.append(" - ")
                        .append(component.get("amount"))
                        .append(" * ")
                        .append(((String) component.get("name")).split("\\.")[1])
                        .append("\n");
            }
            returnMsg.append("```");
        }
        return returnMsg.toString();
    }

    public static JSONObject getMaterialData() {
        if (materialData == null) {
            try {
                materialData = (JSONObject) new JSONParser().parse(new FileReader("materials.json"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return materialData;
    }
}
