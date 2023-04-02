package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Based off of Wiezba#2137's cpp code
public class CleanroomCalcCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "crcalc";
    }

    @Override
    public @Nullable String execute(Message message) {
        String[] args = message.getContent().split(" ");
        if (args.length != 4) {
            return String.format("Usage: %s <x> <y> <z>", args[0]);
        }
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        int z = Integer.parseInt(args[3]);

        if (x < 5 || y < 5 || z < 5 || x > 15 || y > 15 || z > 15) {
            return "⚠️: Invalid size cleanroom";
        }
        return "For a " + x + " by " + y + " by " + z + " cleanroom you need:```" +
                "- " + ((x * z) + (2 * (x * (y - 1))) + (2 * (y - 1) * (z - 2)) - 4) + " plascrete.\n" +
                "- " + ((x - 2) * (z - 2) - 1) + " filters.\n" +
                "- 1 cleanroom controller.\n- 1 energy hatch.\n- 1 maintenance hatch.\n- 1 door.```";
    }

    @Override
    public String helpInfo() {
        return "Calcuates the cost of a certain size cleanroom. Program originally written by Wiezba";
    }
}
