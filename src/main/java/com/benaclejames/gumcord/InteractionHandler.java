package com.benaclejames.gumcord;

import com.benaclejames.gumcord.Commands.LicenseVerifier;
import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumRole;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import com.benaclejames.gumcord.Dynamo.TableTypes.TokenList;
import com.benaclejames.gumcord.Utils.GumRoad;
import kotlin.Triple;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class InteractionHandler extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        switch (event.getCommandPath()) {
            case "spawnverify":
            {
                // Create a button
                var button = Button.primary("verifybutton", "Verify");

                // Create a fancy embed to tell people to click the button below to start verifying their purchase
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Verify your purchase");
                embed.setDescription("Click the button below to begin verifying your purchase");
                embed.setColor(new Color(0x2fdf0c));

                // Send it in the channel that the slash command was run in
                event.getChannel().sendMessageEmbeds(embed.build())
                        .addActionRow(button)
                        .queue();

                event.reply("Button Created!").setEphemeral(true).queue();
            }
            break;

            case "linkrole":
            {
                String productId = event.getOption("product_id").getAsString();
                Role role = event.getOption("role").getAsRole();
                GumServer server = DynamoHelper.GetServer(event.getGuild());

                // Ensure we don't have 25 roles already linked
                if (server.getRoles().size() >= 25) {
                    event.reply("You can't link more than 25 roles (for now). Please unlink a role before using this command").setEphemeral(true).queue();
                    return;
                }

                // Check that this role is not already linked to a product
                if (server.getRoles().containsKey(productId)) {
                    event.reply("This product is already linked to a role!").setEphemeral(true).queue();
                    return;
                }

                // Now ensure we're high enough in the role hierarchy to assign this role and respond with an error if we're not
                if (!event.getGuild().getSelfMember().canInteract(role)) {
                    event.reply("I don't have permission to assign that role! Try moving me up in the role hierarchy.").setEphemeral(true).queue();
                    return;
                }

                String alias = event.getOption("alias").getAsString();

                // Ensure this alias doesn't already exist
                if (server.getAliases().containsKey(alias)) {
                    event.reply("This alias is already in use!").setEphemeral(true).queue();
                    return;
                }

                // Ensure we actually have the permissions to apply this role
                if (!event.getGuild().getSelfMember().canInteract(role))
                {
                    event.reply("I don't have a high enough permission level to apply this role! Please ensure the role for GumCord is higher than the role you're trying to apply in the role hierarchy.").setEphemeral(true).queue();
                    break;
                }

                GumRole newRole = new GumRole();
                newRole.setRoleId(role.getIdLong());
                server.getRoles().put(productId, newRole);
                TokenList newList = new TokenList();
                newList.setId(productId);
                server.getUsedTokens().put(productId, newList);
                server.getPendingTokens().put(productId, newList);
                server.getAliases().put(alias, productId);

                DynamoHelper.SaveServer(server);
                SetupHandler.updateGuildCommands(event.getGuild(), server);

                event.reply("Role linked Successfully!").setEphemeral(true).queue();
            }
            break;

            case "unlinkrole":
            {
                String productId = event.getOption("product_id").getAsString();

                GumServer server = DynamoHelper.GetServer(event.getGuild());
                server.getRoles().remove(productId);
                server.getUsedTokens().remove(productId);
                server.getPendingTokens().remove(productId);
                server.getAliases().entrySet().removeIf(entry -> entry.getValue().equals(productId));

                DynamoHelper.SaveServer(server);
                SetupHandler.updateGuildCommands(event.getGuild(), server);

                event.reply("Role unlinked Successfully!").setEphemeral(true).queue();
            }
            break;

            case "getmemberinfo":
            {
                // Gets information about what roles the mentioned user has verified, and the license key information
                var member = event.getOption("member").getAsMember();

                GumServer server = DynamoHelper.GetServer(event.getGuild());
                // For each role, store the id, alias, and key
                ArrayList<Triple<String, String, String>> roleInfo = new ArrayList<>();
                for (var alias : server.getAliases().keySet()) {
                    String productId = server.getAliases().get(alias);
                    var usedTokens = server.getUsedTokens().get(productId);

                    // Somewhat inneficient, since the primary key is the token, but find the key by the member id
                    var token = usedTokens.getTokens().entrySet().stream().filter(entry -> entry.getValue().equals(member.getIdLong())).findFirst().orElse(null);
                    if (token != null)
                        roleInfo.add(new Triple<>(productId, alias, token.getKey()));
                }

                // Spawn a dropdown with all the roles the user has verified
                SelectMenu.Builder roleDropdown = SelectMenu.create("memberInfoRoleDropdown").setMaxValues(1);
                for (var info : roleInfo) {
                    roleDropdown.addOption(info.getSecond(), "memberinfo:"+info.getFirst()+":"+info.getSecond()+":"+info.getThird());
                }
                event.reply("Select a role to get information about!")
                        .addActionRow(roleDropdown.build())
                        .setEphemeral(true)
                        .queue();
            }
            break;
        }
    }


    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        // Ensure the button clicked has the id "verifybutton"
        if (!event.getComponentId().equals("verifybutton")) return;

        // Attempt to get guild
        GumServer gumGuild = event.isFromGuild() ? DynamoHelper.GetServer(event.getGuild()) : null;
        if (gumGuild == null) return;

        // Create our selector
        SelectMenu.Builder aliasMenu = SelectMenu.create("verifyselector").setMaxValues(1);

        // Find all aliases that the user doesn't already have by finding the ID of each alias, then removing the ones that the user already has
        var aliases = gumGuild.getAliases();

        // If no aliases have been setup, tell the user instead
        if (aliases.isEmpty()) {
            event.reply("No roles have been setup for this server yet!").setEphemeral(true).queue();
            return;
        }

        var userRoleIDs = event.getMember().getRoles().stream().map(net.dv8tion.jda.api.entities.Role::getIdLong).collect(Collectors.toList());
        // Now create a list of gumroad IDs that corresponds to the IDs the user already has
        aliases.forEach((alias, id) -> {
            if (!userRoleIDs.contains(gumGuild.getRoles().get(id).RoleId)) {
                aliasMenu.addOption(alias, id);
            }
        });

        // If the aliasMenu doesn't contain any options, tell the user that they already have all possible roles.
        if (aliasMenu.getOptions().isEmpty()) {
            event.reply("You already have all possible roles for this server!").setEphemeral(true).queue();
            return;
        }

        // However, if we only have a single option, we can skip the menu and just verify the user for that role
        if (aliasMenu.getOptions().size() == 1) {
            String firstId = aliasMenu.getOptions().get(0).getValue();
            String firstAlias = aliasMenu.getOptions().get(0).getLabel();
            event.replyModal(createVerifyWindow(firstId, firstAlias)).queue();
            return;
        }

        // Send the selector
        event.reply("Select a product to verify!")
                .addActionRow(aliasMenu.build())
                .setEphemeral(true)
                .queue();
    }

    private Modal createVerifyWindow(String productId, String productName) {
        TextInput subject = TextInput.create("key", "License Key", TextInputStyle.SHORT)
                .setPlaceholder("12345678-12345678-12345678-12345678")
                .setRequiredRange(35, 35)
                .setRequired(true)
                .build();

        return Modal.create("verifymodal_" + productId, "Verify License Key for " + productName)
                .addActionRows(ActionRow.of(subject))
                .build();
    }

    private String getNameFromIdDropdown(SelectMenu selectDropdown, String id) {
        return selectDropdown.getOptions().stream().filter(option -> option.getValue().equals(id)).findFirst().get().getLabel();
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (event.getComponentId().equals("verifyselector")) {

            String productId = event.getValues().get(0);

            event.replyModal(createVerifyWindow(productId, getNameFromIdDropdown(event.getInteraction().getSelectMenu(), productId))).queue();
        }

        if (event.getComponentId().startsWith("memberInfoRoleDropdown"))
        {
            // Get the selected value
            String[] parts = event.getValues().get(0).split(":");
            String product_id = parts[1];
            String alias = parts[2];
            String key = parts[3];


            var resp = GumRoad.GetLicense(product_id, key);
            // Format a fancy embed for this data
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("License Information");
            embed.setDescription("Information about the license for " + alias);
            embed.setColor(new Color(0x2fdf0c));

            embed.addField("Product Name", resp.purchase.product_name, false);
            embed.addField("Product ID", resp.purchase.product_id, false);
            embed.addField("Email", resp.purchase.email, false);
            embed.addField("Price", resp.purchase.price.toString(), false);
            embed.addField("Currency", resp.purchase.currency, false);
            embed.addField("Referrer", resp.purchase.referrer, false);
            embed.addField("Order Number", resp.purchase.order_number.toString(), false);
            embed.addField("Sale ID", resp.purchase.sale_id, false);
            embed.addField("Sale Timestamp", resp.purchase.sale_timestamp, false);
            embed.addField("Refunded", resp.purchase.refunded.toString(), false);
            embed.addField("IP Country", resp.purchase.ip_country, false);
            embed.addField("Card", resp.purchase.card.visual, false);
            embed.addField("Card Type", resp.purchase.card.type, false);

            for (var customField : resp.purchase.custom_fields) {
                embed.addField(customField, "Custom Field", false);
            }

            embed.addField("PLEASE BE RESPONSIBLE", "Please be mindful of sharing this information. " +
                    "This functionality should only be used when absolutely necessary.", false);

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().startsWith("verifymodal_")) {
            String id = event.getModalId().replace("verifymodal_", "");
            String licenseKey = event.getValue("key").getAsString();

            GumServer gumGuild = DynamoHelper.GetServer(event.getGuild());
            LicenseVerifier.VerifyLicense(event, id, licenseKey, gumGuild);
        }
    }
}
