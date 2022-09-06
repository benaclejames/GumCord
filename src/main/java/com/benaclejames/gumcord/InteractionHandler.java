package com.benaclejames.gumcord;

import com.benaclejames.gumcord.Commands.LicenseVerifier;
import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import net.dv8tion.jda.api.EmbedBuilder;
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

public class InteractionHandler extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (!event.getCommandPath().equals("createverifybutton"))
            return;

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


    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        // Ensure the button clicked has the id "verifybutton"
        if (!event.getComponentId().equals("verifybutton")) return;

        // Attempt to get guild
        GumServer gumGuild = event.isFromGuild() ? DynamoHelper.GetServer(event.getGuild()) : null;
        if (gumGuild == null) return;

        // Create our selector
        SelectMenu.Builder aliasMenu = SelectMenu.create("verifyselector").setMaxValues(1);

        // Add all aliases
        gumGuild.getAliases().forEach(aliasMenu::addOption);

        // Send the selector
        event.reply("Select a product to verify!")
                .addActionRow(aliasMenu.build())
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (!event.getComponentId().equals("verifyselector"))
            return;

        TextInput subject = TextInput.create("key", "License Key", TextInputStyle.SHORT)
                .setPlaceholder("12345678-12345678-12345678-12345678")
                .setRequiredRange(35, 35)
                .setRequired(true)
                .build();

        Modal modal = Modal.create("verifymodal_" + event.getValues().get(0), "Verify License Key")
                .addActionRows(ActionRow.of(subject))
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().startsWith("verifymodal_")) {
            String id = event.getModalId().replace("verifymodal_", "");
            String licenseKey = event.getValue("key").getAsString();

            GumServer gumGuild = event.isFromGuild() ? DynamoHelper.GetServer(event.getGuild()) : null;
            LicenseVerifier.VerifyLicense(event, id, licenseKey, gumGuild);
        }
    }
}
