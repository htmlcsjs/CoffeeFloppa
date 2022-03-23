package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.helpers.lua.LuaHelper;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class EvalCommand implements ICommand{
    @Override
    public String getName() {
        return "eval";
    }

    @Override
    public String execute(Message message) {
        String code = message.getContent().substring(getName().length() + 1);
        if (code.indexOf("```") == 1) {
            code = code.substring(3);
        } else if (code.indexOf("`") == 1) {
            code = code.substring(2);
        }
        if (code.lastIndexOf("```") == code.length()-3) {
            code = code.substring(0, code.length()-4);
        } else if (code.lastIndexOf("`") == code.length()-1) {
            code = code.substring(0, code.length()-2);
        }

        try {
            Varargs returnValue = LuaHelper.runScriptInSandbox(code);

            StringBuilder msgStr = new StringBuilder();
            if (returnValue.checkboolean(1)) {
                LuaValue returnedData = returnValue.arg(2);
                if (returnedData.istable()) {
                    msgStr.append("```lua\n").append(LuaHelper.luaTableToString(returnedData.checktable(), 4)).append("```\n");
                } else {
                    msgStr.append(returnedData).append("\n");
                }
            } else {
                msgStr.append("<:susso:932766288676266065> there was an error <:amonga:932998646973214720>\n")
                        .append("```lua\n")
                        .append(returnValue.arg(2)).append("```");
            }

            return msgStr.toString();
        } catch (Exception e) {
            StringBuilder stackTrace = new StringBuilder();
            int i = 0;
            for (StackTraceElement ste: e.getStackTrace()) {
                stackTrace.append(ste).append("\n");
                if (i > 20) {
                    break;
                }
                i++;
            }
            return "An error occurred:```java\n" + e.getMessage() + "\n" + stackTrace + "```";
        }
    }
}
