package net.htmlcsjs.coffeeFloppa.helpers.lua;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.*;
import discord4j.core.spec.MessageCreateFields;
import discord4j.rest.util.Color;
import discord4j.rest.util.Image;
import net.htmlcsjs.coffeeFloppa.FloppaLogger;
import net.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

public class FloppaLuaLib extends TwoArgFunction {
    private final Message message;
    private final Guild guild;
    private int msgCount;
    public static final int MAX_SENT_MESSAGES = 5;

    public FloppaLuaLib(Message message) {
        this.message = message;
        this.guild = message.getGuild().block();
        this.msgCount = 0;
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue floppaLib = tableOf();
        floppaLib.set("susso", new Susso());
        floppaLib.set("msg", new getMessage().call(LuaValue.valueOf(message.getId().asString())));
        floppaLib.set("get_message", new getMessage());
        floppaLib.set("get_channel", new getChannel());
        floppaLib.set("send_file", new sendFile());
        floppaLib.set("get_user", new getUser());

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
            messageData.set("contents", messageCurrent.getContent());
            messageData.set("channel", messageCurrent.getChannelId().asString());
            List<String> attachmentList = messageCurrent.getAttachments().stream().map(Attachment::getUrl).toList();
            if (!attachmentList.isEmpty()) {
                messageData.set("attachments", LuaHelper.getLuaValueFromList(attachmentList));
            }
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
                        /*List<Message> messagesBefore = textChannel.getMessagesBefore(message.getId()).collectList().block();
                        if (messagesBefore != null) {
                            List<String> messageIds = messagesBefore.stream().map(msg -> msg.getId().asString()).toList();
                            if (!messageIds.isEmpty()) {
                                channelData.set("last_messages", LuaHelper.getLuaValueFromList(messageIds.subList(0, Math.min(messageIds.size(), 20))));
                            }
                        } laggy as fuck, crashes on my pi*/

                        Message lastMsg = textChannel.getLastMessage().block();
                        if (lastMsg != null) {
                            channelData.set("last_message", lastMsg.getId().asString());
                        }
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
                        channelData.set("region", vc.getRtcRegion().getValue());

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

    public class sendFile extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue filename, LuaValue buffer) {
            if (!buffer.isuserdata() || !filename.isstring()) {
                return error("Args are of invalid type. should be (string, userdata)");
            }
            if (msgCount > MAX_SENT_MESSAGES) {
                return error("You have sent too many messages");
            }
            msgCount++;
            byte[] userdata = (byte[]) buffer.checkuserdata();
            message.getChannel().flatMap(channel -> channel.createMessage()
                    .withFiles(MessageCreateFields.File.of(filename.checkjstring(), new ByteArrayInputStream(userdata))))
                    .subscribe();
            return NIL;
        }
    }

    public class getUser extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue userId) {
            try {
                if (guild == null) {
                    FloppaLogger.logger.error("ERROR, Guild is null\n" + message);
                    return error("The referenced guild is null\nThis might be a problem on the bots side");
                }
                Snowflake userSnowflake = Snowflake.of(userId.checkjstring());
                Member member = guild.getMemberById(userSnowflake).block();
                if (member == null) {
                    return error("Couldn't find the member with the specified id.");
                }
                LuaValue memberData = tableOf();

                member.getAccentColor().ifPresent(colour -> memberData.set("accent_colour", CommandUtil.getHexValueFromColour(colour)));
                memberData.set("animated_avatar", valueOf(member.hasAnimatedAvatar()));
                memberData.set("animated_banner", valueOf(member.hasAnimatedBanner()));
                memberData.set("animated_guild_avatar", valueOf(member.hasAnimatedGuildAvatar()));
                memberData.set("avatar_url", member.getAvatarUrl());
                member.getBannerUrl().ifPresent(url -> memberData.set("banner_url", url));
                memberData.set("bot", valueOf(member.isBot()));
                Color colour = member.getColor().block();
                if (colour != null) {
                    memberData.set("colour", CommandUtil.getHexValueFromColour(colour));
                }
                memberData.set("discriminator", member.getDiscriminator());
                memberData.set("display_name", member.getDisplayName());
                memberData.set("default_avatar_url", member.getDefaultAvatarUrl());
                List<String> flags = member.getPublicFlags().stream().map(Enum::name).toList();
                memberData.set("flags", LuaHelper.getLuaValueFromList(flags));
                member.getGuildAvatarUrl(member.hasAnimatedGuildAvatar() ? Image.Format.GIF : Image.Format.PNG).ifPresent(url -> memberData.set("guild_avatar", url));
                member.getNickname().ifPresent(nick -> memberData.set("nick", nick));
                List<String> roleIds = member.getRoleIds().stream().map(Snowflake::asString).toList();
                memberData.set("roles", LuaHelper.getLuaValueFromList(roleIds));
                memberData.set("tag", member.getTag());
                memberData.set("username", member.getUsername());

                return memberData;
            } catch (Exception e) {
                return error(e.getMessage());
            }
        }
    }

}
