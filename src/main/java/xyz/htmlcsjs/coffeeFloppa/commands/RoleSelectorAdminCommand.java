package xyz.htmlcsjs.coffeeFloppa.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.discordjson.json.EmojiData;
import xyz.htmlcsjs.coffeeFloppa.CoffeeFloppa;
import xyz.htmlcsjs.coffeeFloppa.handlers.ReactionHandler;
import xyz.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import xyz.htmlcsjs.coffeeFloppa.toml.FloppaTomlConfig;
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
        try {
            switch (splitArg[1]) {
                case "new" -> {
                    String arg = String.join(" ", Arrays.copyOfRange(splitArg, 2, splitArg.length));
                    Guild guild = message.getGuild().block();
                    if (guild != null) {
                        StringBuilder messageBuilder = new StringBuilder("React with:\n");
                        for (int i = 2; i < splitArg.length; i++) {
                            String[] emoteStrSplit = splitArg[i].replace("\\", "").split("#");
                            Role role = guild.getRoleById(Snowflake.of(emoteStrSplit[0])).block();
                            if (emoteStrSplit[1].matches("[\\d\\s]+")) {
                                if (role == null) {
                                    messageBuilder.append(String.format("<:howdidwegethere:%s> for role %s\n", emoteStrSplit[1], emoteStrSplit[0]));
                                } else {
                                    messageBuilder.append(String.format("<:howdidwegethere:%s> for role %s\n", emoteStrSplit[1], role.getMention()));
                                }
                            } else {
                                if (role == null) {
                                    messageBuilder.append(String.format("%s for role %s\n", emoteStrSplit[1], emoteStrSplit[0]));
                                } else {
                                    messageBuilder.append(String.format("%s for role %s\n", emoteStrSplit[1], role.getMention()));
                                }
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
                            String[] emoteStrSplit = splitArg[i].replace("\\", "").split("#");
                            if (emoteStrSplit[1].matches("\\d+")) {
                                 EmojiData emojiData = CoffeeFloppa.client.getGuildEmojiById(guild.getId(), Snowflake.of(emoteStrSplit[1])).getData().block();
                                if (emojiData != null) {
                                    reactionMessage.addReaction(ReactionEmoji.of(emojiData)).subscribe();
                                }
                            } else {
                                reactionMessage.addReaction(ReactionEmoji.unicode(emoteStrSplit[1])).subscribe();
                            }

                        }
                        configDataBuilder.append(arg.replace(" ", ";").replace("\\", ""));
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
                        String[] emoteStrSplit = splitArg[2].replace("\\", "").split("#");
                        Role role = guild.getRoleById(Snowflake.of(emoteStrSplit[0])).block();
                        String appendToDesc;
                        if (emoteStrSplit[1].matches("\\d+")) {
                            if (role == null) {
                                appendToDesc = String.format("<:howdidwegethere:%s> for role %s\n", emoteStrSplit[1], emoteStrSplit[0]);
                            } else {
                                appendToDesc = String.format("<:howdidwegethere:%s> for role %s\n", emoteStrSplit[1], role.getMention());
                            }
                        } else {
                            if (role == null) {
                                appendToDesc = String.format("%s for role %s\n", emoteStrSplit[1], emoteStrSplit[0]);
                            } else {
                                appendToDesc = String.format("%s for role %s\n", emoteStrSplit[1], role.getMention());
                            }
                        }
                        refMessage.edit(MessageEditSpec.builder()
                                .addEmbed(EmbedCreateSpec.builder()
                                        .description(desc + "\n" + appendToDesc).build()
                                ).build()).subscribe();
                        int index = -1;
                        for (int i = 0; i < FloppaTomlConfig.roleSelectors.size(); i++) {
                            if (FloppaTomlConfig.roleSelectors.get(i).startsWith(refIdentifier)) {
                                index = i;
                                break;
                            }
                        }
                        FloppaTomlConfig.roleSelectors.set(index, String.format("%s;%s", FloppaTomlConfig.roleSelectors.get(index), splitArg[2].replace("\\", "")));
                        if (emoteStrSplit[1].matches("\\d+")) {
                            EmojiData emojiData = CoffeeFloppa.client.getGuildEmojiById(guild.getId(), Snowflake.of(emoteStrSplit[1])).getData().block();
                            if (emojiData != null) {
                                refMessage.addReaction(ReactionEmoji.of(emojiData)).subscribe();
                            }
                        } else {
                            refMessage.addReaction(ReactionEmoji.unicode(emoteStrSplit[1])).subscribe();
                        }
                        CoffeeFloppa.refreshData();
                    });

                    return null;
                }
                case "del" -> {
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
                    int index = -1;
                    for (int i = 0; i < FloppaTomlConfig.roleSelectors.size(); i++) {
                        if (FloppaTomlConfig.roleSelectors.get(i).startsWith(refIdentifier)) {
                            index = i;
                            break;
                        }
                    }
                    if (index < 0) {
                        return "Failed to find the index of that selector";
                    }
                    refMessage.delete("mogus").subscribe();
                    String returnStr = "Deleted role selector `" + FloppaTomlConfig.roleSelectors.get(index) + "`";
                    FloppaTomlConfig.roleSelectors.remove(index);
                    CoffeeFloppa.refreshData();
                    return returnStr;
                }
            }
        } catch (IndexOutOfBoundsException ignored) {}
        return """
                    Unrecognised verb; Verbs are:
                    - `new` - adds a new role selector, format `new role1id#emote1id role2id#emote2id ..`
                    - `add` - adds a new role to a selector. Run this command replying to a role selector with the format `add roleid#emoteid`
                    - `del` - deletes a role selector. Run this command replying to a role selector, with no args
                """;
    }
}
