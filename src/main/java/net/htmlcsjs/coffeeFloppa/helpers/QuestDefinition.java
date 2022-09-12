package net.htmlcsjs.coffeeFloppa.helpers;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class QuestDefinition {
    private final List<Long> preRequisites;
    private final String description;
    private final String name;
    private String formatterFinisher = "";
    private final long id;
    private static final Pattern pattern = Pattern.compile("§r(§[a-zA-Z0-9]) ", Pattern.MULTILINE);

    public QuestDefinition(JSONObject questData) {
        preRequisites = (List<Long>) questData.get("preRequisites:11");
        id = (long) questData.get("questID:3");
        Map<String, Object> deeperQuestData = (Map<String, Object>) ((Map<String, Object>) questData.get("properties:10")).get("betterquesting:10");
        name = (String) deeperQuestData.get("name:8");

        String rawDesc = (String) deeperQuestData.get("desc:8");
        StringBuilder descBuilder = new StringBuilder();
        boolean nextIsFormatCode = false;
        boolean bold = false;
        boolean strikethrough = false;
        boolean underline = false;
        boolean italic = false;
        rawDesc = pattern.matcher(rawDesc).replaceAll("§r $1");
        for (char ch: rawDesc.toCharArray()) {
            if (ch == '§') {
                nextIsFormatCode = true;
            } else if (nextIsFormatCode) {
                if (ch == 'l') {
                    if (!bold) {
                        addFormatting("**", descBuilder);
                        bold = true;
                    }
                } else if (ch == 'm') {
                    if (!strikethrough) {
                        addFormatting("~~", descBuilder);
                        strikethrough = true;
                    }
                } else if (ch == 'n') {
                    if (!underline) {
                        addFormatting("__", descBuilder);
                        underline = true;
                    }
                } else if (ch == 'o') {
                    if (!italic) {
                        addFormatting("*", descBuilder);
                        italic = true;
                    }
                } else if (ch == 'r') {
                    descBuilder.append(formatterFinisher);
                    formatterFinisher = "";
                    bold = false;
                    strikethrough = false;
                    underline = false;
                    italic = false;
                } else if ("0123456789abcdef".contains(String.valueOf(ch))) {
                    if (!bold) {
                        addFormatting("**", descBuilder);
                        bold = true;
                    }
                }
                nextIsFormatCode = false;
            } else {
                descBuilder.append(ch);
            }
        }
        if (formatterFinisher.length() > 0) {
            descBuilder.append(formatterFinisher);
        }
        description = descBuilder.toString();
    }

    public String generateMessage(Questbook qb) {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("__").append(name).append("__\n");
        msgBuilder.append("*ID:").append(id).append("*\n");
        if (!preRequisites.isEmpty()){
            msgBuilder.append("*Prerequisites: ");
            List<String> stringPreRequisites = new ArrayList<>();
            for (Long id: preRequisites) {
                stringPreRequisites.add(qb.questMap.get(id).getName() + " (" + id + ")");
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

    private void addFormatting(String formattingCode, StringBuilder builder) {
        builder.append(formattingCode);
        formatterFinisher += formattingCode;
    }
}
