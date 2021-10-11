package com.benaclejames.gumcord.Utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

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

        channel.sendMessage(builder.build()).queue();
    }
}
