package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import net.htmlcsjs.coffeeFloppa.helpers.lua.LuaHelper;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.jetbrains.annotations.Nullable;

public class EvalCommand implements ICommand{
    @Override
    public @NotNull String getName() {
        return "eval";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        String code = "";
        try {
            code = CommandUtil.getAttachment(message);
        } catch (Exception ignored) {
            code = message.getContent().substring(getName().length() + 1);
            code = code.replace("`", "");
        }
        if (code.lastIndexOf("\n") == code.indexOf("\n") && !code.matches(".*print\\(\".*\"\\).*") && !code.contains("return")) {
            code = "return " + code;
        }

        try {
            Varargs returnValue = LuaHelper.runScriptInSandbox(code, message);

            StringBuilder msgStr = new StringBuilder();
            boolean isPrintEmpty = LuaHelper.getPrintBufferContents().isEmpty();
            boolean auxMessagesSent = LuaHelper.getMessagesSentForExecution() > 0;
            if (!isPrintEmpty) {
                msgStr.append(LuaHelper.getPrintBufferContents()).append("\n");
            }
            if (returnValue.checkboolean(1)) {
                LuaValue returnedData = returnValue.arg(2);
                if (returnedData.istable()) {
                    String tableToStr = LuaHelper.startLuaTableToStr(returnedData.checktable());
                    if (tableToStr.length() < 1750) {
                        msgStr.append("```lua\n").append(tableToStr).append("```\n");
                    } else {
                        msgStr.append(tableToStr).append("\n");
                    }
                } else if (returnedData.isnil() && (!isPrintEmpty || auxMessagesSent)) {
                    //pass
                } else {
                    msgStr.append(returnedData).append("\n");
                }
            } else {
                msgStr.append("<:susso:932766288676266065> there was an error <:amonga:932998646973214720>\n")
                        .append("```lua\n")
                        .append(returnValue.arg(2)).append("```");
            }

            return msgStr.isEmpty() ? null : msgStr.toString();
        } catch (Exception e) {
            return "An error occurred:```java\n" + e.getMessage() + "\n" + CommandUtil.getStackTraceToString(e, 0) + "```";
        }
    }

    @Override
    public String helpInfo() {
        return "Runs code using a lua, more info in the CoffeeFloppa wiki on github";
    }
}
