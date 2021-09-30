package Commands;

import Dynamo.DynamoHelper;
import Utils.ErrorEmbed;
import Utils.GumRoad;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.io.IOException;

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

        verifier.VerifyLicense(msg, args[0], args[1]);
    }
}

/**
 * Contains logic for each step of the license verification process
 */
final class LicenseVerifier {
    public void VerifyLicense(Message msg, String gumroadIdOrAlias, String token) {

        // Check if we have an applicable alias
        String gumroadId = DynamoHelper.GetGumroadIdFromAlias(msg.getGuild().getIdLong(), gumroadIdOrAlias);
        if (gumroadId == null)
            gumroadId = gumroadIdOrAlias;


        // Get Gumroad to RoleID
        Long roleId = DynamoHelper.GetGumroadToRoleId(msg.getGuild().getIdLong(), gumroadIdOrAlias);
        if (roleId == null)
        {
            msg.getChannel().sendMessage(new ErrorEmbed("This Gumroad ID/Alias is missing a role!").build()).queue();
            return;
        }

        // Get literal Discord role
        Role roleToAssign = msg.getGuild().getRoleById(roleId);
        if (roleToAssign == null) {
            msg.getChannel().sendMessage(new ErrorEmbed("Linked role no longer exists!").build()).queue();
            return;
        }

        // Make sure user doesn't already have the role
        if (msg.getMember().getRoles().contains(roleToAssign)) {
            msg.getChannel().sendMessage(new ErrorEmbed("You already have this role!").build()).queue();
            return;
        }

        Long currentLicenseHolder = DynamoHelper.AlreadyUsedToken(msg.getGuild().getIdLong(), gumroadId, token);
        if (currentLicenseHolder != null) {
            msg.getChannel().sendMessage(new ErrorEmbed(currentLicenseHolder == msg.getAuthor().getIdLong() ? "You've already used this license key." : "Someone else has already used this license key.").build()).queue();
            return;
        }

        boolean gumRoadLicenseValid = false;
        try {
            gumRoadLicenseValid = GumRoad.GetLicenseValid(gumroadId, token);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!gumRoadLicenseValid) {
            msg.getChannel().sendMessage(new ErrorEmbed("This license key is invalid.").build()).queue();
            return;
        }

        msg.getGuild().addRoleToMember(msg.getMember(), roleToAssign).queue();
        DynamoHelper.AppendUsedToken(msg.getGuild().getIdLong(), gumroadId, token, msg.getAuthor().getIdLong());

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x2fdf0c));
        eb.addField("Verification Success", "Role added to ```"+msg.getAuthor().getName()+"#"+msg.getAuthor().getDiscriminator()+"```", true);
        eb.setFooter("GumCord");

        msg.getChannel().sendMessage(eb.build()).queue();
    }
}
