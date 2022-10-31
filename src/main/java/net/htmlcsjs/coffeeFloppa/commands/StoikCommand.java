package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.AllowedMentions;
import net.htmlcsjs.coffeeFloppa.handlers.MessageHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StoikCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "stoik";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        String formulaStr;
        try {
            String[] splitFormula = message.getContent().split(" ");
            formulaStr = String.join(" ", Arrays.copyOfRange(splitFormula, 1, splitFormula.length));
        } catch (Exception ignored) {
           return "No formula supplied";
        }

        formulaStr = formulaStr.replace("`", "");
        formulaStr = formulaStr.replace("=", "->");

        String[] formulaSides = formulaStr.split("->");
        if (formulaSides.length !=2) {
            return """
                    Error, malformed expression
                    Please format equation as `Reactant1 + Reactant2 -> Product1 + Product2`
                    E.g. `6CO2 + 6H2O -> C6H12O6 + 6O2`
                    """;
        }

        List<String> reactants = new ArrayList<>();
        for (String formula: formulaSides[0].split("\\+")) {
            try {
                reactants.addAll(parseChemical(formula));
            } catch (IllegalArgumentException e) {
                return String.format("Formula `%s` has unpaired brackets.", formula);
            }
        }
        List<String> products = new ArrayList<>();
        for (String formula: formulaSides[1].split("\\+")) {
            try {
                products.addAll(parseChemical(formula));
            } catch (IllegalArgumentException e) {
                return String.format("Formula `%s` has unpaired brackets.", formula);
            }
        }
        reactants.removeAll(Collections.singleton(""));
        products.removeAll(Collections.singleton(""));

        Map<String, Integer> reactantsMap = new HashMap<>();
        Map<String, Integer> productsMap = new HashMap<>();

        for (String element: reactants) {
            int newVal = reactantsMap.getOrDefault(element, 0) + 1;
            if (newVal != 0) {
                reactantsMap.put(element, newVal);
            }
        }
        for (String element: products) {
            int newVal = productsMap.getOrDefault(element, 0) + 1;
            if (newVal != 0) {
                productsMap.put(element, newVal);
            }
        }

        StringBuilder reactantsStrBuilder = new StringBuilder("```");
        StringBuilder productsStrBuilder = new StringBuilder("```");
        StringBuilder clayStrBuilder = new StringBuilder("```");
        boolean balanced = true;

        List<String> elementKeys = new ArrayList<>();
        elementKeys.addAll(productsMap.keySet());
        elementKeys.addAll(reactantsMap.keySet());

        for (String element: elementKeys.stream().distinct().sorted().toList()) {
            if (productsMap.getOrDefault(element, -1).equals(reactantsMap.getOrDefault(element, -1))) {
                clayStrBuilder.append("✅✅");
            } else {
                clayStrBuilder.append("❌❌");
                balanced = false;
            }
            productsStrBuilder.append(element).append(element.length() == 1 ? ":  " : ": ").append(productsMap.getOrDefault(element, 0)).append("\n");
            reactantsStrBuilder.append(element).append(element.length() == 1 ? ":  " : ": ").append(reactantsMap.getOrDefault(element, 0)).append("\n");

            clayStrBuilder.append("\n");
        }

        clayStrBuilder.append("```");
        reactantsStrBuilder.append("```");
        productsStrBuilder.append("```");
        String balancedMsg;

        if (balanced) {
            balancedMsg = "\u2705 Your reaction is balanced \u2705";
        } else {
            balancedMsg = "\u274C Your reaction is unbalanced \u274C";
        }

        MessageHandler.sendRegisterMessage(message, message.getChannel().flatMap(channel -> channel.createMessage().withEmbeds(
                EmbedCreateSpec.builder().addField("Reactants", reactantsStrBuilder.toString(), true)
                        .addField("Products", productsStrBuilder.toString(), true)
                        .addField("Balanced", clayStrBuilder.toString(), true)
                        .addField("Status", balancedMsg, false)
                        .build()
                ).withMessageReference(message.getId())
                .withAllowedMentions(AllowedMentions.suppressEveryone())));
        return null;

    }

    public static List<String> parseChemical(String formula, String multiplier) {
        formula = formula.replace(" ", "");

        List<String> elements = new ArrayList<>();
        Status status = Status.LOOKING_COMPOUND_MULTIPLIER;
        StringBuilder count = new StringBuilder();
        StringBuilder element = new StringBuilder();
        int bracketLevel = 0;
        int balancedBrackets = 0;
        int bracketStartPos = -1;
        boolean isStateSymbol = false;


        for (String c : formula.split("")) {
            switch (c) {
                case "(", "[" -> balancedBrackets++;
                case ")", "]" -> balancedBrackets--;
            }
        }
        if (balancedBrackets != 0) {
            throw new IllegalArgumentException("Formula has unpaired brackets");
        }

        for (int i = 0; i < formula.length(); i++) {
            String character = formula.split("")[i];
            int j = 0;
            int stopPoint = 1;
            do {
                j++;
                switch (status) {
                    case NORMAL -> {
                        if (character.matches("[0-9]")) {
                            count.append(character);
                        } else if (character.matches("[\\[(]")) {
                            status = Status.IN_BRACKET;
                            addElements(elements, count, element, multiplier);
                            count = new StringBuilder();
                            element = new StringBuilder();
                            stopPoint++;
                        } else if (!character.matches("[a-z()\\[\\]]")) {
                            addElements(elements, count, element, multiplier);
                            count = new StringBuilder();
                            element = new StringBuilder(character);
                        } else if (!character.matches("[])]")){
                            element.append(character);
                        }
                    }
                    case IN_BRACKET -> {
                        if (character.matches("[\\[(]")) {
                            if (bracketLevel == 0) {
                                bracketStartPos = i;
                            }
                            bracketLevel++;
                        } else if (character.matches("[])]")) {
                            bracketLevel--;
                            if (bracketLevel == 0) {
                                status = Status.LOOKING_BRACKET_COUNT;
                                isStateSymbol = false;
                            }
                        } else if (i == bracketStartPos + 1 && character.matches("[a-z]")) {
                            isStateSymbol = true;
                        }
                        if (!isStateSymbol) {
                            element.append(character);
                        }
                    }
                    case LOOKING_BRACKET_COUNT -> {
                        if (character.matches("[0-9]")) {
                            count.append(character);
                        } else {
                            status = Status.NORMAL;
                            stopPoint++;
                        }
                    }
                    case LOOKING_COMPOUND_MULTIPLIER -> {
                        if (character.matches("[0-9]")) {
                            count.append(character);
                        } else {
                            status = Status.NORMAL;
                            if (i != 0) {
                                multiplier = count.toString();
                            }
                            count = new StringBuilder();
                            stopPoint++;
                        }
                    }
                }
            } while (j < stopPoint);
        }
        addElements(elements, count, element, multiplier);

        return elements;
    }

    public static List<String> parseChemical(String formula) {
        return parseChemical(formula, "1");
    }

    private static void addElements(List<String> elements, StringBuilder count, StringBuilder element, String multiplier) {
        if (count.isEmpty()) {
            count.append("1");
        }
        long amount = Long.parseLong(count.toString()) * Long.parseLong(multiplier);
        if (element.toString().matches("[\\[(].*[])]")) {
            List<String> subchemParsed = parseChemical(element.substring(1, element.length() - 1));
            for (long j = 0; j < amount; j++) {
                elements.addAll(subchemParsed);
            }
        } else {
            if (element.toString().matches("[()\\[\\]]") || element.length() < 1) {
                return;
            }
            for (long j = 0; j < amount; j++) {
                elements.add(element.toString());
            }
        }
    }

    private enum Status {
        NORMAL,
        IN_BRACKET,
        LOOKING_BRACKET_COUNT,
        LOOKING_COMPOUND_MULTIPLIER
    }

    @Override
    public String helpInfo() {
        return "Check weather a chemical equation is balanced";
    }
}
