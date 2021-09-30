package com.benaclejames.gumcord.Utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class ErrorEmbed extends EmbedBuilder {
    public ErrorEmbed(String errorMsg) {
        setColor(new Color(0xdf2b0c));
        addField("Error", errorMsg, false);
        setFooter("GumCord");
    }
}
