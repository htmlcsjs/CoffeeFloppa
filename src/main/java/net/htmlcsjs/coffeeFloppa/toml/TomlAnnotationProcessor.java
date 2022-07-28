package net.htmlcsjs.coffeeFloppa.toml;

import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import org.apache.commons.text.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.reflections.scanners.Scanners.TypesAnnotated;


public class TomlAnnotationProcessor {

    private static Reflections reflections;
    private static List<Class<?>> annotations;
    private static Map<String, Map<String, String>> fieldMap;
    private static boolean inited = false;

    public static void loadConfigs() {
        initReflections();
        for (Class<?> clazz : annotations) {
            TomlConfig configAnnotation = clazz.getAnnotation(TomlConfig.class);
            try {
                BufferedReader reader = new BufferedReader(new FileReader("config/" + configAnnotation.filename() + ".toml"));
                String line = reader.readLine();
                String curTable = null;

                while (line != null) {
                    if (line.matches("\\[.*\\]")) {
                        curTable = line.substring(1, line.length() - 1);
                    } else {
                        Map<String, String> classFieldMap = fieldMap.get(clazz.getName());

                        String[] splitLine = line.split("=");
                        String loc = (curTable != null ? curTable + "." : "") + splitLine[0].strip();
                        if (classFieldMap.containsKey(loc)) {
                            String rawValue = Arrays.stream(splitLine).map(s -> !s.equals(splitLine[0]) ? s : "").collect(Collectors.joining()).strip();
                            Field f = clazz.getField(classFieldMap.get(loc).split(" ")[1]);
                            f.setAccessible(true);
                            Object value = getValidValForToml(rawValue, f.getType(), f.getAnnotation(TomlConfig.ConfigElement.class).getClass());
                            FloppaLogger.logger.info(loc);
                            if (value != null) {
                                if (f.getType() == long.class) {
                                    f.set(null, value);
                                } else if (f.getType() == int.class) {
                                    f.set(null, value);
                                } else if (f.getType() == short.class) {
                                    f.set(null, value);
                                } else if (f.getType() == byte.class) {
                                    f.set(null, value);
                                } else if (f.getType() == double.class) {
                                    f.set(null, value);
                                } else if (f.getType() == float.class) {
                                    f.set(null, value);
                                } else {
                                    f.set(null, f.getType().cast(value));
                                }
                            }
                        }
                    }
                    line = reader.readLine();
                }

                reader.close();
            } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            FileWriter writer = new FileWriter("aaaa.json");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("feilds", fieldMap);
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
        fieldMap = new TreeMap<>();

        for (Class<?> clazz : annotations) {
            Map<String, String> classMap = new TreeMap<>();
            for (Field f : clazz.getFields()) {
                if (f.isAnnotationPresent(TomlConfig.ConfigElement.class)) {
                    classMap.put(f.getAnnotation(TomlConfig.ConfigElement.class).location(), clazz.getName() + " " + f.getName() + " " + clazz.getAnnotation(TomlConfig.class).filename());
                }
            }
            fieldMap.put(clazz.getName(), classMap);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveConfigs() {
        initReflections();
        new File("config/").mkdirs();
        for (Class<?> clazz : annotations) {
            TomlConfig configAnnotation = clazz.getAnnotation(TomlConfig.class);
            try {
                FileWriter writer = new FileWriter("config/" + configAnnotation.filename() + ".sus.toml");
                for (Field f : clazz.getFields()) {
                    if (f.isAnnotationPresent(TomlConfig.ConfigElement.class)) {
                        TomlConfig.ConfigElement elementAnnotation = f.getAnnotation(TomlConfig.ConfigElement.class);
                        if (elementAnnotation.location().matches("[A-Za-z\\d_\\-\\.]*")) {
                            writer.write(elementAnnotation.location());
                        } else {
                            writer.write(StringEscapeUtils.escapeJava(elementAnnotation.location()));
                        }
                        writer.write(" = " + getValidTomlForVal(f.get(null), f.getType(), elementAnnotation.listType()) + "\n");
                    }
                }
                writer.close();
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String getValidTomlForVal(Object obj, Class<?> clazz, Class<?> listType) {
        if (clazz == long.class) {
            return obj == null ? "0" : ((Long) obj).toString();
        } else if (clazz == int.class) {
            return obj == null ? "0" : ((Integer) obj).toString();
        } else if (clazz == short.class) {
            return obj == null ? "0" : ((Short) obj).toString();
        } else if (clazz == byte.class) {
            return obj == null ? "0" : ((Byte) obj).toString();
        } else if (clazz == float.class || clazz == double.class) {
            return obj.toString();
        } else if (clazz == List.class) {
            if (obj == null) {
                return "[]";
            }
            return "["+((List<?>) obj).stream().map(i -> getValidTomlForVal(i, listType, listType)).collect(Collectors.joining(", ")) + "]";
        } else {
            return obj == null ? "\"\"" : "\"" + StringEscapeUtils.escapeJava(obj.toString()) + "\"";
        }
    }

    private static Object getValidValForToml(String str, Class<?> clazz, Class<?> listType) {
        if (clazz == long.class) {
            return Long.getLong(str);
        } else if (clazz == int.class) {
            return Integer.getInteger(str);
        } else if (clazz == short.class) {
            return Short.parseShort(str);
        } else if (clazz == byte.class) {
            return Byte.parseByte(str);
        } else if (clazz == double.class) {
            return Double.parseDouble(str);
        } else if (clazz == float.class){
            return Float.parseFloat(str);
        } else if (clazz == List.class) {
            if (str.equals("[]") || !str.matches("\\[.*\\]")) {
                return new ArrayList<>();
            }
            String susStr = str.substring(1, str.length() - 1);
            String[] raw = susStr.split(",");
            List<Object> objs = new ArrayList<>();
            for (String sus : raw) {
                objs.add(getValidValForToml(sus, listType, listType));
            }
            return objs;
        } else if (clazz == String.class) {
            return StringEscapeUtils.unescapeJava(str.substring(1, str.length() -1));
        } else {
            return null;
        }
    }
}
