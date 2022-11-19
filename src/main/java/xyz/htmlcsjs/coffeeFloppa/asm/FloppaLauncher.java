package xyz.htmlcsjs.coffeeFloppa.asm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class FloppaLauncher {
    public static final Logger logger = LoggerFactory.getLogger("FloppaASM");

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        ClassLoader classLoader = new FloppaClassLoader("s".getClass().getClassLoader(), Arrays.asList("java.", "sun.", "org.slf4j.", "org.reflections", "org.objectweb.asm.", "xyz.htmlcsjs.coffeeFloppa.toml.", "javax", "jdk"));
        logger.info("Starting main CoffeeFloppa process");
        Class<?> clazz = classLoader.loadClass("xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa");
        Object obj = clazz.getConstructor().newInstance();
        Method run = clazz.getMethod("run");
        run.invoke(obj);
    }
}
