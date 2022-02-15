package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import net.htmlcsjs.coffeeFloppa.helpers.QuestDefinition;
import net.htmlcsjs.coffeeFloppa.helpers.Questbook;

import java.util.Arrays;

public class QuestbookCommand implements ICommand {
    private Questbook qb;
    private final String qbName;

    public QuestbookCommand (String qbName) {
        this.qbName = qbName;
        qb = new Questbook(qbName);
    }

    @Override
    public String getName() {
        return qbName + "_quests";
    }

    @Override
    public String execute(Message message) {
        String verb = message.getContent().split(" ")[1];
        String[] splitArg = message.getContent().split(" ");
        String arg = String.join(" ", Arrays.copyOfRange(splitArg, 2, splitArg.length));
        int questCount = 0;
        String lastQuestName = "";

        try {
            QuestDefinition quest = qb.questMap.get(Long.parseLong(verb));
            if (quest != null) {
                return quest.generateMessage(qb);
            } else {
                return "Sorry, that quest could not be found";
            }
        } catch (NumberFormatException e) {
            if (verb.equalsIgnoreCase("id")) {
                QuestDefinition quest = qb.questMap.get(Long.parseLong(arg));
                if (quest != null) {
                    return quest.generateMessage(qb);
                } else {
                    return "Sorry, that quest could not be found";
                }
            } else if (verb.equalsIgnoreCase("search")) {
                StringBuilder msgBuilder = new StringBuilder();
                for (String questname : qb.nameMap.keySet()) {
                    for (String str : arg.split(" ")) {
                        if (questname.toLowerCase().contains(str.toLowerCase())) {
                            msgBuilder.append(qb.nameMap.get(questname))
                                    .append(": ")
                                    .append(questname)
                                    .append("\n");
                            questCount++;
                            lastQuestName = questname;
                            break;
                        }
                    }
                }
                if (questCount == 1) {
                    return qb.questMap.get(qb.nameMap.get(lastQuestName)).generateMessage(qb);
                }
                if (msgBuilder.isEmpty()) {
                    msgBuilder.append("Sorry, we couldnt find any quests matching ")
                            .append(arg)
                            .append(" in the database for ")
                            .append(qbName);
                }
                FloppaLogger.logger.info(msgBuilder.toString());
                msgBuilder.insert(0, "Found " + questCount + " quests matching `" + arg + "`:```\n");
                return msgBuilder.append("```").toString();
            }
        }
        return "Sorry, that was an invalid input";
    }
}
