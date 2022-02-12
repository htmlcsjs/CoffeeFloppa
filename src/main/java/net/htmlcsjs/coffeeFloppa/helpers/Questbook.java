package net.htmlcsjs.coffeeFloppa.helpers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class Questbook {
    public Map<Long, QuestDefinition> questMap = new HashMap<>();
    public Map<String, Long> nameMap = new HashMap<>();

    public Questbook(String name) {
        JSONObject jsonData = null;
        try {
            FileReader jsonReader = new FileReader("qb/" + name + ".json");
            jsonData = (JSONObject) new JSONParser().parse(jsonReader);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        JSONObject questArray = (JSONObject) jsonData.get("questDatabase:9");
        for (Object obj : questArray.values()) {
            QuestDefinition currentDef = new QuestDefinition((JSONObject) obj);
            nameMap.put(currentDef.getName(), currentDef.getId());
            questMap.put(currentDef.getId(), currentDef);
        }
    }

}
