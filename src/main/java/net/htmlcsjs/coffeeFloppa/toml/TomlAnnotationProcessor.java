package net.htmlcsjs.coffeeFloppa.toml;

import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import org.apache.commons.text.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.reflections.Reflections;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.reflections.scanners.Scanners.TypesAnnotated;

public class TomlAnnotationProcessor {

    private static Reflections reflections;
    private static List<Class<?>> annotations;
    private static List<String> fieldList;
    private static boolean inited = false;

    public static void loadConfigs() {
        initReflections();
        Map<String, List<String>> dataLocMap = new HashMap<>();

        /*for (String fieldLoc : fieldList) {
            String[] nameParts = fieldLoc.split("\\|");
            try {
                Class<?> clazz = Class.forName(nameParts[0]);
                Field field = clazz.getField(nameParts[1]);
                dataLocMap.put()
            } catch (NoSuchFieldException e) {
                FloppaLogger.logger.error(String.format("Couldn't find the field %s in class %s", nameParts[1], nameParts[0]));
            } catch (ClassNotFoundException e) {
                FloppaLogger.logger.error(String.format("Couldn't find the class %s", nameParts[0]));
            }
        }*/

        try {
            FileWriter writer = new FileWriter("aaaa.json");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", dataLocMap);
            jsonObject.put("feilds", fieldList);
            writer.write(CoffeeFloppa.formatJSONStr(jsonObject.toJSONString(), 4));
            writer.close();
        } catch (Exception e) {
            FloppaLogger.logger.error(e.getMessage());
        }
    }

    private static void initReflections() {
        if (inited) {
            return;
        }
        inited = true;
        reflections = new Reflections("net.htmlcsjs.coffeeFloppa");
        annotations = reflections.get(TypesAnnotated.with(TomlConfig.class).asClass()).stream().toList();
        fieldList = new ArrayList<>();

        for (Class<?> clazz : annotations) {
            for (Field f : clazz.getFields()) {
                if (f.isAnnotationPresent(TomlConfig.ConfigElement.class)) {
                    fieldList.add(clazz.getName() + " " + f.getName());
                }
            }
        }
    }

    public static void saveConfigs() {
        initReflections();
        new File("config/").mkdirs();
        for (Class<?> clazz : annotations) {
            TomlConfig configAnnotation = clazz.getAnnotation(TomlConfig.class);
            try {
                FileWriter writer = new FileWriter("config/" + configAnnotation.filename() + ".toml");
                for (Field f : clazz.getFields()) {
                    if (f.isAnnotationPresent(TomlConfig.ConfigElement.class)) {
                        TomlConfig.ConfigElement elementAnnotation = f.getAnnotation(TomlConfig.ConfigElement.class);
                        if (elementAnnotation.location().matches("[A-Za-z\\d_\\-\\.]*")) {
                            writer.write(elementAnnotation.location());
                        } else {
                            writer.write(StringEscapeUtils.escapeJava(elementAnnotation.location()));
                        }
                        writer.write(" = " + getValidTomlForVal(f.get(null), f.getType()) + "\n");
                    }
                }
                writer.close();
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String getValidTomlForVal(Object obj, Class<?> clazz) {
        if (clazz == Long.class) {
            return obj == null ? "0" : ((Long) obj).toString();
        } else if (clazz == Integer.class) {
            return obj == null ? "0" : ((Integer) obj).toString();
        } else if (clazz == Short.class) {
            return obj == null ? "0" : ((Short) obj).toString();
        } else if (clazz == Byte.class) {
            return obj == null ? "0" : ((Byte) obj).toString();
        } else if (clazz == Float.class || clazz == Double.class) {
            return obj.toString();
        } else if (clazz.isArray()) {
            if (obj == null) {
                return "[]";
            }
            return "["+ Arrays.stream((Object[]) obj).map(i -> getValidTomlForVal(i, i.getClass())).collect(Collectors.joining(", ")) + "]";
        } else if (clazz == List.class) {
            if (obj == null) {
                return "[]";
            }
            return "["+((List<?>) obj).stream().map(i -> getValidTomlForVal(i, i.getClass())).collect(Collectors.joining(", ")) + "]";
        } else {
            return obj == null ? "\"\"" : "\"" + StringEscapeUtils.escapeJava(obj.toString()) + "\"";
        }
    }
}
