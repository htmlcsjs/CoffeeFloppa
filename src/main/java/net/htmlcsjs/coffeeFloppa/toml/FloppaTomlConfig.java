package net.htmlcsjs.coffeeFloppa.toml;

import java.util.List;

@TomlConfig(filename = "floppa_config")
public class FloppaTomlConfig {
    @TomlConfig.ConfigElement(location = "admin.roles")
    public static List<String> adminRoles;

    @TomlConfig.ConfigElement(location = "admin.users")
    public static List<String> adminUsers;
}
