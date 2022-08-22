package net.htmlcsjs.coffeeFloppa.helpers;

import java.util.Map;

public record RoleSelectionData(String messageID, String channelID, String guildID, Map<String, String> roleEmoteLinkage) {}
