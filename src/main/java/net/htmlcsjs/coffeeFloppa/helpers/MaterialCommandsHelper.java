package net.htmlcsjs.coffeeFloppa.helpers;

import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import net.htmlcsjs.coffeeFloppa.commands.StoikCommand;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MaterialCommandsHelper {

    public static JSONObject materialData;

    @Deprecated
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

    public static List<EmbedCreateSpec> parseMaterialEmbed(Map<String, Object> materialInfo) {
        List<EmbedCreateSpec> returnList = new ArrayList<>();
        String name = ((String) materialInfo.get("unlocalized_name")).split("\\.")[1];
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder().color(Color.of(((Long) materialInfo.getOrDefault("colour", 0x36393f)).intValue()))
                .title(name.substring(0, 1).toUpperCase() + name.substring(1))
                .author(String.format("ID: %d", (long) materialInfo.get("id")), null, "https://cdn.discordapp.com/emojis/924907233618329640.png")
                .addField("Mass", String.valueOf(materialInfo.get("mass")), true)
                .addField("Icon Set", (String) materialInfo.get("icon_set"), true)
                .addField("Colour", String.format("#%x", (long) materialInfo.get("colour")), true);

        if (!((String) materialInfo.getOrDefault("formula", "")).isBlank()) {
            builder.addField("Formula", (String) materialInfo.get("formula"), true);
        }

        Map<String, Object> propertiesMap = (Map<String, Object>) materialInfo.get("properties");
        for (String propertyName: propertiesMap.keySet()) {
            builder.addField(propertyName.substring(0, 1).toUpperCase() + String.join(" ", propertyName.substring(1).split("_")),
                    propertiesMap.get(propertyName).toString(), false);
        }

        List<Map<String, Object>> componentsList = (List<Map<String, Object>>) materialInfo.get("components");
        StringBuilder compBuilder = new StringBuilder();
        if (!componentsList.isEmpty()) {
            compBuilder.append("```java\n");
            for (Map<String, Object> component: componentsList) {
                compBuilder.append(" - ")
                        .append(component.get("amount"))
                        .append(" * ")
                        .append(((String) component.get("name")).split("\\.")[1])
                        .append("\n");
            }
            compBuilder.append("```");
            builder.addField("Composition", compBuilder.toString(), false);
        }

        returnList.add(builder.build());
        return returnList;
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
