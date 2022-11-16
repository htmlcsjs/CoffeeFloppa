package xyz.htmlcsjs.coffeeFloppa.helpers.lua;

import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class LuaBufferLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue buffLib = tableOf();

        buffLib.set("new", new newBuffer());
        buffLib.set("get", new getFromBuffer());
        buffLib.set("set", new setInBuffer());
        buffLib.set("size", new bufferSize());
        buffLib.set("from_string", new bufferFromString());

        env.set("buffer", buffLib);
        env.get("package").get("loaded").set("buffer", buffLib);
        return buffLib;
    }

    public static class newBuffer extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            if (!arg.isint()) {
                return error("Array Size is not an int");
            }
            return new LuaUserdata(new byte[arg.checkint()]);
        }
    }

    public static class getFromBuffer extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue buffer, LuaValue pos) {
            if (!buffer.isuserdata() || !pos.isint()) {
                return error("Args are of invalid type. should be (userdata, int)");
            }
            byte[] userdata = (byte[]) buffer.checkuserdata();
            int posInt = pos.checkint() - 1;
            if (posInt > userdata.length) {
                return error("Pos out of bounds");
            }
            return valueOf(userdata[posInt] - Byte.MIN_VALUE);
        }
    }

    public static class setInBuffer extends ThreeArgFunction {
        @Override
        public LuaValue call(LuaValue buffer, LuaValue pos, LuaValue value) {
            if (!buffer.isuserdata() || !pos.isint()) {
                return error("Args are of invalid type. should be (userdata, int, int)");
            }
            byte[] userdata = (byte[]) buffer.checkuserdata();
            int posInt = pos.checkint() - 1;
            int valInt = value.checkint() + Byte.MIN_VALUE;
            if (valInt > Byte.MAX_VALUE || valInt < Byte.MIN_VALUE) {
                return error("value is not in the bounds of a byte");
            }
            if (posInt > userdata.length) {
                return error("Pos out of bounds");
            }
            userdata[posInt] = (byte) valInt;
            return NIL;
        }
    }

    public static class bufferSize extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue buffer) {
            if (!buffer.isuserdata()) {
                return error("The arg is of incorrect type, it should be userdata");
            }
            byte[] userdata = (byte[]) buffer.checkuserdata();
            return valueOf(userdata.length);
        }
    }

    public static class bufferFromString extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue str) {
            if (!str.isstring()) {
                return error("the argument is not a string");
            }
            return userdataOf(str.checkjstring().getBytes());
        }
    }

}
