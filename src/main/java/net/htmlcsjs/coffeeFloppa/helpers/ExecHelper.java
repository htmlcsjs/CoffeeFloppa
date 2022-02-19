package net.htmlcsjs.coffeeFloppa.helpers;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateFields;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class ExecHelper {

    private static List<String> illegalText;
    private static List<String> prependStatements;

    public static void initTextProcessingLists() {
        JSONObject jsonData = CoffeeFloppa.getJsonData();

        illegalText = (List<String>) jsonData.getOrDefault("eval_illegal_text", Arrays.asList("import",
                "__builtins__", "eval", "exec", "from"));
        prependStatements = (List<String>) jsonData.getOrDefault("eval_prepend_statements", Arrays.asList(
                "import math", "import numpy as np", "import pandas as pd", "import matplotlib.pyplot as plt",
                "import copy", "import random"));
    }

    public static String execString(String string, Message message) {
        try {
            File pyFile = new File("tmp.py");
            pyFile.delete();
            FileWriter pyWriter = new FileWriter("tmp.py");
            pyWriter.write(processCode(string));
            pyWriter.close();

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("timeout", "10", "python", "../tmp.py");
            File pwd = new File("evalRun/");
            pwd.mkdirs();
            processBuilder.directory(pwd);
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
            errorOut.append("\n```");
            output.append("\n```");

            return handleOutput(process, message, output.toString(), errorOut.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "an error occurred";
        }
    }

    private static String handleOutput(Process process, Message message, String output, String errorOut) throws InterruptedException {
        int exitVal = process.waitFor();
        if (exitVal == 0) {
            List<MessageCreateFields.File> attachedFiles = new ArrayList<>();
            for (String str: output.split("\n")) {
                if (!(str.equals("")) && str.charAt(0) == '$') {
                    String action = str.toLowerCase().split(" ")[0].replace("$", " ").strip();
                    if (action.equals("attach")) {
                        try {
                            String[] fileNameSplit = str.split(" ");
                            String fileName = String.join(" ", Arrays.copyOfRange(fileNameSplit, 1, fileNameSplit.length));
                            if (!fileName.contains("..") && !(fileName.charAt(0) == '/')) {
                                InputStream fileReader = new FileInputStream("evalRun/" + fileName);
                                attachedFiles.add(MessageCreateFields.File.of(fileName, fileReader));
                            }
                        } catch (FileNotFoundException e) {
                            message.getChannel().flatMap(channel -> channel.createMessage("Couldnt fine file with the name referanced\n```java\n" + e.getMessage() + "```"));
                        }
                    }
                }
            }

            // Output handling
            if (!attachedFiles.isEmpty()) {
                if (output.length() < 2000) {
                    message.getChannel().flatMap(channel -> channel.createMessage(output.toString())
                            .withMessageReference(message.getId())
                            .withFiles(attachedFiles)).subscribe();
                } else {
                    message.getChannel().flatMap(channel -> {
                        ByteArrayInputStream outputStream = new ByteArrayInputStream(output.toString().getBytes(StandardCharsets.UTF_8));
                        attachedFiles.add(MessageCreateFields.File.of("msg.txt", outputStream));
                        return channel.createMessage("Message content too large for msg, falling to an attachment")
                                .withFiles(attachedFiles)
                                .withMessageReference(message.getId());
                    }).subscribe();
                }
                return null;
            } else {
                return output;
            }
        } else if (exitVal == 124) {
            return "Floppa doesn't have time for your bullshittery";
        } else {
            return "The command errored \n" + errorOut;
        }
    }

    private static String processCode(String code) {
        for (String statement: illegalText) {
            code = code.replace(statement, "#");
        }

        StringBuilder codeBuilder = new StringBuilder(code);
        for (String statement: prependStatements) {
            codeBuilder.insert(0, statement + "\n");
        }
        code = codeBuilder.toString();
        return code;
    }
}
