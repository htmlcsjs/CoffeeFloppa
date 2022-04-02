package net.htmlcsjs.coffeeFloppa.helpers.lua;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.*;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.Optional;
import java.util.stream.Collectors;

public class FloppaLuaLib extends TwoArgFunction {
    private final Message message;
    private final Guild guild;

    public FloppaLuaLib(Message message) {
        this.message = message;
        this.guild = message.getGuild().block();
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue floppaLib = tableOf();
        floppaLib.set("susso", new Susso());
        floppaLib.set("msg", new getMessage().call(LuaValue.valueOf(message.getId().asString())));
        floppaLib.set("get_message", new getMessage());
        floppaLib.set("get_channel", new getChannel());

        env.set("floppa", floppaLib);
        env.get("package").get("loaded").set("floppa", floppaLib);
        return floppaLib;
    }

    static public class Susso extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf("I AM SUS, THIS IS FROM JAVA");
        }
    }

    public class getMessage extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue messageID, LuaValue channel) {
            if (guild == null) {
                FloppaLogger.logger.error("ERROR, Guild is null\n" + message);
                return error("The referenced guild is null\nThis might be a problem on the bots side");
            }
            LuaValue channelID;
            try {
                channelID = channel.get("id");
            } catch (Exception ignored) {
                channelID = LuaValue.NIL;
            }

            Channel channelJava = guild.getChannelById(Snowflake.of(channelID.isnil() ? message.getChannelId().asString() : channelID.checkjstring())).block();
            MessageChannel msgChannel = (MessageChannel) channelJava;
            Message messageCurrent;
            if (msgChannel != null) {
                messageCurrent = msgChannel.getMessageById(Snowflake.of(messageID.isnil() ? message.getId().asString() : messageID.checkjstring())).block();
            } else {
                return error("failed to find channel from id " + channelID);
            }

            if (messageCurrent == null) {
                return error("failed to find message from id " + messageID);
            }

            LuaValue messageData = tableOf();
            messageCurrent.getAuthor().ifPresent(author -> messageData.set("author", author.getId().asString()));
            messageData.set("id", messageCurrent.getId().asString());
            messageData.set("content", messageCurrent.getContent());
            messageData.set("attachments", LuaHelper.getLuaValueFromList(messageCurrent.getAttachments().stream().map(Attachment::getUrl).collect(Collectors.toList())));
            return messageData;
        }
    }

    public class getChannel extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue channelID) {
            try {
                if (guild == null) {
                    FloppaLogger.logger.error("ERROR, Guild is null\n" + message);
                    return error("The referenced guild is null\nThis might be a problem on the bots side");
                }
                Channel channelJava = guild.getChannelById(Snowflake.of(channelID.isnil() ? message.getChannelId().asString() : channelID.checkjstring())).block();
                if (channelJava == null){
                    return error("failed to find channel from id " + channelID);
                }
                LuaValue channelData = tableOf();

                channelData.set("id", channelJava.getId().asString());
                channelData.set("type", channelJava.getType().name());
                channelData.set("mention", channelJava.getMention());
                switch (channelJava.getType()) { // missing DM,GROUP_DM,GUILD_CATEGORY,GUILD_NEWS,GUILD_STORE
                    case GUILD_TEXT -> {
                        TextChannel textChannel = (TextChannel) channelJava;
                        channelData.set("rate_limit", textChannel.getRateLimitPerUser());
                        channelData.set("is_nsfw", LuaValue.valueOf(textChannel.isNsfw()));

                        Optional<Snowflake> lastMessageId = textChannel.getLastMessageId();
                        lastMessageId.ifPresent(snowflake -> channelData.set("last_message", snowflake.asString()));
                        Optional<String> topic = textChannel.getTopic();
                        topic.ifPresent(str -> channelData.set("topic", str));
                        Category category = textChannel.getCategory().block();
                        if (category != null)
                            channelData.set("category", category.getName());
                        channelData.set("name", textChannel.getName());
                    }
                    case GUILD_VOICE, GUILD_STAGE_VOICE -> {
                        VoiceChannel vc = (VoiceChannel) channelJava;
                        channelData.set("bitrate", vc.getBitrate());
                        channelData.set("user_limit", vc.getUserLimit());
                        channelData.set("video_quality_mode", vc.getVideoQualityMode().name());

                        Optional<Snowflake> lastMessageId = vc.getLastMessageId();
                        lastMessageId.ifPresent(snowflake -> channelData.set("last_message", snowflake.asString()));
                        Optional<String> topic = vc.getTopic();
                        topic.ifPresent(str -> channelData.set("topic", str));
                        Category category = vc.getCategory().block();
                        if (category != null)
                            channelData.set("category", category.getName());
                        channelData.set("name", vc.getName());
                    }
                    default -> {}
                }
                return channelData;
            } catch (Exception e) {
                return error(e.getMessage());
            }
        }
    }
}
