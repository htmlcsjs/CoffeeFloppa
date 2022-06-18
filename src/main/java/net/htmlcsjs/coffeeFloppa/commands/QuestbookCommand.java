package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.htmlcsjs.coffeeFloppa.helpers.QuestDefinition;
import net.htmlcsjs.coffeeFloppa.helpers.Questbook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestbookCommand implements ICommand {
    private Questbook qb;
    private final String qbName;

    public QuestbookCommand (String qbName) {
        this.qbName = qbName;
        qb = new Questbook(qbName);
    }

    @Override
    public @NotNull String getName() {
        return qbName + "_quests";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        String[] splitArg = message.getContent().split(" ");
        String arg = String.join(" ", Arrays.copyOfRange(splitArg, 1, splitArg.length));
        int questCount = 0;
        String lastQuestName = "";

        try {
            QuestDefinition quest = qb.questMap.getOrDefault(Long.parseLong(arg), null);
            if (quest != null) {
                return quest.generateMessage(qb);
            } else {
                return "Sorry, that quest could not be found";
            }
        } catch (NumberFormatException e) {
            StringBuilder msgBuilder = new StringBuilder();
            for (String questname : qb.nameMap.keySet()) {
                if (FuzzySearch.partialRatio(arg,questname) > 75) {
                    Pattern pattern = Pattern.compile("§.");
                    Matcher matcher = pattern.matcher(questname);
                    msgBuilder.append(qb.nameMap.get(questname))
                            .append(": ")
                            .append(matcher.replaceAll(""))
                            .append("\n");
                    questCount++;
                    lastQuestName = questname;
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
            msgBuilder.insert(0, "Found " + questCount + " quests matching `" + arg + "`:```\n");
            return msgBuilder.append("```").toString();
        }
    }

    @Override
    public String helpInfo() {
        return "A command to look at quests from the " + qbName + " questbook";
    }
}
