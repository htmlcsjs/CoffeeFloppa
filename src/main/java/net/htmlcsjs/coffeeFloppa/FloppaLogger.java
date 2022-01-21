package net.htmlcsjs.coffeeFloppa;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CoffeeFloppa Logger
 * One edit to this class and you're not alive anymore
 */
public class FloppaLogger {

    public static Logger logger;

    public static void init() {
        logger = LoggerFactory.getLogger("CoffeeFloppa");
    }
}
