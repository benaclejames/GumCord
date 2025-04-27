package com.benaclejames.gumcord.utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class ErrorEmbed extends EmbedBuilder {
    public ErrorEmbed(String errorMsg, String additionalInfo) {
        setColor(new Color(0xdf2b0c));
        addField("Error", errorMsg, false);
        setFooter("GumCord");

        if (additionalInfo != null)
            addField("Additional Info", additionalInfo, false);
    }

    @Override
    public String toString() {
        return "Error! \n"+getFields().get(0).getValue()+"\n\n>Please enable message embed permissions for GumCord";
    }
}
