package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import gregtech.api.GTValues;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.util.GTUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OcCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "oc";
    }

    @Override
    public @Nullable String execute(Message message) {

        List<String> args = List.of(message.getContent().split(" "));
        boolean epicFormattingFail = false;

        try {
            if (args.get(1).equalsIgnoreCase("ebf")) {
                //OverclockingLogic.heatingCoilOverclockingLogic(args[2]) todo
            } else {
                int eut = Integer.parseInt(args.get(1));
                float duration = Float.parseFloat(args.get(2));
                boolean ticks = args.contains("--ticks");

                List<String[]> outputList = new ArrayList<>();

                byte startingVoltage = GTUtility.getTierByVoltage(eut);
                for (int v = startingVoltage; v <= GTValues.MAX; v++) {
                    String[] sus = {"", "", ""};
                    int[] ocResult = OverclockingLogic.standardOverclockingLogic(eut, GTValues.V[v], (int) Math.floor(ticks ? duration : (duration * 20)), 2, 4, 14);
                    sus[0] = String.format("%,d EU/t", ocResult[0]);
                    if (ticks || ocResult[1] < 10) {
                        sus[1] = String.format("%,dt", ocResult[1]);
                    } else {
                        sus[1] = String.format("%,.2fs", (float)ocResult[1] / 20);
                    }
                    sus[2] = GTValues.VN[v];
                    outputList.add(sus);
                }

                StringBuilder outputStr = new StringBuilder("```lua\n");
                for (String[] strs :outputList) {
                    outputStr.append(String.join(" | ", strs)).append("\n");
                }
                return outputStr.append("```").toString();
            }
        } catch (IndexOutOfBoundsException ignored) {
            epicFormattingFail = true;
        }
        String errorStr = "must be formatted as eut, duration, and have the flag `--ticks` if you want answers in ticks";
        return errorStr;
    }
}
