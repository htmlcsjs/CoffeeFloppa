package net.htmlcsjs.coffeeFloppa.helpers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateFields;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class ExecHelper {

    private static List<String> illegalText;
    private static List<String> prependStatements;
    private static final DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    private static final DockerHttpClient client = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

    public static void initTextProcessingLists() {
        JSONObject jsonData = CoffeeFloppa.getJsonData();

        illegalText = (List<String>) jsonData.getOrDefault("eval_illegal_text", Arrays.asList("import",
                "__builtins__", "eval", "exec"));
        prependStatements = (List<String>) jsonData.getOrDefault("eval_prepend_statements", Arrays.asList(
                "import math", "import numpy as np", "import pandas as pd", "import matplotlib.pyplot as plt",
                "import copy", "import random"));
    }

    public static String execString(String string, Message message) {
        try {
            File pwd = new File("evalRun/");
            pwd.mkdirs();
            File pyFile = new File("evalRun/tmp.py");
            pyFile.delete();
            FileWriter pyWriter = new FileWriter("evalRun/tmp.py");
            pyWriter.write(processCode(string));
            pyWriter.close();

            DockerClient dockerClient = DockerClientImpl.getInstance(config, client);
            CreateContainerResponse container = dockerClient.createContainerCmd("python:3.7")
                    .withBinds(new Bind(pwd.getAbsolutePath(), new Volume("/opt/floppa/")))
                    .withWorkingDir("/opt/floppa")
                    .withName("floppaExec")
                    .withCmd("python", "tmp.py", ">", "out.txt", "2>&1")
                    .withStopTimeout(10000)
                    .exec();
            dockerClient.startContainerCmd(container.getId()).exec();

            WaitContainerResultCallback callback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(container.getId()).exec(callback);
            int exitVal = callback.awaitStatusCode();
            dockerClient.removeContainerCmd(container.getId()).exec();

            StringBuilder output = new StringBuilder();
            output.append("```py\n");
            BufferedReader reader = new BufferedReader(new FileReader("evalRun/out.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line.replaceAll("[^A-Za-z0-9._~()\\[\\]{}\\n\\t'!*:,;+@?\\-/$ \\\\]", "█")).append("\n");
            }
            output.append("\n```");

            return handleOutput(exitVal, message, output.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "an error occurred";
        }
    }

    private static String handleOutput(int exitVal, Message message, String output) throws InterruptedException {
        if (exitVal == 0 || exitVal == 2) {
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
                            message.getChannel().flatMap(channel -> channel.createMessage("Couldnt find file with the name referanced\n```java\n" + e.getMessage() + "```"));
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
            return "The command errored \n" + output;
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

    public static List<String> getIllegalText() {
        return illegalText;
    }

    public static List<String> getPrependStatements() {
        return prependStatements;
    }
}
