package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.*;

public class StoikCommand implements ICommand {
    private String elementMultiplier;

    @Override
    public String getName() {
        return "stoik";
    }

    @Override
    public String execute(Message message) {
        String formulaStr = "";
        try {
            String[] splitFormula = message.getContent().split(" ");
            formulaStr = String.join(" ", Arrays.copyOfRange(splitFormula, 1, splitFormula.length));
        } catch (Exception ignored) {
           return "No formula supplied";
        }

        formulaStr = formulaStr.replace("`", "");

        String[] formulaSides = formulaStr.split("->");
        if (formulaSides.length !=2) {
            return "Error, malformed expression";
        }

        List<String> reactants = new ArrayList<>();
        for (String formula: formulaSides[0].split("\\+")) {
            elementMultiplier = "";
            reactants.addAll(parseChemical(formula));
        }
        List<String> products = new ArrayList<>();
        for (String formula: formulaSides[1].split("\\+")) {
            elementMultiplier = "";
            products.addAll(parseChemical(formula));
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

        for (String element: elementKeys.stream().distinct().toList()) {
            if (productsMap.getOrDefault(element, -1) == reactantsMap.getOrDefault(element, -1)) {
                clayStrBuilder.append("✅\u2705");
            } else {
                clayStrBuilder.append("\u274C");
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

        message.getChannel().flatMap(channel -> channel.createMessage().withEmbeds(
                EmbedCreateSpec.builder().addField("Reactants", reactantsStrBuilder.toString(), true)
                        .addField("Products", productsStrBuilder.toString(), true)
                        .addField("Balanced", clayStrBuilder.toString(), true)
                        .addField("Status", balancedMsg, false)
                        .build()
        ).withMessageReference(message.getId())).subscribe();
        return null;

    }

    private List<String> parseChemical(String formula) {
        formula = formula.strip();
        List<String> elements = new ArrayList<>();

        String inProgressElement = "";
        String inProgressCount = "";

        int subChemCount = 0;
        boolean lookingForSubCount = false;
        boolean lookingForMultiplier = false;
        String[] splitFormula = formula.split("");
        for (int i = 0; i < splitFormula.length; i++) {
            String str = splitFormula[i];

            if (str.equals("(")) {
                subChemCount++;
                if (subChemCount > 1) {
                    inProgressElement += str;
                }
            } else if (str.equals(")")) {
                if (subChemCount == 1) {
                    lookingForSubCount = true;
                }
                subChemCount--;
                if (subChemCount > 0) {
                    inProgressElement += str;
                }
            } else if (subChemCount > 0) {
                inProgressElement += str;
                continue;
            } else if (str.matches("[A-Z?]")) {
                elementDefEndHandler(elements, lookingForSubCount, inProgressCount, inProgressElement);

                if (lookingForMultiplier) {
                    lookingForMultiplier = false;
                }
                inProgressCount = "";
                inProgressElement = str;
            } else if (str.matches("[0-9]")) {
                if (i == 0) {
                    lookingForMultiplier = true;
                }
                if (lookingForMultiplier) {
                    elementMultiplier += str;
                } else if (subChemCount == 0) {
                    inProgressCount += str;
                }
            }  else {
                inProgressElement += str;
            }
            if (i + 1 == splitFormula.length) {
                elementDefEndHandler(elements, lookingForSubCount, inProgressCount, inProgressElement);
            }
        }
        return elements;
    }

    private void elementDefEndHandler(List<String> elements, boolean lookingForSubCount, String inProgressCount, String inProgressElement) {
        if (inProgressCount.equals("")) {
            inProgressCount = "1";
        }
        if (elementMultiplier.equals("")) {
            elementMultiplier = "1";
        }
        if (lookingForSubCount) {
            List<String> subChemAtoms = parseChemical(inProgressElement);
            for (int i = 0; i < Long.parseLong(inProgressCount) * Long.parseLong(elementMultiplier); i++) {
                elements.addAll(subChemAtoms);
            }
        } else {
            for (int i = 0; i < Long.parseLong(inProgressCount) * Long.parseLong(elementMultiplier); i++) {
                elements.add(inProgressElement);
            }
        }
    }
}
