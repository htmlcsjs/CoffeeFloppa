package net.htmlcsjs.coffeeFloppa.helpers.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FloppaPackageLib extends PackageLib {

    /* Copy pasted from https://github.com/luaj/luaj/blob/daf3da94e3cdba0ac6a289148d7e38bd53d3fe64/src/core/org/luaj/vm2/lib/PackageLib.java, all just consts */
    private static final LuaString _LOADED      = valueOf("loaded");
    private static final LuaString _SEARCHERS   = valueOf("searchers");
    private static final LuaString _SENTINEL    = valueOf("\u0001");
    private static final List<String> classBlacklist = new ArrayList<>(Collections.singletonList("org.luaj.vm2.lib.DebugLib'"));

    public static void addClassToBlacklist(String className) {
        classBlacklist.add(className);
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        super.call(modname, env);
        /*// reflection stuff 2
        // WHYYYYYYYYYYYYY (this is very hacky)
        try {
            globals = (Globals) this.getClass().getField("globals").get(this);
        } catch (Exception ignored) {
            FloppaLogger.logger.error("Couldn't find the globals field in " + this.classnamestub());
            return LuaValue.NIL;
        }*/
        Globals globals = env.checkglobals();

        globals.set("require", new requireFlop());
        return env;
    }

    public class requireFlop extends OneArgFunction {
        public LuaValue call(LuaValue arg) {
            if (classBlacklist.contains(arg.checkjstring()) || arg.checkjstring().toLowerCase().contains("debug")) {
                return LuaValue.NIL;
            } else {
                return new require().call(arg); // super call
            }
        }
    }
}
