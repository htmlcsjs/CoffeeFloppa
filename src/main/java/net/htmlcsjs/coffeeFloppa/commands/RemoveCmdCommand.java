package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoveCmdCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "removeCommand";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        if (CommandUtil.getAllowedToRun(message)) {
            String messageValue = message.getContent();
            String call = messageValue.split(" ")[1].toLowerCase();

            boolean removed = false;
            JSONObject jsonData = CoffeeFloppa.getJsonData();
            JSONArray newCommands = new JSONArray();
            for (Object obj : (JSONArray) jsonData.get("commands")) {
                Map<?, ?> commandMap = (Map<?, ?>) obj;
                JSONArray newResponses = new JSONArray();
                if (!(commandMap.get("call").equals(call))) {
                    newResponses.addAll((List<?>) commandMap.get("responses"));
                    Map <String, Object> newCommandMap = new HashMap<>();
                    newCommandMap.put("call", commandMap.get("call"));
                    newCommandMap.put("responses", newResponses);
                    newCommands.add(newCommandMap);
                } else {
                    removed = true;
                }
            }
            jsonData.put("commands", newCommands);
            CoffeeFloppa.updateConfigFile(jsonData);
            if (removed) {
                return "command " + CoffeeFloppa.prefix + call + " was removed";
            } else {
                return "command " + CoffeeFloppa.prefix + call + " wasn't found";
            }
        } else {
            return "Sorry, but you dont have the required permissions";
        }
    }
}
