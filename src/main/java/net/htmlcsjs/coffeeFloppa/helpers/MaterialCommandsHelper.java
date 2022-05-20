package net.htmlcsjs.coffeeFloppa.helpers;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MaterialCommandsHelper {

    public static JSONObject materialData;
    public static Map<String, String> materialLang = new HashMap<>();

    public static void loadMaterials(BufferedReader reader) throws IOException {
        String line = "mog=sus";
        FloppaLogger.logger.info("Starting to load lang");
        while (line != null) {
            if (line.isBlank() || line.charAt(0) == '#') {
                line = reader.readLine();
                continue;
            }

            int separatorIndex = line.indexOf("=");
            if (separatorIndex == -1) {
                FloppaLogger.logger.warn(String.format("Line %s is malformed", line));
            } else {
                materialLang.put(line.substring(0, separatorIndex), line.substring(separatorIndex+1));
            }

            line = reader.readLine();
        }
        FloppaLogger.logger.info(materialLang.get("Lang Loaded"));
    }

    public static List<EmbedCreateSpec> parseMaterialEmbed(Map<String, Object> materialInfo) {
        List<EmbedCreateSpec> returnList = new ArrayList<>();

        String key = (String) materialInfo.get("unlocalized_name");
        String name;

        if (materialLang.containsKey(key)) {
            name = materialLang.get(key);
        } else {
            String keyBody = key.split("\\.")[1];
            name = keyBody.substring(0, 1).toUpperCase() + keyBody.substring(1);
        }

        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder().color(Color.of(((Long) materialInfo.getOrDefault("colour", 0x36393f)).intValue()))
                .title(name)
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
