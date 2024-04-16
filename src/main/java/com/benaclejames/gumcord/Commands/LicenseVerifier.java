package com.benaclejames.gumcord.Commands;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumPurchase;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumRole;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import com.benaclejames.gumcord.Utils.ErrorEmbed;
import com.benaclejames.gumcord.Utils.GumRoad;
import com.benaclejames.gumcord.Utils.GumRoadResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.awt.*;
import java.util.HashSet;
import java.util.List;

/**
 * Contains logic for each step of the license verification process
 */
public final class LicenseVerifier {

    private static void PrintError(IReplyCallback reply, String errorText, String additionalInfo) {
        ErrorEmbed embed = new ErrorEmbed(errorText, additionalInfo);
        reply.replyEmbeds(embed.build()).setEphemeral(true).queue();
        System.out.println(errorText+": "+additionalInfo);
    }

    private static String ConstructUserIdentifier(User user) {
        return "```" + user.getName() + "#" + user.getDiscriminator() + "```";
    }

    public static void VerifyLicense(IReplyCallback msg, String gumroadIdOrAlias, String token, GumServer guild) {

        // Print our debug info
        System.out.println("GuildID: " + guild.guild.getId());
        System.out.println("User: " + msg.getUser().getName() + "#" + msg.getUser().getDiscriminator());
        System.out.println("Verifying license for " + gumroadIdOrAlias + " with token " + token);

        // Check if we have an applicable alias
        String gumroadId = guild.getAliases().get(gumroadIdOrAlias);
        if (gumroadId == null)
            gumroadId = gumroadIdOrAlias;

        // Get Gumroad to RoleID
        GumRole roleInfo = guild.getRoles().get(gumroadId);
        if (roleInfo == null || roleInfo.RoleId == null) {
            //guild.getGuildSettings().adminChannel.Announce("License Verification Failed", "User " + ConstructUserIdentifier(msg.getUser()) + " attempted to verify a license for " + gumroadId + " but no role was found.");
            PrintError(msg, "Missing Role!", "Please alert a server administrator!");
            return;
        }

        // Now check if this key has already been redeemed in this server. If it has and it was redeemed by a different person, reject it
        GumPurchase purchase = DynamoHelper.GetPurchaseByKey(token);
        if (purchase != null && purchase.getUserId() != msg.getMember().getIdLong()) {
                PrintError(msg, "Someone else has already used this license key.", null);
                //msg.getGuild().retrieveMemberById(currentLicenseHolder).queue(
                //        member -> guild.getGuildSettings().adminChannel.Announce("Potentially Stolen Key", ConstructUserIdentifier(msg.getMember().getUser()) + " attempted to use " + ConstructUserIdentifier(member.getUser()) + "'s license key."));
                return;
        }
        // Otherwise continue since the person may have just rejoined

        /*Long pendingKeyOwner = guild.getPendingTokens().get(gumroadId).getTokens().get(token);
        if (pendingKeyOwner != null) {
            PrintError(msg, "This license key is already pending manual verification.", roleInfo.OODAdditionalInfo);

            //if (pendingKeyOwner != msg.getMember().getIdLong()) {
            //    guild.getGuildSettings().adminChannel.Announce("Pending Key", ConstructUserIdentifier(msg.getMember().getUser()) + " attempted to use a pending license key owned by someone else.");
            //}

            return;
        }*/

        // Ensure our RoleLiteral isn't null
        if (roleInfo.RoleLiterals == null) {
            PrintError(msg, "This Gumroad ID/Alias is missing a role!", null);
            return;
        }

        // Make sure user doesn't already have the role
        if (new HashSet<>(msg.getMember().getRoles()).containsAll(List.of(roleInfo.RoleLiterals))) {
            PrintError(msg, "You already have this role!", null);

            // If the license is valid, but it's not been used and the user already has the role, snag the token and set the user as the owner
            if (GumRoad.GetLicense(gumroadId, token).IsValid()) {
                DynamoHelper.CreatePurchase(gumroadId, msg.getUser(), token);
                DynamoHelper.SaveServer(guild);
            }
            return;
        }

        GumRoadResponse response = GumRoad.GetLicense(gumroadId, token);
        if (!response.IsValid()) {
            PrintError(msg, "This license key is invalid.", null);
            return;
        }

        /*if (roleInfo.MaxKeyAge != null) {
            long keyAgeDiff = response.GetKeyAge() - roleInfo.MaxKeyAge;
            if (keyAgeDiff > 0) {   // Key is older than max age
                PrintError(msg, "This license key has expired.", guild.getGuildSettings().getOODAdditionalInfo());
                guild.getPendingTokens().get(gumroadId).getTokens().put(token, msg.getMember().getIdLong());
                DynamoHelper.SaveServer(guild);
                //guild.getGuildSettings().adminChannel.Announce("Expired Key", ConstructUserIdentifier(msg.getMember().getUser()) + " attempted to use a key that expired " + keyAgeDiff + " hours ago.");
                return;
            }
        }*/

        for (var roleLiteral : roleInfo.RoleLiterals) {
            msg.getGuild().addRoleToMember(msg.getMember(), roleLiteral).queue();
        }
        DynamoHelper.CreatePurchase(gumroadId, msg.getUser(), token);
        DynamoHelper.SaveServer(guild);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x2fdf0c));
        eb.addField("Verification Success", "Role added to " + ConstructUserIdentifier(msg.getMember().getUser()), true);
        eb.setFooter("GumCord");

        msg.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }
}
