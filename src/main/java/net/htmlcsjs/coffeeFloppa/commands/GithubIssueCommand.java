package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GithubIssueCommand implements ICommand {

    @Override
    public @NotNull String getName() {
        return "issue";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        String repo;
        try {
            repo = message.getContent().split(" ")[1].toLowerCase();
        } catch (Exception ignored) {
            return "No Github Repo supplied";
        }

        if (repo.equalsIgnoreCase("ceu"))
            return "<https://github.com/GregTechCEu/GregTech/issues/new/choose>";
        if (repo.equalsIgnoreCase("gcy"))
            return "<https://github.com/GregTechCEu/gregicality-legacy/issues/new>";
        if (repo.equalsIgnoreCase("gcym"))
            return "<https://github.com/GregTechCEu/gregicality-multiblocks/issues/new>";
        if (repo.equalsIgnoreCase("gcys"))
            return "<https://github.com/GregTechCEu/gregicality-science/issues/new>";
        if (repo.equalsIgnoreCase("floppa"))
            return "<https://github.com/htmlcsjs/CoffeeFloppa/issues/new>";

        return String.format("GitHub repository %s not found", repo);
    }

    @Override
    public String helpInfo() {
        return "Provides links to issues for github repos";
    }
}