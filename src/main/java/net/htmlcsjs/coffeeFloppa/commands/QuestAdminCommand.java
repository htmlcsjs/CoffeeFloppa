package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class QuestAdminCommand implements ICommand{
    @Override
    public @NotNull String getName() {
        return "bq_admin";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        String questBook = "", verb = "", urlStr = "";
        boolean isAdmin = CommandUtil.getAllowedToRun(message);
        try {
            String[] msgSplit = message.getContent().split(" ");
            verb = msgSplit[1];
            questBook = msgSplit[2];
            urlStr = msgSplit[3];
        } catch (Exception ignored) {}

        if (verb.equalsIgnoreCase("add")) {
            if (isAdmin) {
                JSONObject jsonData = CoffeeFloppa.getJsonData();
                List<String> qbList = new ArrayList<>((List<String>) jsonData.getOrDefault("quest_books", Collections.emptyList()));
                if (qbList.contains(questBook)) {
                    return "Use `update` instead of `add`";
                }
                if (questBook.equalsIgnoreCase("")) {
                    return "No Questbook name supplied";
                }
                try {
                    String attachmentReturnStr = CommandUtil.getAttachment(message);
                    File outFile = new File("qb/" + questBook + ".json");
                    outFile.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream("qb/" + questBook + ".json");
                    fileOutputStream.write(attachmentReturnStr.getBytes());
                    fileOutputStream.close();
                } catch (Exception e) {
                    return "```" + e.getMessage() + "```";
                }

                qbList.add(questBook);
                jsonData.put("quest_books", qbList);
                CoffeeFloppa.updateConfigFile(jsonData);
                FloppaLogger.logger.info(String.format("Added Questbook %s", questBook));
                return String.format("Added Questbook %s", questBook);
            } else {
                return "You dont have the required permissions for this";
            }
        } else if (verb.equalsIgnoreCase("update")) {
            if (isAdmin) {
                JSONObject jsonData = CoffeeFloppa.getJsonData();
                List<String> qbList = (List<String>) jsonData.getOrDefault("quest_books", Collections.emptyList());
                if (!qbList.contains(questBook)) {
                    return "Use `add` instead of `update`";
                }
                if (questBook.equalsIgnoreCase("")) {
                    return "No Questbook name supplied";
                }
                try {
                    String attachmentReturnStr = CommandUtil.getAttachment(message);
                    File outFile = new File("qb/" + questBook + ".json");
                    outFile.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream("qb/" + questBook + ".json");
                    fileOutputStream.write(attachmentReturnStr.getBytes());
                    fileOutputStream.close();
                } catch (Exception e) {
                    return "```" + e.getMessage() + "```";
                }
                CoffeeFloppa.refreshData();
                FloppaLogger.logger.info(String.format("Updated Questbook %s", questBook));
                return String.format("Updated Questbook %s", questBook);
            } else {
                return "You dont have the required permissions for this";
            }
        } else if (verb.equalsIgnoreCase("delete")) {
            if (isAdmin) {
                JSONObject jsonData = CoffeeFloppa.getJsonData();
                List<String> qbList = (List<String>) jsonData.getOrDefault("quest_books", Collections.emptyList());
                if (!qbList.contains(questBook)) {
                    return "Questbook not found";
                }
                if (questBook.equalsIgnoreCase("")) {
                    return "No Questbook name supplied";
                }
                try {
                    File qbFile = new File("qb/"+questBook+".json");
                    if (qbFile.delete()) {
                        qbList.remove(questBook);
                        jsonData.put("quest_books", qbList);
                        CoffeeFloppa.updateConfigFile(jsonData);
                        FloppaLogger.logger.info(String.format("Deleted questbook %s", questBook));
                        return String.format("Deleted questbook %s", questBook);
                    } else {
                        return String.format("Couldn't delete questbook %s", questBook);
                    }
                } catch (Exception ignored) {
                    return "An error occurred";
                }
            } else {
                return "You dont have the required permissions for this";
            }
        }
        return """
                Options:
                 - add: adds a new questbook
                 - update: updates a questbook
                 - delete: deletes a questbook""";
    }

    @Override
    public String helpInfo() {
        return "Mod only command, DO NOT USE";
    }
}
