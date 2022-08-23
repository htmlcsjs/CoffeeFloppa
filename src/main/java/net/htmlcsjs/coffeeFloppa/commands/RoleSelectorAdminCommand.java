package net.htmlcsjs.coffeeFloppa.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.discordjson.json.EmojiData;
import net.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import net.htmlcsjs.coffeeFloppa.handlers.ReactionHandler;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import net.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class RoleSelectorAdminCommand implements ICommand {
    @Override
    public @NotNull String getName() {
        return "roleSelector";
    }

    @Override
    public @Nullable String execute(Message message) {
        if (!CommandUtil.getAllowedToRun(message)) {
            return "ADMIN ONLY";
        }
        String[] splitArg = message.getContent().split(" ");
        String arg = String.join(" ", Arrays.copyOfRange(splitArg, 2, splitArg.length));
        switch (splitArg[1]) {
            case "new" -> {
                Guild guild = message.getGuild().block();
                if (guild != null) {
                    StringBuilder messageBuilder = new StringBuilder("React with:\n");
                    for (int i = 2; i < splitArg.length; i++) {
                        String[] emoteStrSplit = splitArg[i].split("#");
                        Role role = guild.getRoleById(Snowflake.of(emoteStrSplit[0])).block();
                        if (role == null) {
                            messageBuilder.append(String.format("<:howdidwegethere:%s> for role %s\n", emoteStrSplit[1], emoteStrSplit[0]));
                        } else {
                            messageBuilder.append(String.format("<:howdidwegethere:%s> for role %s\n", emoteStrSplit[1], role.getMention()));
                        }
                    }
                    Message reactionMessage = message.getChannel().flatMap(messageChannel -> messageChannel.createMessage(EmbedCreateSpec.builder()
                            .description(messageBuilder.toString())
                            .build())).block();
                    if (reactionMessage == null) {
                        return "ERROR: message is null";
                    }
                    StringBuilder configDataBuilder = new StringBuilder();
                    configDataBuilder.append(reactionMessage.getId().asString())
                            .append("@")
                            .append(reactionMessage.getChannelId().asString())
                            .append("@")
                            .append(guild.getId().asString())
                            .append(";");

                    for (int i = 2; i < splitArg.length; i++) {
                        String[] emoteStrSplit = splitArg[i].split("#");
                        EmojiData emojiData = CoffeeFloppa.client.getGuildEmojiById(guild.getId(), Snowflake.of(emoteStrSplit[1])).getData().block();
                        if (emojiData != null) {
                            reactionMessage.addReaction(ReactionEmoji.of(emojiData)).subscribe();
                        }
                    }
                    configDataBuilder.append(arg.replace(" ", ";"));
                    FloppaTomlConfig.roleSelectors.add(configDataBuilder.toString());
                    CoffeeFloppa.refreshData();
                    return null;
                } else {
                    return "ERROR: guild is null";
                }
            }
            case "add" -> {
                if (message.getMessageReference().isEmpty() || message.getMessageReference().get().getMessageId().isEmpty()) {
                    return "ERROR: Not referncing a message";
                }
                MessageChannel channel = message.getChannel().block();
                if (channel == null) {
                    return "ERROR: Channel is null";
                }
                Message refMessage = channel.getMessageById(message.getMessageReference().get().getMessageId().get()).block();
                if (refMessage == null) {
                    return "ERROR: Referenced message is null";
                }
                Guild guild = message.getGuild().block();
                if (guild == null) {
                    return "ERROR: Guild is null";
                }

                String refIdentifier = String.format("%s@%s", refMessage.getId().asString(), refMessage.getChannelId().asString());
                if (!ReactionHandler.getRoleSelectors().containsKey(refIdentifier) || refMessage.getAuthor().isEmpty() || !refMessage.getAuthor().get().getId().equals(CoffeeFloppa.self.getId())) {
                    return "Referenced message isn't a role selector";
                }
                refMessage.getEmbeds().get(0).getDescription().ifPresent(desc -> {
                    String[] emoteStrSplit = splitArg[2].split("#");
                    Role role = guild.getRoleById(Snowflake.of(emoteStrSplit[0])).block();
                    String appendToDesc;
                    if (role == null) {
                        appendToDesc = String.format("<:howdidwegethere:%s> for role %s\n", emoteStrSplit[1], emoteStrSplit[0]);
                    } else {
                        appendToDesc = String.format("<:howdidwegethere:%s> for role %s\n", emoteStrSplit[1], role.getMention());
                    }
                    refMessage.edit(MessageEditSpec.builder()
                            .addEmbed(EmbedCreateSpec.builder()
                                    .description(desc+"\n"+appendToDesc).build()
                            ).build()).subscribe();
                    int index = -1;
                    for (int i = 0; i < FloppaTomlConfig.roleSelectors.size(); i++) {
                        if (FloppaTomlConfig.roleSelectors.get(i).startsWith(refIdentifier)) {
                            index = i;
                            break;
                        }
                    }
                    FloppaTomlConfig.roleSelectors.set(index,String.format("%s;%s", FloppaTomlConfig.roleSelectors.get(index), splitArg[2]));
                    EmojiData emojiData = CoffeeFloppa.client.getGuildEmojiById(guild.getId(), Snowflake.of(emoteStrSplit[1])).getData().block();
                    if (emojiData != null) {
                        refMessage.addReaction(ReactionEmoji.of(emojiData)).subscribe();
                    }
                    CoffeeFloppa.refreshData();
                });

                return null;
            }
            default -> {
                return "Unrecognised verb";
            }
        }
    }
}
