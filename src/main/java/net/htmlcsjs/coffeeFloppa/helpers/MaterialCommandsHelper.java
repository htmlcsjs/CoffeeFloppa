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
        FloppaLogger.logger.info("Lang loaded");
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
                .addField("ID", String.format("ID: %d", (long) materialInfo.get("id")), true)
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

        long id = (long) materialInfo.get("id");
        if (id <= 2999) {
            builder.author("GregTech CEu",
                    "https://www.curseforge.com/minecraft/mc-mods/gregtech-ce-unofficial/",
                    "https://media.forgecdn.net/avatars/468/148/637751129280745336.png");
        } else if (id <= 3059) {
            builder.author("Gregicality Multiblocks",
                    "https://www.curseforge.com/minecraft/mc-mods/gregicality-multiblocks",
                    "https://media.forgecdn.net/avatars/476/750/637770403565836783.png");
        } else if (id <= 19999) {
            builder.author("Gregicality Science",
                    "https://github.com/GregTechCEu/gregicality-science",
                    "https://cdn.discordapp.com/emojis/949194079500120154.gif");
        } else if (id <= 20999) {
            builder.author("Gregification",
                    "https://github.com/GregTechCEu/Gregification",
                    "https://cdn.discordapp.com/attachments/904846945095385138/984013433584353280/gregification_logo.png");
        } else if (id <= 21499) {
            builder.author("HtmlTech",
                    "https://www.curseforge.com/minecraft/mc-mods/htmltech",
                    "https://media.forgecdn.net/avatars/471/74/637758777197994901.png");
        } else if (id <= 21999) {
            builder.author("GregTech Food Option",
                    "https://www.curseforge.com/minecraft/mc-mods/gregtech-food-option",
                    "https://media.forgecdn.net/avatars/376/445/637552985536571351.png");
        } else if (id <= 23599) {
            builder.author("PCM's Ore Addon", null, null);
        } else if (id <= 23999) {
            builder.author("MechTech",
                    "https://www.curseforge.com/minecraft/mc-mods/mechtech",
                    "https://media.forgecdn.net/avatars/360/897/637525560735345110.png");
        } else if (id >= 32000) {
            builder.author("CraftTweaker Material",
                    null,
                    "https://media.forgecdn.net/avatars/142/108/636546700830987709.png");
        } else {
            builder.author("Custom",
                    null,
                    "https://raw.githubusercontent.com/GregTechCEu/GregTech/master/src/main/resources/assets/gregtech/textures/items/tools/wrench.png");
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
