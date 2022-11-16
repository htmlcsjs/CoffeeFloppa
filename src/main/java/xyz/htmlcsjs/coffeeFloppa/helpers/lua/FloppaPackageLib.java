package xyz.htmlcsjs.coffeeFloppa.helpers.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FloppaPackageLib extends PackageLib {

    private static final List<String> classBlacklist = new ArrayList<>(Collections.singletonList("org.luaj.vm2.lib.DebugLib'"));

    public static void addClassToBlacklist(String className) {
        classBlacklist.add(className);
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        super.call(modname, env);
        this.java_searcher = new flop_java_searcher();
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

    public class flop_java_searcher extends PackageLib.java_searcher {
        public Varargs invoke(Varargs args) {
            return valueOf("no");
        }
    }
}
