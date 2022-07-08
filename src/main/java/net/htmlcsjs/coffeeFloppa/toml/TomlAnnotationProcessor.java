package net.htmlcsjs.coffeeFloppa.toml;

import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import org.json.simple.JSONObject;
import org.reflections.Reflections;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.reflections.scanners.Scanners.TypesAnnotated;

public class TomlAnnotationProcessor {
    public static void processAnnotations() {
        Reflections reflections = new Reflections("net.htmlcsjs.coffeeFloppa");
        List<Class<?>> annotations = reflections.get(TypesAnnotated.with(TomlConfig.class).asClass()).stream().toList();
        Map<String, List<String>> locationMap = new HashMap<>();
        for (Class<?> clazz: annotations) {
            TomlConfig configAnnotation = clazz.getAnnotation(TomlConfig.class);
            FloppaLogger.logger.info("pretend to open " + configAnnotation.filename());

            List<String> locationList = new ArrayList<>();
            for (Field f: clazz.getFields()) {
                if (f.isAnnotationPresent(TomlConfig.ConfigElement.class)) {
                    TomlConfig.ConfigElement elementAnnotation = f.getAnnotation(TomlConfig.ConfigElement.class);
                    locationList.add(elementAnnotation.location());
                }
            }
            locationMap.put(configAnnotation.filename(), Collections.unmodifiableList(locationList));
        }
        try {
            FileWriter writer = new FileWriter("aaaa.json");
            writer.write(new JSONObject(locationMap).toJSONString());
            writer.close();
        } catch (IOException e) {
            FloppaLogger.logger.error(CommandUtil.getStackTraceToString(e));
        }
    }
}
