package net.htmlcsjs.coffeeFloppa.helpers.lua;

import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HTTPLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue httpLib = tableOf();

        httpLib.set("get", new httpGet());

        env.set("http", httpLib);
        env.get("package").get("loaded").set("http", httpLib);
        return httpLib;
    }

    public static class httpGet extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue inUrl) {
            if (!inUrl.isstring()) {
                return error("The URL is not a string");
            }

            LuaValue returnInfo = tableOf();
            try {
                URL url = new URI(inUrl.checkjstring()).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                returnInfo.set("status_code", connection.getResponseCode());
                String body = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining("\n"));
                returnInfo.set("body", body);
                Map<String, List<String>> headerMap = connection.getHeaderFields();
                LuaTable headers = tableOf();
                for (String headerName: headerMap.keySet()) {
                    if (headerName != null) {
                        headers.set(headerName, LuaHelper.getLuaValueFromList(headerMap.get(headerName)));
                    }
                }
                returnInfo.set("headers", headers);
                returnInfo.set("status_msg", connection.getResponseMessage());

            } catch (Exception e) {
                return error(e.getMessage() + "\n" + CommandUtil.getStackTraceToString(e, 2));
            }

            return returnInfo;
        }
    }
}
