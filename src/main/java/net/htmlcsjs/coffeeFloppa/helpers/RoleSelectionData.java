package net.htmlcsjs.coffeeFloppa.helpers;

import java.util.Map;

public record RoleSelectionData(String messageID, String channelID, String guildID, Map<String, String> roleEmoteLinkage) {
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(messageID).append("@").append(channelID).append("@").append(guildID);
        for (String k : roleEmoteLinkage().keySet()) {
            builder.append(";").append(roleEmoteLinkage.get(k)).append("#").append(k);
        }
        return builder.toString();
    }
}
