package com.benaclejames.gumcord.commands;

import com.benaclejames.gumcord.dynamo.DynamoHelper;
import com.benaclejames.gumcord.dynamo.TableTypes.GumPurchase;
import com.benaclejames.gumcord.dynamo.TableTypes.GumRole;
import com.benaclejames.gumcord.dynamo.TableTypes.GumServer;
import com.benaclejames.gumcord.utils.ErrorEmbed;
import com.benaclejames.gumcord.utils.GumRoad;
import com.benaclejames.gumcord.utils.GumRoadResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Contains logic for each step of the license verification process
 */
public final class LicenseVerifier {
    Logger logger = LoggerFactory.getLogger(LicenseVerifier.class);

    private void printError(IReplyCallback reply, String errorText, String additionalInfo) {
        ErrorEmbed embed = new ErrorEmbed(errorText, additionalInfo);
        reply.replyEmbeds(embed.build()).setEphemeral(true).queue();
        logger.error("{}: {}", errorText, additionalInfo);
    }

    private static String constructUserIdentifier(User user) {
        return "```" + user.getName() + "```";
    }

    public void verifyLicense(IReplyCallback msg, String gumroadIdOrAlias, String token, GumServer guild) {

        // Print our debug info
        logger.info("GuildID: {}", guild.guild.getId());
        logger.info("User: {}", msg.getUser().getName());
        logger.info("Verifying license for {} with token {}", gumroadIdOrAlias, token);

        // Check if we have an applicable alias
        String gumroadId = guild.getAliases().get(gumroadIdOrAlias);
        if (gumroadId == null)
            gumroadId = gumroadIdOrAlias;

        // Get Gumroad to RoleID
        GumRole roleInfo = guild.getRoles().get(gumroadId);
        if (roleInfo == null || roleInfo.getRoleIds() == null) {
            printError(msg, "Missing Role!", "Please alert a server administrator!");
            return;
        }

        // Now check if this key has already been redeemed in this server. If it has, and it was redeemed by a different person, reject it
        GumPurchase purchase = DynamoHelper.getPurchaseByKey(token);
        if (purchase != null && purchase.getUserId() != Objects.requireNonNull(msg.getMember()).getIdLong()) {
                printError(msg, "Someone else has already used this license key.", null);
                return;
        }
        // Otherwise continue since the person may have just rejoined


        // Ensure our RoleLiteral isn't null
        if (roleInfo.RoleLiterals == null) {
            printError(msg, "This Gumroad ID/Alias is missing a role!", null);
            return;
        }

        // Make sure user doesn't already have the role
        if (new HashSet<>(Objects.requireNonNull(msg.getMember()).getRoles()).containsAll(List.of(roleInfo.RoleLiterals))) {
            printError(msg, "You already have this role!", null);

            // If the license is valid, but it's not been used and the user already has the role, snag the token and set the user as the owner
            if (GumRoad.GetLicense(gumroadId, token).IsValid()) {
                DynamoHelper.createPurchase(gumroadId, msg.getUser(), token);
                DynamoHelper.saveServer(guild);
            }
            return;
        }

        GumRoadResponse response = GumRoad.GetLicense(gumroadId, token);
        if (!response.IsValid()) {
            printError(msg, "This license key is invalid.", null);
            return;
        }

        for (var roleLiteral : roleInfo.RoleLiterals) {
            Objects.requireNonNull(msg.getGuild()).addRoleToMember(msg.getMember(), roleLiteral).queue();
        }
        DynamoHelper.createPurchase(gumroadId, msg.getUser(), token);
        DynamoHelper.saveServer(guild);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x2fdf0c));
        eb.addField("Verification Success", "Role added to " + constructUserIdentifier(msg.getMember().getUser()), true);
        eb.setFooter("GumCord");

        msg.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }
}
