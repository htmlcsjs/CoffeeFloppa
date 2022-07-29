package net.htmlcsjs.coffeeFloppa.toml;

import org.apache.commons.text.StringEscapeUtils;
import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static org.reflections.scanners.Scanners.TypesAnnotated;


public class TomlAnnotationProcessor {

    private static List<Class<?>> annotations;
    private static Map<String, Map<String, String>> fieldMap;
    private static final List<Class<?>> primateClasses = Arrays.asList(long.class, int.class, short.class, byte.class, double.class, float.class);
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
                    if (line.matches("\\[.*]")) {
                        curTable = line.substring(1, line.length() - 1);
                    } else {
                        Map<String, String> classFieldMap = fieldMap.get(clazz.getName());

                        String[] splitLine = line.split("=");
                        String loc = (curTable != null ? curTable + "." : "") + splitLine[0].strip();

                        if (classFieldMap.containsKey(loc)) {

                            String rawValue = Arrays.stream(splitLine).map(s -> !s.equals(splitLine[0]) ? s : "").collect(Collectors.joining()).strip();
                            Field f = clazz.getField(classFieldMap.get(loc).split(" ")[1]);

                            f.setAccessible(true);

                            Object value = getValidValForToml(rawValue, f.getGenericType());

                            if (value != null) {
                                if (primateClasses.contains(f.getType())) {
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
    }

    private static void initReflections() {
        if (inited) {
            return;
        }
        inited = true;
        Reflections reflections = new Reflections("net.htmlcsjs.coffeeFloppa");
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

                        if (elementAnnotation.location().matches("[A-Za-z\\d_\\-.]*")) {
                            writer.write(elementAnnotation.location());
                        } else {
                            writer.write(StringEscapeUtils.escapeJava(elementAnnotation.location()));
                        }

                        writer.write(" = " + getValidTomlForVal(f.get(null), f.getGenericType()) + "\n");
                    }
                }
                writer.close();
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String getValidTomlForVal(Object obj, Type type) {
        if (type == long.class) {
            return obj == null ? "0" : ((Long) obj).toString();
        } else if (type == int.class) {
            return obj == null ? "0" : ((Integer) obj).toString();
        } else if (type == short.class) {
            return obj == null ? "0" : ((Short) obj).toString();
        } else if (type == byte.class) {
            return obj == null ? "0" : ((Byte) obj).toString();
        } else if (type == float.class || type == double.class) {
            return obj.toString();
        } else if (type.getTypeName().matches("java\\.util\\.List<.*>")) {
            if (obj == null) {
                return "[]";
            }
            Type listGenericType = ((ParameterizedType) type).getActualTypeArguments()[0];
            return "["+((List<?>) obj).stream().map(i -> getValidTomlForVal(i, listGenericType)).collect(Collectors.joining(", ")) + "]";
        } else {
            return obj == null ? "\"\"" : "\"" + StringEscapeUtils.escapeJava(obj.toString()) + "\"";
        }
    }

    private static Object getValidValForToml(String str, Type type) {
        if (type == long.class) {
            return Long.getLong(str);
        } else if (type == int.class) {
            return Integer.getInteger(str);
        } else if (type == short.class) {
            return Short.parseShort(str);
        } else if (type == byte.class) {
            return Byte.parseByte(str);
        } else if (type == double.class) {
            return Double.parseDouble(str);
        } else if (type == float.class){
            return Float.parseFloat(str);
        } else if (type.getTypeName().matches("java\\.util\\.List<.*>")) {

            if (str.equals("[]") || !str.matches("\\[.*]")) {
                return new ArrayList<>();
            }

            Type listGenericType = ((ParameterizedType) type).getActualTypeArguments()[0];
            List<Object> objs = new ArrayList<>();

            for (String sus : str.substring(1, str.length() - 1).split(",")) {
                objs.add(getValidValForToml(sus.strip(), listGenericType));
            }

            return objs;
        } else if (type == String.class) {
            if (str.matches("\"\"\".*\"\"\"") || str.matches("'''.*'''")) {
                throw new RuntimeException("multiline strings are not implemented");
            } else if (str.matches("\".*\"")) {
                return StringEscapeUtils.unescapeJava(str.substring(1, str.length() -1));
            } else if (str.matches("'.*'")) {
                return str.substring(1, str.length() -1);
            } else {
                throw new RuntimeException("String not of valid structure");
            }
        } else {
            return null;
        }
    }
}
