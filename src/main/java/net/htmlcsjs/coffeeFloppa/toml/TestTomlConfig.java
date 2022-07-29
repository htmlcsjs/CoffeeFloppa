package net.htmlcsjs.coffeeFloppa.toml;

import java.util.Arrays;
import java.util.List;

@TomlConfig(filename = "test")
public class TestTomlConfig {

    @TomlConfig.ConfigElement(location = "str")
    public static String str = "among";

    @TomlConfig.ConfigElement(location = "int")
    public static int testInt = 69;

    @TomlConfig.ConfigElement(location = "long")
    public static long testLong = (long) (Math.pow(2, 63) - 1);

    @TomlConfig.ConfigElement(location = "float")
    public static float testFloat = 6.9f;

    @TomlConfig.ConfigElement(location = "double")
    public static double testDouble = 238748923746.27364723478;

    @TomlConfig.ConfigElement(location = "list")
    public static List<String> strList = Arrays.asList("Morb", "floppa", "greg");

    @TomlConfig.ConfigElement(location = "name.name")
    public static String testName = "amongla";
}
