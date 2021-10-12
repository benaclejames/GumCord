package com.benaclejames.gumcord.Commands;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumGuild;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumRole;
import com.benaclejames.gumcord.Utils.AdminChannel;
import com.benaclejames.gumcord.Utils.ErrorEmbed;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumRoad;
import com.benaclejames.gumcord.Utils.GumRoadResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Target handler for the "verify" command
 */
public class Verify implements GumCommand {

    LicenseVerifier verifier = new LicenseVerifier();

    @Override
    public void Invoke(Message msg, String[] args, GumGuild guild) {
        if (msg.isFromType(ChannelType.PRIVATE)) {    // License verification not supported in DMs
            msg.getChannel().sendMessage(new ErrorEmbed("License Verification isn't supported in DMs... **Yet**").build()).queue();
            return;
        }

        if (args.length == 2)
            verifier.VerifyLicense(msg, args[0], args[1], guild.getAdminChannel());

        // Delete the request in case it contained a token, though stealing the token would be unlikely
        msg.delete().queue();
    }
}

/**
 * Contains logic for each step of the license verification process
 */
final class LicenseVerifier {

    private void PrintError(Guild guild, MessageChannel channel, String errorText) {
        ErrorEmbed embed = new ErrorEmbed(errorText);
        if (guild.getSelfMember().getPermissions((GuildChannel) channel).contains(Permission.MESSAGE_EMBED_LINKS))
            channel.sendMessage(embed.build()).delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
        else
            channel.sendMessage(embed.toString()).queue();
    }

    private String ConstructUserIdentifier(User user) {
        return "```"+user.getName()+"#"+user.getDiscriminator()+"```";
    }

    public void VerifyLicense(Message msg, String gumroadIdOrAlias, String token, AdminChannel admins) {

        // Check if we have an applicable alias
        String gumroadId = DynamoHelper.GetGumroadIdFromAlias(msg.getGuild().getIdLong(), gumroadIdOrAlias);
        if (gumroadId == null)
            gumroadId = gumroadIdOrAlias;

        token = token.replaceAll("[^a-zA-Z0-9-]", "");

        // Get Gumroad to RoleID
        GumRole roleInfo = DynamoHelper.GetGumroadRoleInfo(msg.getGuild().getIdLong(), gumroadId);
        if (roleInfo == null || roleInfo.RoleId == null)
        {
            PrintError(msg.getGuild(), msg.getChannel(), "This Gumroad ID/Alias is missing a role!");
            return;
        }

        // Get literal Discord role
        Role roleToAssign = msg.getGuild().getRoleById(roleInfo.RoleId);
        if (roleToAssign == null) {
            PrintError(msg.getGuild(), msg.getChannel(), "Linked role no longer exists!");
            return;
        }

        Long currentLicenseHolder = DynamoHelper.AlreadyUsedToken(msg.getGuild().getIdLong(), gumroadId, token);
        if (currentLicenseHolder != null) {
            if (currentLicenseHolder == msg.getAuthor().getIdLong())
                PrintError(msg.getGuild(), msg.getChannel(), "You've already used this license key.");
            else {
                PrintError(msg.getGuild(), msg.getChannel(), "Someone else has already used this license key.");
                msg.getGuild().retrieveMemberById(currentLicenseHolder).queue(
                        member -> admins.Announce("Potentially Stolen Key", ConstructUserIdentifier(msg.getAuthor())+" attempted to use "+ConstructUserIdentifier(member.getUser())+"'s license key."),
                        new ErrorHandler().handle(ErrorResponse.UNKNOWN_USER,
                                // Failed to find user, use generic name
                                e -> admins.Announce("Potentially Stolen Key", ConstructUserIdentifier(msg.getAuthor())+" attempted to use another user's license key.")));
            }
            return;
        }

        // Make sure user doesn't already have the role
        if (Objects.requireNonNull(msg.getMember()).getRoles().contains(roleToAssign)) {
            PrintError(msg.getGuild(), msg.getChannel(), "You already have this role!");

            // If the license is valid, but it's not been used and the user already has the role, snag the token and set the user as the owner
            if (DynamoHelper.AlreadyUsedToken(msg.getGuild().getIdLong(), gumroadId, token) == null && GumRoad.GetLicense(gumroadId, token).IsValid())
                DynamoHelper.AppendUsedToken(msg.getGuild().getIdLong(), gumroadId, token, msg.getAuthor().getIdLong());
            return;
        }

        GumRoadResponse response = GumRoad.GetLicense(gumroadId, token);
        if (!response.IsValid()) {
            PrintError(msg.getGuild(), msg.getChannel(), "This license key is invalid.");
            return;
        }

        if (response.ExceedsTimestamp(roleInfo.MaxKeyAge)) {
            PrintError(msg.getGuild(), msg.getChannel(), "This license key has expired.");
            admins.Announce("Expired Key", ConstructUserIdentifier(msg.getAuthor())+" attempted to use an expired key.");
            return;
        }

        msg.getGuild().addRoleToMember(msg.getMember(), roleToAssign).queue();
        DynamoHelper.AppendUsedToken(msg.getGuild().getIdLong(), gumroadId, token, msg.getAuthor().getIdLong());

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x2fdf0c));
        eb.addField("Verification Success", "Role added to "+ConstructUserIdentifier(msg.getAuthor()), true);
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
