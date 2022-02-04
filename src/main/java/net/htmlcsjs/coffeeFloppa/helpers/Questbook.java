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
            jsonData = (JSONObject) new JSONParser().parse(new FileReader("qb/" + name + ".json"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Map<String,Map<String, Object>> questArray = (Map<String, Map<String, Object>>) jsonData.get("questDatabase:9");
        for (Map<String, Object> questData : questArray.values()) {
            QuestDefinition currentDef = new QuestDefinition(questData);
            nameMap.put(currentDef.getName(), currentDef.getId());
            questMap.put(currentDef.getId(), currentDef);
        }
    }

}
