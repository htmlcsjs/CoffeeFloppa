package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import xyz.htmlcsjs.coffeeFloppa.handlers.MessageHandler;
import xyz.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import xyz.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class GithubIssueCommand implements ICommand {

    private static final String apiRoot = "https://api.github.com/";
    private static final Map<String, String> aliasMap = new HashMap<>();
    private static final Map<String, String> categoryAliasMap = new HashMap<>();


    public GithubIssueCommand() {
        aliasMap.clear();
        for (String str : FloppaTomlConfig.repoAliases.split(";")) {
            List<String> split = new ArrayList<>(List.of(str.split(":")));
            String alias = split.remove(0);
            String repo;
            if (split.size() > 1) {
                repo = String.join(":", split);
            } else {
                repo = split.get(0);
            }
            aliasMap.put(alias, repo);
        }
        categoryAliasMap.clear();
        for (String str : FloppaTomlConfig.channelAliases.split(";")) {
            List<String> split = new ArrayList<>(List.of(str.split(":")));
            String alias = split.remove(0);
            String repo;
            if (split.size() > 1) {
                repo = String.join(":", split);
            } else {
                repo = split.get(0);
            }
            categoryAliasMap.put(alias, repo);
        }
    }

    @Override
    public @NotNull String getName() {
        return "gh";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        Map<String, String> issueMap = new HashMap<>();
        CommandUtil.ghIssuePattern.matcher(message.getContent())
                .results()
                .limit(5)
                .forEach(matchResult -> issueMap.put(matchResult.group(1), matchResult.group(2)));

        CommandUtil.ghIssueSmallPattern.matcher(message.getContent())
                .results()
                .limit(5 - issueMap.size())
                .forEach(matchResult -> {
                    if (matchResult.group(1) == null) {
                        MessageChannel channel = message.getChannel().block();
                        if (channel != null && categoryAliasMap.containsKey(channel.getId().asString())) {
                            issueMap.put(aliasMap.get(categoryAliasMap.get(channel.getId().asString())), matchResult.group(2));
                        } else if (channel instanceof ThreadChannel thread) {
                            Optional<Snowflake> parentId = thread.getParentId();
                            if (parentId.isPresent() && categoryAliasMap.containsKey(parentId.get().asString())) {
                                issueMap.put(aliasMap.get(categoryAliasMap.get(parentId.get().asString())), matchResult.group(2));
                            }
                        }
                    } else if (aliasMap.containsKey(matchResult.group(1))){
                        issueMap.put(aliasMap.get(matchResult.group(1)), matchResult.group(2));
                    }
                });

        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(5)).build();
        List<EmbedCreateSpec> embeds = new ArrayList<>();

        if (issueMap.isEmpty()) {
            return "No valid issue descriptors supplied\nUse the format `owner/repo#issueid` or `alias#issueid`";
        }

        issueMap.forEach((repo, id) -> {
            if (!repo.matches("[\\w.-]+/[\\w.-]+")) {
                if (aliasMap.containsKey(repo)) {
                    repo = aliasMap.get(repo);
                } else {
                    return;
                }
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(apiRoot + "repos/" + repo + "/issues/" + id));
            if (!FloppaTomlConfig.githubToken.isEmpty()) {
                builder.header("Authorization", String.format("Bearer %s", FloppaTomlConfig.githubToken));
            }
            try {
                HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                JSONObject issueJSON = (JSONObject) new JSONParser().parse(response.body());
                if (response.statusCode() != 200) {
                    embeds.add(EmbedCreateSpec.builder()
                            .title(String.format("Couldn't find issue %s in repo %s", id, repo))
                            .addField("status", String.format("`%d`", response.statusCode()), true)
                            .addField("message", String.format("`%s`", issueJSON.get("message")), true)
                            .build());
                } else {
                    EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                            .title(Possible.of((String) issueJSON.get("title")))
                            .url((String) issueJSON.get("html_url"));
                    if (issueJSON.containsKey("user") && issueJSON.get("user") instanceof JSONObject userData) {
                        embedBuilder.author(userData.containsKey("name") ? (String) userData.get("name") : (String) userData.get("login"), (String) userData.get("html_url"), (String) userData.get("avatar_url"));
                    }
                    if (issueJSON.containsKey("body") && issueJSON.get("body") instanceof String rawBody) {
                        StringBuilder body = new StringBuilder();
                        int i = 0;
                        for (String s : rawBody.split("\n")) {
                            if (i > 10) {
                                body = new StringBuilder(body.toString().trim() + "...\n");
                                break;
                            }
                            body.append(s).append("\n");
                            i++;
                        }
                        embedBuilder.description(CommandUtil.trimString(body.toString(), 1000, "..."));
                    } else {
                        embedBuilder.description("*No description*");
                    }
                    embedBuilder.timestamp(Instant.parse((String) issueJSON.get("updated_at")));
                    embeds.add(embedBuilder.build());
                }
            } catch (IOException | InterruptedException | ParseException e) {
                CoffeeFloppa.handleException(e);
            }
        });
        MessageHandler.sendRegisterMessage(message, message.getChannel().flatMap(channel -> channel.createMessage()
                .withEmbeds(embeds)
                .withMessageReference(message.getId())
                .withAllowedMentions(AllowedMentions.suppressEveryone())));
        return null;
    }

    @Override
    public String helpInfo() {
        return "Provides links to issues for github repos";
    }
}