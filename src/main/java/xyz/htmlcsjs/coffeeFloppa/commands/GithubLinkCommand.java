package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;

public class GithubLinkCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "github";
    }

    @Override
    public @Nullable String execute(Message message) {
        String[] args = message.getContent().split("\\s");
        String alias;
        if (args.length > 1) {
            alias = args[1];
        } else {
            alias = GithubIssueCommand.getChannelAliasMap().get(message.getChannelId().asString());
        }
        if (alias == null) {
            return String.format("No repository provided\nUsage: `%s%s <repo>` where `repo` is a repository name/alias, "
                    + "can be omitted in any channel that has a default alias (i.e ones which #{issue number] works in)", FloppaTomlConfig.prefix, getName());
        }

        String repo;
        if (alias.contains("/")) { // attempt to use alias as the repo directly
            repo = alias;
        } else {
            repo = GithubIssueCommand.getAliasMap().get(alias);
            if (repo == null) {
                return String.format("No defined repository for alias `%s`", alias);
            }
        }

        return String.format("https://github.com/%s/issues", repo);
    }
}
