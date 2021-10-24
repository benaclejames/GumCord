package com.benaclejames.gumcord.Utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;

public class AdminChannel {
    private final Guild owningGuild;
    private final MessageChannel channel;

    public AdminChannel(Guild guild, MessageChannel channel) {
        this.owningGuild = guild;
        this.channel = channel;
    }

    public void Announce(String title, String message) {
        if (channel == null) return;

        if (!owningGuild.getSelfMember().getPermissions((GuildChannel) channel).contains(Permission.MESSAGE_EMBED_LINKS)) {
            // We're missing embed perms, fallback to this.
            channel.sendMessage("**Admin Notification**\n"+title+"\n"+message+"\n\n>This is an embed fallback. Please enable send embed permissions for GumCord in this channel").queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(0xfe7134));
        builder.setDescription("Admin Notification");
        builder.addField(title, message, true);
        builder.setFooter("GumCord");

        // Send as embed
        channel.sendMessage(builder.build()).queue();
    }
}
