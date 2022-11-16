package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class WikiCommand implements ICommand {

    private static final String ENDPOINT = "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=";
    private static final String WIKI_PAGE = "https://en.wikipedia.org/wiki/";
    private static final String TITLE_KEY = "\"title\":";

    @Override
    public @NotNull String getName() {
        return "wiki";
    }

    @Override
    public String helpInfo() {
        return "Searches Wikipedia";
    }

    @Override
    public @Nullable String execute(Message message) {
        String msg = Arrays.stream(message.getContent().split(" ")).skip(1).collect(Collectors.joining("+"));
        if (msg.isEmpty()) {
            return "Missing argument for wiki lookup.";
        }

        // make a request to wikipedia for the search result of the message
        String rawResult;
        try {
            URL url = new URL(ENDPOINT + msg.replace(" ", "+"));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                rawResult = reader.readLine();
            } catch (IOException e) {
                CoffeeFloppa.handleException(e);
                return String.format("Failed to get response from Wikipedia: `%s`", e.getMessage());
            }
        } catch (MalformedURLException e) {
            return String.format("Malformed URL: `%s`", e.getMessage()) + e.getMessage();
        }

        if (rawResult == null || rawResult.isEmpty()) {
            return "No page found.";
        }

        // parse the json response manually
        int location = rawResult.indexOf(TITLE_KEY);
        if (location < 0) {
            return "Could not find page.";
        }
        int endPoint = rawResult.indexOf(',', location);
        if (endPoint < 0) return "Could not find page.";

        // pull out the page title and remove invalid spaces and quotes
        final String result = StringEscapeUtils.unescapeJson(rawResult.substring(location + TITLE_KEY.length(), endPoint)
                .replace(" ", "_")
                .replace("\"", ""));

        // return the link to the wiki page
        return WIKI_PAGE + result;
    }
}
