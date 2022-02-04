package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
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
        return "questbook_" + qbName;
    }

    @Override
    public String execute(Message message) {
        String verb = message.getContent().split(" ")[1];
        String[] splitArg = message.getContent().split(" ");
        String arg = String.join(" ", Arrays.copyOfRange(splitArg, 2, splitArg.length));
        if (verb.equalsIgnoreCase("id")) {
            QuestDefinition quest = qb.questMap.get(Long.parseLong(arg));
            if (quest != null) {
                return quest.generateMessage();
            } else {
                return "Sorry, that quest could not be found";
            }
        }
        return "Sorry, that was an invalid input";
    }
}
