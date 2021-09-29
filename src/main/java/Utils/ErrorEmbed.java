package Utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class ErrorEmbed extends EmbedBuilder {
    public ErrorEmbed(String errorMsg) {
        setColor(Color.decode("0xdf2b0c"));
        addField("Error", errorMsg, false);
        setFooter("GumCord");
    }
}
