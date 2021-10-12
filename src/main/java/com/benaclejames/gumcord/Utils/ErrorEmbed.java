package com.benaclejames.gumcord.Utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class ErrorEmbed extends EmbedBuilder {
    public ErrorEmbed(String errorMsg) {
        setColor(new Color(0xdf2b0c));
        addField("Error", errorMsg, false);
        setFooter("GumCord");
    }

    @Override
    public String toString() {
        return "Error! \n"+getFields().get(0).getValue()+"\n\n>Please enable message embed permissions for GumCord";
    }
}
