package net.htmlcsjs.coffeeFloppa.toml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TomlConfig {


    /**
     * The filename for the config file to be loaded
     */
    String filename();

    /**
     * The annotation to add a value to the toml file
     * No need to add a specific table type, use `table.key`
     * Needs a location in the format `a.b.c.d`
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigElement {
        String location();
    }
}
