package xyz.htmlcsjs.coffeeFloppa.helpers.lua;

import discord4j.core.object.entity.Message;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

import java.util.ArrayList;
import java.util.List;

// Adapted from https://github.com/luaj/luaj/blob/master/examples/jse/SampleSandboxed.java
public class LuaHelper {
    private static Globals server_globals;
    private static final int tabSize = 4;
    private static final List<LuaTable> hitTables = new ArrayList<>();
    private static StringBuilder printBuffer = new StringBuilder();
    private static FloppaLuaLib floppaLib;

    public static void initLuaServer() {
        // Create server globals with just enough library support to compile user scripts.
        server_globals = new Globals();
        server_globals.load(new JseBaseLib());
        server_globals.load(new FloppaPackageLib());
        server_globals.load(new StringLib());

        // To load scripts, we occasionally need a math library in addition to compiler support.
        // To limit scripts using the debug library, they must be closures, so we only install LuaC.
        server_globals.load(new JseMathLib());
        LoadState.install(server_globals);
        LuaC.install(server_globals);
    }

    /**
     * Run a script in a lua thread and limit it to a certain number of instructions by setting a hook function.
     * Give each script its own globals, but leave out libraries that contain functions that can be abused.
     *
     * @return the return args form the script
     */
    public static Varargs runScriptInSandbox(String script, Message message) {

        printBuffer = new StringBuilder();
        floppaLib = new FloppaLuaLib(message);

        Globals user_globals = new Globals();
        user_globals.load(new JseBaseLib());
        user_globals.load(new FloppaPackageLib());
        user_globals.load(new Bit32Lib());
        user_globals.load(new TableLib());
        user_globals.load(new StringLib());
        user_globals.load(new JseMathLib());
        user_globals.load(floppaLib);
        user_globals.load(new HTTPLib());
        user_globals.load(new LuaBufferLib());
        user_globals.set("print", new PrintFunc());
        user_globals.set("maths", user_globals.get("math"));

        // The debug library must be loaded for hook functions to work, which we use for timeout
        // However it can be **heavily** abused so we disable the rest of it
        user_globals.load(new DebugLib());
        LuaValue sethook = user_globals.get("debug").get("sethook");
        user_globals.set("debug", LuaValue.NIL);
        user_globals.get("package").get("loaded").set("debug", LuaValue.NIL); // :trollge:

        // Set up the script to run in its own thread, allowing for our timeout
        // Note that the environment is set to the user globals, however compiling is done with the server globals.
        LuaValue chunk = server_globals.load(script, "main", user_globals);
        LuaThread thread = new LuaThread(user_globals, chunk);

        // Set the hook function to immediately throw an Error, which will not be
        // handled by any Lua code other than the coroutine.
        LuaValue hookfunc = new ZeroArgFunction() {
            public LuaValue call() {
                // A simple lua error may be caught by the script, but a
                // Java Error will pass through to top and stop the script.
                throw new Error("Script overran resource limits.");
            }
        };
        int instruction_count = 1000000;
        sethook.invoke(LuaValue.varargsOf(new LuaValue[]{thread, hookfunc, LuaValue.EMPTYSTRING, LuaValue.valueOf(instruction_count)}));

        // When we resume the thread, it will run up to 'instruction_count' instructions
        // then call the hook function which will error out and stop the script.
        return thread.resume(LuaValue.NIL);
    }

    public static String startLuaTableToStr(LuaTable table) {
        hitTables.clear();
        String tableStr = luaTableToString(table, tabSize);
        hitTables.clear();
        return tableStr;
    }

    private static String luaTableToString(LuaTable table, long tabLevel) {
        StringBuilder tableStrBuilder = new StringBuilder("{\n");
        for (LuaValue key: table.keys()) {
            LuaValue value = table.get(key);

            for (int i = 0; i < tabLevel; i++) {
                tableStrBuilder.append(' ');
            }

            if (key.isstring()){
                tableStrBuilder.append("\"").append(key).append("\"");
            } else {
                tableStrBuilder.append(key);
            }

            if (value.istable()) {
                if (!hitTables.contains(value.checktable())) {
                    hitTables.add(value.checktable());
                    tableStrBuilder.append(": ").append(luaTableToString(value.checktable(), tabLevel + tabSize)).append(",\n");
                } else {
                    tableStrBuilder.append(": ").append("<recursive table>").append(",\n");
                }
            } else if (value.isstring()){
                tableStrBuilder.append(": \"").append(value).append("\",").append("\n");
            } else {
                tableStrBuilder.append(": ").append(value).append(",\n");
            }

        }
        for (int i = 0; i < tabLevel - tabSize; i++) {
            tableStrBuilder.append(' ');
        }
        tableStrBuilder.append("}");
        return tableStrBuilder.toString();
    }

    public static class PrintFunc extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            printBuffer.append(args.tojstring()).append("\n");
            return LuaValue.NIL;
        }
    }

    public static String getPrintBufferContents() {
        return printBuffer.toString();
    }

    public static LuaValue getLuaValueFromList(List<?> list) {
        LuaValue array = LuaValue.tableOf();
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            array.set(i+1, element.toString());
        }
        return array;
    }

    public static int getMessagesSentForExecution() {
        return floppaLib.getMsgCount();
    }
}
