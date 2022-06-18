package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class AddonCommand implements ICommand{
    @Override
    public @NotNull String getName() {
        return "addons";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        String addon;
        try {
            String[] splitAddon = message.getContent().split(" ");
            addon = String.join(" ", Arrays.copyOfRange(splitAddon, 1, splitAddon.length));
        } catch (Exception ignored) {
            return "No addon name supplied supplied";
        }

        return switch (addon.toLowerCase()) {
            case "gtfo", "gregtech food option" -> "GTFO adds various food chains and machines, to make... food! Making Bread or Coffee won't be too easy, and will require some lovely automation. A nice alternate challenge.";
            case "htmltech" -> "htmlTech adds an assortment of things. The main focus is on energy transportation thanks to lasers, as seen in TecTech.";
            case "gcys", "gregicality science" -> "Gregicality Science (still WIP), formerly known as simply Gregicality, is a huge addon. It extends the lategame with 5 additional voltage tiers, and adds some additional challenges to the earlier tiers, such as a harder Platinum Group processing line. If you want to play with it, you may choose a different modpack, though.";
            case "gcym", "gregicality multiblocks" -> "Gregicality Multiblocks adds Multiblocks (crazy huh?) for the late game. These multiblocks are improved versions of single block machines which can perform many recipes in parallel. A must have for large scale automation.";
            case "gregification" -> "Gregification is a mod which aims to bridge the gap between GregTech: CE Unofficial (and its addons) and other mods. It adds content in older CE addons and new content. It is still WIP and not on curseforge yet";
            case "mechtech" -> "MechTech adds a modular armor that can be configured heavily, great as QoL. It also has a Tesla Tower, partially inspired by TecTech.";
            case "" -> "No addon name supplied supplied";
            default -> String.format("Addon %s not recognised", addon);
        };

    }

    @Override
    public String helpInfo() {
        return "lists a bunch of CEu addons";
    }
}
