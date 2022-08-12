package net.htmlcsjs.coffeeFloppa.toml;

import java.util.List;

@TomlConfig(filename = "floppa_config")
public class FloppaTomlConfig {
    @TomlConfig.ConfigElement(location = "admin.roles")
    public static List<String> adminRoles;

    @TomlConfig.ConfigElement(location = "admin.users")
    public static List<String> adminUsers;

    @TomlConfig.ConfigElement(location = "prefix")
    public static String prefix = "$";

    @TomlConfig.ConfigElement(location = "emote.count")
    public static long emoteCount = 0;

    @TomlConfig.ConfigElement(location = "emote.guild")
    public static String emoteGuild = "664888369087512601";

    @TomlConfig.ConfigElement(location = "emote.phrase")
    public static String emotePhrase = "flop";

    @TomlConfig.ConfigElement(location = "emote.emote")
    public static String emoteID = "853358698964713523";

    @TomlConfig.ConfigElement(location = "disabled_commands")
    public static List<String> disabledCommands;

    @TomlConfig.ConfigElement(location = "quest_books")
    public static List<String> questBooks;

}