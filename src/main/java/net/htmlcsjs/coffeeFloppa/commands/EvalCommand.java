package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateFields;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
                codeBuilder.append("import math\nimport numpy as np\nimport pandas as pd\nimport matplotlib.pyplot as plt\nimport copy\nimport random\neval = 'no'\nexec = 'no'\n");
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

                int exitVal = process.waitFor();
                if (exitVal == 0) {
                    List<MessageCreateFields.File> attachedFiles = new ArrayList<>();
                    for (String str: output.toString().split("\n")) {
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
                        if (output.toString().length() < 2000) {
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
                        return output.toString();
                    }
                } else if (exitVal == 124) {
                    return "The command timeouted";
                } else {
                    return "The command errored \n" + errorOut;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "an error occurred";
            }
        } else if (verb.equalsIgnoreCase("help")) {
            return """
                    This command can be used to run python code with the verb `run`, with more coming soon
                    **run:**
                     - you can attach attach a python file to run or provide it after the verb inside or out of `codeblocks`
                     - print out "$attach" followed by the file name to attach any files generated by your code
                     - imports and attaching files from outside of the run dir is disallowed
                     - default imports: math, numpy as np, pandas as pd, matplotlib.pyplot as plt, copy, random
                     """;
        }
        return "Invalid input";
    }
}
