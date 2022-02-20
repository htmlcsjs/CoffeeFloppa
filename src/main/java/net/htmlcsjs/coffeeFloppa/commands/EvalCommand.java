package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import net.htmlcsjs.coffeeFloppa.helpers.ExecHelper;
import org.json.simple.JSONObject;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            String code = "";
            if (message.getAttachments().size() > 0) {
                String data;
                try {
                    URL url = new URL(message.getAttachments().get(0).getUrl());
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream("dirty.py");
                    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    fileOutputStream.close();

                    data = Files.readString(Path.of("dirty.py"));
                } catch (Exception e) {
                    e.printStackTrace();
                    return "an error occurred";
                }

                code = data.replace("`", "");
            } else {
                code = arg.replace("`", "");
            }

            return ExecHelper.execString(code, message);
        } else if (verb.equalsIgnoreCase("add_prepend")) {
            if (!CommandUtil.getAllowedToRun(message)) {
                return "You dont have the required perms to execute this";
            }
            List<String> prependStatements = ExecHelper.getPrependStatements();
            prependStatements.add(arg);
            JSONObject jsonData = CoffeeFloppa.getJsonData();
            jsonData.put("eval_prepend_statements", prependStatements);
            CoffeeFloppa.updateConfigFile(jsonData);

            return String.format("Added `%s` to prepend statments", arg);
        } else if (verb.equalsIgnoreCase("add_illegal")) {
            if (!CommandUtil.getAllowedToRun(message)) {
                return "You dont have the required perms to execute this";
            }
            List<String> illegalText = ExecHelper.getIllegalText();
            illegalText.add(arg);
            JSONObject jsonData = CoffeeFloppa.getJsonData();
            jsonData.put("eval_illegal_text", illegalText);
            CoffeeFloppa.updateConfigFile(jsonData);

            return String.format("Added `%s` to illegal texts", arg);
        } else if (verb.equalsIgnoreCase("del_prepend")) {
            if (!CommandUtil.getAllowedToRun(message)) {
                return "You dont have the required perms to execute this";
            }
            List<String> prependStatements = new ArrayList<>();
            boolean deleted = false;
            for (String statement: ExecHelper.getPrependStatements()) {
                if (!statement.equalsIgnoreCase(arg))
                    prependStatements.add(statement);
                else
                    deleted = true;
            }
            JSONObject jsonData = CoffeeFloppa.getJsonData();
            jsonData.put("eval_prepend_statements", prependStatements);
            CoffeeFloppa.updateConfigFile(jsonData);

            return deleted ? String.format("Deleted `%s` from prepend statements", arg) :
                    String.format("Couldnt delete `%s` from prepend statements", arg) ;
        } else if (verb.equalsIgnoreCase("del_illegal")) {
            if (!CommandUtil.getAllowedToRun(message)) {
                return "You dont have the required perms to execute this";
            }
            List<String> illegalText = new ArrayList<>();
            boolean deleted = false;
            for (String statement : ExecHelper.getIllegalText()) {
                if (!statement.equalsIgnoreCase(arg))
                    illegalText.add(statement);
                else
                    deleted = true;
            }
            JSONObject jsonData = CoffeeFloppa.getJsonData();
            jsonData.put("eval_illegal_text", illegalText);
            CoffeeFloppa.updateConfigFile(jsonData);

            return deleted ? String.format("Deleted `%s` from illegal texts", arg) :
                    String.format("Couldn't delete `%s` from illegal texts", arg);
        } else if (verb.equalsIgnoreCase("list_info")) {
            List<String> illegalText = ExecHelper.getIllegalText();
            List<String> prependStatements = ExecHelper.getPrependStatements();
            return String.format("Prepend statements :\n```\n%s```\n Illegal Statements: `%s`",
                    String.join("\n", prependStatements), String.join(", ", illegalText));
        } else if (verb.equalsIgnoreCase("help")) {
            return """
                    This command can be used to run python code with the verb `run`, with more coming soon
                    **run:**
                     - you can attach attach a python file to run or provide it after the verb inside or out of `codeblocks`
                     - print out "$attach" followed by the file name to attach any files generated by your code
                     - imports and attaching files from outside of the run dir is disallowed
                    **list_info:**
                     - lists any imports and any illegal statements like `eval` and `import`
                     """;
        }
        return "Invalid input, use $eval run to execute code";
    }
}
