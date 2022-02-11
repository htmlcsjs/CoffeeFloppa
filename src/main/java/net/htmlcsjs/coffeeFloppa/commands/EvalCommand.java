package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Arrays;

public class EvalCommand implements ICommand{
    @Override
    public String getName() {
        return "eval";
    }

    @Override
    public String execute(Message message) {
        String verb = message.getContent().split(" ")[1];
        String[] splitArg = message.getContent().split(" ");
        String arg = String.join(" ", Arrays.copyOfRange(splitArg, 2, splitArg.length));
        if (verb.equalsIgnoreCase("run")) {
            try {
                FileWriter pyWriter = new FileWriter("tmp.py");
                StringBuilder codeBuilder = new StringBuilder();
                codeBuilder.append("import math, numpy");
                for (String str: arg.split("\\n")) {
                    if (!str.toLowerCase().contains("import")) {
                        codeBuilder.append(str);
                    }
                }
                pyWriter.write(arg);
                pyWriter.close();

                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("python", "tmp.py");
                Process process = processBuilder.start();
                StringBuilder output = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                int exitVal = process.waitFor();
                if (exitVal == 0) {
                    return output.toString();
                } else {
                    return "The command errored \n" + output;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "an error occurred";
            }
        }

        return "Invalid input";
    }
}
