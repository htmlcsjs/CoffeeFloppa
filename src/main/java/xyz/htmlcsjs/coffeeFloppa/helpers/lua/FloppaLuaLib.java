package xyz.htmlcsjs.coffeeFloppa.helpers.lua;

import discord4j.common.util.Snowflake;
import discord4j.core.object.RoleTags;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.*;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateMono;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import discord4j.rest.util.Image;
import xyz.htmlcsjs.coffeeFloppa.FloppaLogger;
import xyz.htmlcsjs.coffeeFloppa.handlers.MessageHandler;
import xyz.htmlcsjs.coffeeFloppa.helpers.CommandUtil;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
        floppaLib.set("get_user", new getUser());
        floppaLib.set("get_role", new getRole());
        floppaLib.set("send_message", new sendMessage());

        // handles some other stuff added to other packages
        // mega cursed
        env.get("package").get("loaded").get("table").set("to_string", new tableToString());

        env.set("floppa", floppaLib);
        env.get("package").get("loaded").set("floppa", floppaLib);
        return floppaLib;
    }

    public int getMsgCount() {
        return msgCount;
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
            messageCurrent.getAuthor().ifPresent(author -> messageData.set("author", new getUser().call(valueOf(author.getId().asString()))));
            messageData.set("id", messageCurrent.getId().asString());
            messageData.set("contents", messageCurrent.getContent());
            messageData.set("channel", new getChannel().call(messageCurrent.getChannelId().asString()));
            messageData.set("flags", LuaHelper.getLuaValueFromList(messageCurrent.getFlags().stream().map(Enum::name).toList()));
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

    public class getRole extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            try {
                if (guild == null) {
                    FloppaLogger.logger.error("ERROR, Guild is null\n" + message);
                    return error("The referenced guild is null\nThis might be a problem on the bots side");
                }
                Snowflake roleSnowflake = Snowflake.of(arg.tojstring());
                Role role = guild.getRoleById(roleSnowflake).block();
                if (role == null) {
                    return error("Couldn't find a role with the id " + arg.tojstring());
                }
                LuaValue roleData = tableOf();

                roleData.set("name", role.getName());
                roleData.set("id", role.getId().asString());
                roleData.set("colour", CommandUtil.getHexValueFromColour(role.getColor()));
                roleData.set("permissions", LuaHelper.getLuaValueFromList(role.getPermissions().asEnumSet().stream().map(Enum::name).toList()));
                if (role.getTags().isPresent()) {
                    RoleTags roleTags = role.getTags().get();
                    roleData.set("nitro_role", valueOf(roleTags.isPremiumSubscriberRole()));
                    roleTags.getBotId().ifPresent(botId -> roleData.set("bot_id", botId.asString()));
                    roleTags.getIntegrationId().ifPresent(id -> roleData.set("integration_id", id.asString()));
                } else {
                    roleData.set("nitro_role", FALSE);
                }
                roleData.set("is_everyone", valueOf(role.isEveryone()));
                roleData.set("is_separated", valueOf(role.isHoisted()));
                roleData.set("is_mentionable", valueOf(role.isMentionable()));

                return roleData;
            } catch (Exception e) {
                return error(e.getMessage());
            }
        }
    }

    public static class tableToString extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue table) {
            if (!table.istable()) {
                return error("argument isn't a table.");
            }
            return valueOf(LuaHelper.startLuaTableToStr(table.checktable()));
        }
    }

    public class sendMessage extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue messageData) {
            if (msgCount >= MAX_SENT_MESSAGES) {
                return FALSE;
            }
            msgCount++;
            if (messageData.isstring()) {
                if (!MessageHandler.sendMessage(message, messageData.checkjstring(), msgCount == 1)) {
                    return error("Internal error");
                }
                return TRUE;
            } else if (messageData.istable()) {
                LuaTable messageTable = messageData.checktable();
                message.getChannel().flatMap(channel -> {
                    MessageCreateMono msg = channel.createMessage(messageTable.get("content").optjstring(""))
                            .withAllowedMentions(AllowedMentions.suppressEveryone());
                    if (msgCount == 1) {
                        msg = msg.withMessageReference(message.getId());
                    }

                    LuaValue fileData = messageTable.get("file");
                    if (fileData != NIL && fileData.istable() && fileData.get("data") != NIL) {
                        LuaTable fileTable = fileData.checktable();
                        if (fileTable.get("name") == NIL || !fileTable.get("name").isstring()) {
                            fileTable.set("name", "unknown.txt");
                        }
                        LuaValue fileRaw = fileTable.get("data");
                        InputStream fileIStream;
                        if (fileRaw.isuserdata()) {
                            fileIStream = new ByteArrayInputStream((byte[]) fileRaw.checkuserdata());
                        } else {
                            fileIStream = new ByteArrayInputStream(fileRaw.checkjstring().getBytes(StandardCharsets.UTF_8));
                        }
                        msg = msg.withFiles(MessageCreateFields.File.of(fileTable.get("name").checkjstring(), fileIStream));
                    }

                    if (messageTable.get("embed") != null && messageTable.get("embed").istable()) {
                        LuaTable embedTable = messageTable.get("embed").checktable();
                        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();

                        LuaValue author = embedTable.get("author");
                        if (author != NIL) {
                            if (author.isstring())
                                embed.author(author.tojstring(), null, null);
                            else if (author.istable())
                                embed.author(author.get("name").tojstring(), author.get("url").tojstring(), author.get("icon_url").checkjstring());
                        }

                        LuaValue colour = embedTable.get("colour");
                        if (colour != NIL && colour.isint())
                            embed.color(Color.of(colour.checkint()));

                        LuaValue desc = embedTable.get("description");
                        if (desc != NIL && desc.isstring())
                            embed.description(desc.tojstring());

                        LuaValue image = embedTable.get("image");
                        if (image != NIL && image.isstring())
                            embed.image(image.tojstring());

                        LuaValue thumbnail = embedTable.get("thumbnail");
                        if (thumbnail != NIL && thumbnail.isstring())
                            embed.thumbnail(thumbnail.tojstring());

                        LuaValue title = embedTable.get("title");
                        if (title != NIL && title.isstring())
                            embed.title(title.tojstring());

                        LuaValue url = embedTable.get("url");
                        if (url != NIL && url.isstring())
                            embed.url(url.tojstring());

                        LuaValue timestamp = embedTable.get("timestamp");
                        if (timestamp != NIL && timestamp.islong())
                            embed.timestamp(Instant.ofEpochSecond(timestamp.checklong()));

                        LuaValue footer = embedTable.get("footer");
                        if (footer != NIL) {
                            if (footer.isstring())
                                embed.footer(footer.tojstring(), null);
                            else if (footer.istable())
                                embed.footer(footer.get("body").tojstring(), footer.get("icon_url").checkjstring());
                        }

                        LuaValue fields = embedTable.get("fields");
                        if (fields != NIL && fields.istable()) {
                            for (int i = 1; i < fields.length() + 1; i++) {
                                LuaValue field = fields.get(i);
                                if (field != NIL && field.istable()) {
                                    embed.addField(field.get("name").tojstring(), field.get("body").tojstring(), field.get("inline").optboolean(false));
                                }
                            }
                        }

                        msg = msg.withEmbeds(embed.build());
                    }

                    return msg;
                }).onErrorResume(e -> {
                    if (e.getClass() == ClientException.class) {
                        ClientException clientError = (ClientException) e;
                        if (clientError.getErrorResponse().isPresent() && (int) clientError.getErrorResponse().get().getFields().get("code") == 50006) {
                            MessageChannel channel = message.getChannel().block();
                            if (channel != null) {
                                return channel.createMessage(EmbedCreateSpec.builder()
                                        .footer("you dumbass, you cannot send a empty message", null)
                                        .image("https://i.imgur.com/V9R9uVS.gif")
                                        .build());
                            }
                        }
                    }
                    return Mono.empty();
                }).subscribe();
                return TRUE;
            }
            return error("Message string nor table with message information supplied");
        }
    }
}
