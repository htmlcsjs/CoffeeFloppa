package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;

import java.util.Arrays;

public class StoikCommand implements ICommand {
    @Override
    public String getName() {
        return "stoik";
    }

    @Override
    public String execute(Message message) {
        String formulaStr = "";
        try {
            String[] splitFormula = message.getContent().split(" ");
            formulaStr = String.join(" ", Arrays.copyOfRange(splitFormula, 1, splitFormula.length));
        } catch (Exception ignored) {
           // return "No formula supplied";
        }

        return "```";//json \n" + StringEscapeUtils.escapeJson(CommandUtil.getMessageJson(message).toJSONString()) + "```";
        ///return "```json \n" + StringEscapeUtils.escapeJson(CommandUtil.getMessageJson(message).toJSONString()) + "```";
    }
}
