package xyz.htmlcsjs.coffeeFloppa.handlers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import xyz.htmlcsjs.coffeeFloppa.FloppaLogger;

public class OtherHandler extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        CoffeeFloppa.self = CoffeeFloppa.client.getSelfUser();
        FloppaLogger.logger.info("Logged in as " + CoffeeFloppa.self.getAsTag());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        event.getMessage().getId();
    }
}
