package net.htmlcsjs.coffeeFloppa.helpers.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class FloppaLuaLib extends TwoArgFunction {
    public FloppaLuaLib() {}

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue floppaLib = tableOf();
        floppaLib.set("susso", new Susso());
        env.set("floppa", floppaLib);
        env.get("package").get("loaded").set("floppa", floppaLib);
        return floppaLib;
    }

    static public class Susso extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf("I AM SUS, THIS IS FROM JAVA");
        }
    }
}
