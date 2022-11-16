package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import xyz.htmlcsjs.coffeeFloppa.FloppaLogger;
import xyz.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import xyz.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddCmdCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "addCommand";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        if (CommandUtil.getAllowedToRun(message)) {
            String messageValue = message.getContent();
            String call = messageValue.split(" ")[1].toLowerCase();
            String[] splitResponse = messageValue.split(" ");
            String response = String.join(" ", Arrays.copyOfRange(splitResponse, 2, splitResponse.length));

            boolean exists = false;
            JSONObject jsonData = CoffeeFloppa.getJsonData();
            JSONArray newCommands = new JSONArray();
            for (Object obj : (JSONArray) jsonData.get("commands")) {
                Map<?, ?> commandMap = (Map<?, ?>) obj;
                JSONArray newResponses = new JSONArray();
                if (((String)commandMap.get("call")).equalsIgnoreCase(call)) {
                    newResponses.add(response);
                    exists = true;
                }
                newResponses.addAll((List<?>) commandMap.get("responses"));
                Map <String, Object> newCommandMap = new HashMap<>();
                newCommandMap.put("call", commandMap.get("call"));
                newCommandMap.put("responses", newResponses);
                newCommands.add(newCommandMap);
            }
            if (!exists) {;
                Map <String, Object> newCommandMap = new HashMap<>();
                JSONArray newResponses = new JSONArray();
                newResponses.add(response);
                newCommandMap.put("call", call);
                newCommandMap.put("responses", newResponses);
                newCommands.add(newCommandMap);
            }
            jsonData.put("commands", newCommands);
            CoffeeFloppa.updateConfigFile(jsonData);
            if (exists) {
                return "command " + FloppaTomlConfig.prefix + call + " was edited";
            } else {
                return "command " + FloppaTomlConfig.prefix + call + " was added";
            }
        } else {
            FloppaLogger.logger.warn(message.getAuthor().get().getTag() + " is being very naughty");
            return "Sorry, but you dont have the required permissions";
        }
    }

    @Override
    public String helpInfo() {
        return "Mod only command, DO NOT USE";
    }
}
