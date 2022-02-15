package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;

public class QuestAdminCommand implements ICommand{
    @Override
    public String getName() {
        return "bq_admin";
    }

    @Override
    public String execute(Message message) {
        String questBook = "", verb = "";
        boolean isAdmin = CommandUtil.getAllowedToRun(message);
        try {
            String[] msgSplit = message.getContent().split(" ");
            questBook = String.join(" ", Arrays.copyOfRange(msgSplit, 2, msgSplit.length));
            verb = msgSplit[1];
        } catch (Exception ignored) {}

        if (verb.equalsIgnoreCase("add")) {
            if (isAdmin) {
                JSONObject jsonData = CoffeeFloppa.getJsonData();
                List<String> qbList = (List<String>) jsonData.get("quest_books");
                if (qbList.contains(questBook)) {
                    return "Use `update` instead of `add`";
                }
                if (questBook.equalsIgnoreCase("")) {
                    return "No Questbook name supplied";
                }

                if (message.getAttachments().size() > 0) {
                    try {
                        URL url = new URL(message.getAttachments().get(0).getUrl());
                        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                        FileOutputStream fileOutputStream = new FileOutputStream("qb/" + questBook + ".json");
                        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                        fileOutputStream.close();
                    } catch (Exception e) {
                        return "An error occurred:\n" + e.getClass().getName();
                    }
                    qbList.add(questBook);
                    jsonData.put("quest_books", qbList);
                    CoffeeFloppa.updateConfigFile(jsonData);
                    return String.format("Added Questbook %s", questBook);
                } else {
                    return "No questbook file attached";
                }
            } else {
                return "You dont have the required permissions for this";
            }
        } else if (verb.equalsIgnoreCase("update")) {
            if (isAdmin) {
                JSONObject jsonData = CoffeeFloppa.getJsonData();
                List<String> qbList = (List<String>) jsonData.get("quest_books");
                if (!qbList.contains(questBook)) {
                    return "Use `add` instead of `update`";
                }
                if (questBook.equalsIgnoreCase("")) {
                    return "No Questbook name supplied";
                }

                if (message.getAttachments().size() > 0) {
                    try {
                        URL url = new URL(message.getAttachments().get(0).getUrl());
                        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                        FileOutputStream fileOutputStream = new FileOutputStream("qb/" + questBook + ".json");
                        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                        fileOutputStream.close();
                    } catch (Exception e) {
                        return "An error occurred:\n" + e.getClass().getName();
                    }
                    CoffeeFloppa.refreshConfig();
                    return String.format("Updated Questbook %s", questBook);
                } else {
                    return "No questbook file attached";
                }
            } else {
                return "You dont have the required permissions for this";
            }
        } else if (verb.equalsIgnoreCase("delete")) {
            if (isAdmin) {
                JSONObject jsonData = CoffeeFloppa.getJsonData();
                List<String> qbList = (List<String>) jsonData.get("quest_books");
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
}
