package com.benaclejames.gumcord.Utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.MissingAccessException;

import java.awt.*;

public class AdminChannel {
    private final MessageChannel channel;

    public AdminChannel(MessageChannel channel) {
        this.channel = channel;
    }

    public void Announce(String title, String message) {
        if (channel == null) return;

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(0xfe7134));
        builder.setDescription("Admin Notification");
        builder.addField(title, message, false);
        builder.setFooter("GumCord");

        try {   // Try to send as embed
            channel.sendMessage(builder.build()).queue();
        } catch (MissingAccessException e) {    // We don't have perms to send embed, send normally and notify of embed perms missing
            channel.sendMessage("**Admin Notification**\n"+title+"\n"+message+"\n\n>This is an embed fallback. Please enable send embed permissions for GumCord in this channel").queue();
        }
    }
}
