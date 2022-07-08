package net.htmlcsjs.coffeeFloppa.toml;

import java.util.List;

@TomlConfig(filename = "test.toml")
public class TestTomlConfig {

    @TomlConfig.ConfigElement(location = "str")
    public static String str;

    @TomlConfig.ConfigElement(location = "int")
    public static int testInt;

    @TomlConfig.ConfigElement(location = "float")
    public static float testFloat;

    @TomlConfig.ConfigElement(location = "double")
    public static double testDouble;

    @TomlConfig.ConfigElement(location = "array")
    public static String[] strArray;

    @TomlConfig.ConfigElement(location = "list")
    public static List<String> strList;

    @TomlConfig.ConfigElement(location = "name.name")
    public static String testName;
}
