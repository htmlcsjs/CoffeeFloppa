package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class AddCmdCommand implements ICommand {
    @Override
    public String getName() {
        return "addCommand";
    }

    @Override
    public String execute(Message message) {
        boolean validRole = false;
        Snowflake guildID = message.getGuild().block().getId();
        Set<Snowflake> userRoleIDs = (message.getAuthor().get().asMember(guildID).block().getRoleIds());
        for (String str : CoffeeFloppa.adminRolesByGuild.values()) {
            if (userRoleIDs.contains(Snowflake.of(str))) {
                validRole = true;
            }
        }
        if (message.getAuthor().get().getId().equals(CoffeeFloppa.admin)) {
            validRole = true;
        }
        if (validRole) {
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
                if (commandMap.get("call").equals(call)) {
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
                return "command " + CoffeeFloppa.prefix + call + " was edited";
            } else {
                return "command " + CoffeeFloppa.prefix + call + " was added";
            }
        } else {
            FloppaLogger.logger.warn(message.getAuthor().get().getTag() + " is being very naughty");
            return "Sorry, but you dont have the required permissions";
        }
    }
}
