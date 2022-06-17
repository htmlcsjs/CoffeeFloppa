package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.AllowedMentions;
import gregtech.api.GTValues;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.util.GTUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OcCommand implements ICommand {

    private static final String aboutStr = "This calculates oc according to gt's logic";
    private static final String errorStr = "must be formatted as eut, duration, and have the flag `--ticks` if you want answers in ticks";

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

                StringBuilder eutStr = new StringBuilder("```lua\n");
                StringBuilder timeStr = new StringBuilder("```lua\n");
                StringBuilder voltageStr = new StringBuilder("```lua\n");
                for (String[] strs :outputList) {
                    eutStr.append(strs[0]).append("\n");
                    timeStr.append(strs[1]).append("\n");
                    voltageStr.append(strs[2]).append("\n");
                }
                String time = ticks ? String.format("%,ft", duration) : String.format("%,.2fs", duration / 20);
                message.getChannel().flatMap(channel -> channel.createMessage().withEmbeds(
                                EmbedCreateSpec.builder().title(String.format("%,d EU/t (%s) for (%s)", eut, GTValues.VN[startingVoltage], time))
                                        .addField("EU/t", eutStr.append("```").toString(), true)
                                        .addField("Time", timeStr.append("```").toString(), true)
                                        .addField("Voltage", voltageStr.append("```").toString(), true)
                                        .build()
                        ).withMessageReference(message.getId())
                        .withAllowedMentions(AllowedMentions.suppressEveryone())).subscribe();
                return null;
            }
        } catch (IndexOutOfBoundsException ignored) {
            epicFormattingFail = true;
        }
        return epicFormattingFail ? aboutStr + "\n\n" + errorStr : aboutStr;
    }
}
