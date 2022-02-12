package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;

public class GithubIssueCommand implements ICommand {

    @Override
    public String getName() {
        return "issue";
    }

    @Override
    public String execute(Message message) {
        String repo = message.getContent().split(" ")[1].toLowerCase();
        if (repo.equalsIgnoreCase("ceu"))
            return "<https://github.com/GregTechCEu/GregTech/issues/new/choose>";
        if (repo.equalsIgnoreCase("gcy"))
            return "<https://github.com/GregTechCEu/gregicality-legacy/issues/new>";
        if (repo.equalsIgnoreCase("gcym"))
            return "<https://github.com/GregTechCEu/gregicality-multiblocks/issues/new>";
        if (repo.equalsIgnoreCase("gcys"))
            return "<https://github.com/GregTechCEu/gregicality-science/issues/new>";

        return String.format("GitHub repository %s not found", repo);
    }
}