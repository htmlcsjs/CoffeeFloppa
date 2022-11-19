package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.AllowedMentions;
import gregtech.api.GTValues;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.util.GTUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.htmlcsjs.coffeeFloppa.handlers.MessageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class OcCommand implements ICommand {

    private static final String aboutStr = "This calculates oc according to with CEus logic";
    private static final String errorStr = """
    ```sh
    must be formatted as $oc EU/t duration [flags]
    Flags:
     --ticks - have the duration in ticks
     --tj    - use 2.8 for oc (for the TJ pack)
    ```
    """;
    private static final String[] TJVN = {"ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV", "UEV", "UIV", "UMV", "UXV", "MAX"};


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
                int eut;
                float duration;
                try {
                    eut = Integer.parseInt(args.get(1));
                    duration = Float.parseFloat(args.get(2));
                } catch (NumberFormatException e) {
                    Pattern pattern = Pattern.compile("F.+ing: \"(.+)\"", Pattern.MULTILINE);
                    return pattern.matcher(e.getMessage()).replaceAll("`$1` is not formatted correctly.");
                }
                boolean ticks = args.contains("--ticks");
                boolean tj = args.contains("--tj");

                List<String[]> outputList = new ArrayList<>();

                byte startingTier = GTUtility.getTierByVoltage(eut);
                if (startingTier == GTValues.ULV && !tj) {
                    startingTier = GTValues.LV;
                }

                long lastVoltage = 0;
                for (int v = startingTier; v <= GTValues.MAX; v++) {
                    String[] sus = {"", "", ""};
                    int[] ocResult = OverclockingLogic.standardOverclockingLogic(eut, eut * ((long) Math.pow(4, v - startingTier)), (int) Math.floor(ticks ? duration : (duration * 20)), 14, tj ? 2.8: 2, 4);
                    if (lastVoltage == ocResult[0]) {
                        break;
                    }
                    lastVoltage = ocResult[0];

                    sus[0] = String.format("%,d EU/t", ocResult[0]);
                    if (ticks || ocResult[1] < 10) {
                        sus[1] = String.format("%,dt", ocResult[1]);
                    } else {
                        sus[1] = String.format("%,.2fs", (float) ocResult[1] / 20);
                    }
                    sus[2] = tj ? TJVN[v] : GTValues.VN[v];
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
                String time = ticks ? String.format("%,ft", duration) : String.format("%,.2fs", duration);
                byte finalStartingTier = startingTier;
                MessageHandler.sendRegisterMessage(message, message.getChannel().flatMap(channel -> channel.createMessage().withEmbeds(
                                EmbedCreateSpec.builder().title(String.format("%,d EU/t (%s) for %s", eut, GTValues.VN[finalStartingTier], time) + (tj ? " in TJ" : ""))
                                        .addField("EU/t", eutStr.append("```").toString(), true)
                                        .addField("Time", timeStr.append("```").toString(), true)
                                        .addField("Voltage", voltageStr.append("```").toString(), true)
                                        .build()
                        ).withMessageReference(message.getId())
                        .withAllowedMentions(AllowedMentions.suppressEveryone())));
                return null;
            }
        } catch (IndexOutOfBoundsException ignored) {
            epicFormattingFail = true;
        }
        return epicFormattingFail ? aboutStr + "\n\n" + errorStr : aboutStr;
    }

    @Override
    public String helpInfo() {
        return aboutStr;
    }
}
