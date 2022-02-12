package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
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
                codeBuilder.append("import math\nimport numpy as np\nimport pandas as pd\nimport matplotlib.pyplot as plt\nimport copy\n");
                if (message.getAttachments().size() > 0) {
                    URL url = new URL(message.getAttachments().get(0).getUrl());
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream("dirty.py");
                    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    fileOutputStream.close();

                    String data = Files.readString(Path.of("dirty.py"));

                    for (String strRaw : data.split("\n")) {
                        String str = strRaw.replaceAll("`", "");
                        if (!str.toLowerCase().contains("import")) {
                            codeBuilder.append(str).append("\n");
                        }
                    }
                } else {
                    for (String strRaw : arg.split("\n")) {
                        String str = strRaw.replaceAll("`", "");
                        if (!str.toLowerCase().contains("import")) {
                            codeBuilder.append(str).append("\n");
                        }
                    }
                }
                pyWriter.write(codeBuilder.toString());
                pyWriter.close();

                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("python", "tmp.py");
                Process process = processBuilder.start();
                StringBuilder output = new StringBuilder();
                output.append("```py\n");
                StringBuilder errorOut = new StringBuilder();
                errorOut.append("```py\n");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                while ((line = errorReader.readLine()) != null) {
                    errorOut.append(line).append("\n");
                }
                errorOut.append("```");
                output.append("```");

                int exitVal = process.waitFor();
                if (exitVal == 0) {
                    return output.toString();
                } else {
                    return "The command errored \n" + errorOut;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "an error occurred";
            }
        }

        return "Invalid input";
    }
}
