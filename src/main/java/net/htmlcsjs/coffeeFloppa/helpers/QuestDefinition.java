package net.htmlcsjs.coffeeFloppa.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class QuestDefinition {
    private final List<Long> preRequisites;
    private final String description;
    private final String name;
    private final long id;

    public QuestDefinition(Map<String, Object> questData) {
        preRequisites = (List<Long>) questData.get("preRequisites:11");
        id = (long) questData.get("questID:3");
        Map<String, Object> deeperQuestData = (Map<String, Object>) ((Map<String, Object>) questData.get("properties:10")).get("betterquesting:10");
        name = (String) deeperQuestData.get("name:8");

        String rawDesc = (String) deeperQuestData.get("desc:8");
        StringBuilder descBuilder = new StringBuilder();
        String formatterFinisher = "";
        boolean nextIsFormatCode = false;

        for (char ch: rawDesc.toCharArray()) {
            if (ch == '§') {
                nextIsFormatCode = true;
            } else if (nextIsFormatCode) {
                if (ch == 'l') {
                    descBuilder.append("**");
                    formatterFinisher += "**";
                } else if (ch == 'm') {
                    descBuilder.append("~~");
                    formatterFinisher += "~~";
                } else if (ch == 'n') {
                    descBuilder.append("__");
                    formatterFinisher += "__";
                } else if (ch == 'o') {
                    descBuilder.append("*");
                    formatterFinisher += "*";
                } else if (ch == 'r') {
                    descBuilder.append(formatterFinisher);
                    formatterFinisher = "";
                }
                nextIsFormatCode = false;
            } else {
                descBuilder.append(ch);
            }
        }
        description = descBuilder.toString();
    }

    public String generateMessage() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("__").append(name).append("__\n");
        msgBuilder.append("*ID:").append(id).append("*\n");
        if (!preRequisites.isEmpty()){
            msgBuilder.append("*Prerequisites: ");
            List<String> stringPreRequisites = new ArrayList<>();
            for (Long id: preRequisites) {
                stringPreRequisites.add(id.toString());
            }
            msgBuilder.append(String.join(", ", stringPreRequisites));
            msgBuilder.append("*\n\n");
        }
        msgBuilder.append(description);
        return msgBuilder.toString();
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

}
