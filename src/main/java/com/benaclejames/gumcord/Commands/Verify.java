package com.benaclejames.gumcord.Commands;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Utils.ErrorEmbed;
import com.benaclejames.gumcord.Utils.GumRoad;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Target handler for the "verify" command
 */
public class Verify implements GumCommand {

    LicenseVerifier verifier = new LicenseVerifier();

    @Override
    public void Invoke(Message msg, String[] args) {
        if (msg.isFromType(ChannelType.PRIVATE)) {    // License verification not supported in DMs
            msg.getChannel().sendMessage(new ErrorEmbed("License Verification isn't supported in DMs... **Yet**").build()).queue();
            return;
        }

        if (args.length == 2)
            verifier.VerifyLicense(msg, args[0], args[1]);

        // Delete the request in case it contained a token, though stealing the token would be unlikely
        msg.delete().queue();
    }
}

/**
 * Contains logic for each step of the license verification process
 */
final class LicenseVerifier {
    private Consumer<Message> DeleteIn(long seconds) {
        return message -> {
            try {
                TimeUnit.SECONDS.sleep(seconds);
                message.delete().queue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    private void PrintError(MessageChannel channel, String errorText) {
        channel.sendMessage(new ErrorEmbed(errorText).build()).queue(DeleteIn(10L));
    }

    public void VerifyLicense(Message msg, String gumroadIdOrAlias, String token) {

        // Check if we have an applicable alias
        String gumroadId = DynamoHelper.GetGumroadIdFromAlias(msg.getGuild().getIdLong(), gumroadIdOrAlias);
        if (gumroadId == null)
            gumroadId = gumroadIdOrAlias;


        // Get Gumroad to RoleID
        Long roleId = DynamoHelper.GetGumroadToRoleId(msg.getGuild().getIdLong(), gumroadId);
        if (roleId == null)
        {
            PrintError(msg.getChannel(), "This Gumroad ID/Alias is missing a role!");
            return;
        }

        // Get literal Discord role
        Role roleToAssign = msg.getGuild().getRoleById(roleId);
        if (roleToAssign == null) {
            PrintError(msg.getChannel(), "Linked role no longer exists!");
            return;
        }

        // Make sure user doesn't already have the role
        if (Objects.requireNonNull(msg.getMember()).getRoles().contains(roleToAssign)) {
            PrintError(msg.getChannel(), "You already have this role!");

            // If the license is valid, but it's not been used and the user already has the role, snag the token and set the user as the owner
            if (DynamoHelper.AlreadyUsedToken(msg.getGuild().getIdLong(), gumroadId, token) == null && GumRoad.GetLicenseValid(gumroadId, token))
                DynamoHelper.AppendUsedToken(msg.getGuild().getIdLong(), gumroadId, token, msg.getAuthor().getIdLong());
            return;
        }

        Long currentLicenseHolder = DynamoHelper.AlreadyUsedToken(msg.getGuild().getIdLong(), gumroadId, token);
        if (currentLicenseHolder != null) {
            PrintError(msg.getChannel(), currentLicenseHolder == msg.getAuthor().getIdLong() ? "You've already used this license key." : "Someone else has already used this license key.");
            return;
        }

        if (!GumRoad.GetLicenseValid(gumroadId, token)) {
            PrintError(msg.getChannel(), "This license key is invalid.");
            return;
        }

        msg.getGuild().addRoleToMember(msg.getMember(), roleToAssign).queue();
        DynamoHelper.AppendUsedToken(msg.getGuild().getIdLong(), gumroadId, token, msg.getAuthor().getIdLong());

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x2fdf0c));
        eb.addField("Verification Success", "Role added to ```"+msg.getAuthor().getName()+"#"+msg.getAuthor().getDiscriminator()+"```", true);
        eb.setFooter("GumCord");

        msg.getChannel().sendMessage(eb.build()).queue(botResponse -> {
            try {
                TimeUnit.SECONDS.sleep(30L);
                botResponse.delete().queue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
