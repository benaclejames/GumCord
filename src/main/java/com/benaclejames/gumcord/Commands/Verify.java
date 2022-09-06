package com.benaclejames.gumcord.Commands;

import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import com.benaclejames.gumcord.Utils.ErrorEmbed;
import net.dv8tion.jda.api.entities.*;

/**
 * Target handler for the "verify" command
 */
public class Verify implements GumCommand {

    @Override
    public void Invoke(Message msg, String[] args, GumServer guild) {
        if (msg.isFromType(ChannelType.PRIVATE)) {    // License verification not supported in DMs
            msg.getChannel().sendMessageEmbeds(new ErrorEmbed("Initiating License Verification isn't supported in DMs... **Yet**", null).build()).queue();
            return;
        }

        // Print details about the message, sender, channel and guild
        System.out.println("Message: " + msg.getContentDisplay());
        System.out.println("Sender: " + msg.getAuthor().getName());
        System.out.println("Channel: " + msg.getChannel().getName());
        System.out.println("Guild: " + msg.getGuild().getName());

        try {
            //if (args.length == 2)
                //LicenseVerifier.VerifyLicense(msg, args[0], args[1], guild);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Delete the request in case it contained a token, though stealing the token would be unlikely
        msg.delete().queue();
    }
}

