package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.core.object.entity.Message;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import xyz.htmlcsjs.coffeeFloppa.FloppaLogger;
import xyz.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import xyz.htmlcsjs.coffeeFloppa.toml.TomlAnnotationProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RefreshCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "refreshConfig";
    }

    @Nullable
    @Override
    public String execute(Message message) {
        if (CommandUtil.getAllowedToRun(message)) {
            FloppaLogger.logger.info("Starting manually initiated config sync");
            TomlAnnotationProcessor.loadConfigs();
            TomlAnnotationProcessor.saveConfigs();
            CoffeeFloppa.refreshData();
            return "Config Successfully updated";
        } else {
            return "Config unable to be updated";
        }
    }

    @Override
    public String helpInfo() {
        return "Mod only command, DO NOT USE";
    }
}
