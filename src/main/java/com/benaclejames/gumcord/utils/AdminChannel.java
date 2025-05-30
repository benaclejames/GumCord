package com.benaclejames.gumcord.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.*;
import java.time.Duration;

/**
 * @deprecated In favor of ephemeral messages
 */
@Deprecated(forRemoval = true)
public class AdminChannel {
    private final Guild owningGuild;
    public final MessageChannel channelLiteral;

    public AdminChannel(Guild guild, MessageChannel channel) {
        this.owningGuild = guild;
        this.channelLiteral = channel;
    }

    public void announce(String title, String message) {
        if (channelLiteral == null) return;

        if (!owningGuild.getSelfMember().getPermissions((GuildChannel) channelLiteral).contains(Permission.MESSAGE_EMBED_LINKS)) {
            // We're missing embed perms, fallback to this.
            channelLiteral.sendMessage("**Admin Notification**\n"+title+"\n"+message+"\n\n>This is an embed fallback. Please enable send embed permissions for GumCord in this channel").queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(0xfe7134));
        builder.setDescription("Admin Notification");
        builder.addField(title, message, true);
        builder.setFooter("GumCord");

        // Send as embed
        channelLiteral.sendMessageEmbeds(builder.build()).delay(Duration.ofSeconds(5*60)).flatMap(Message::delete).queue();
    }
}
