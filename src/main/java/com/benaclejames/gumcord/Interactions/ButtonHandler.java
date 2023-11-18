package com.benaclejames.gumcord.Interactions;

import com.benaclejames.gumcord.Interactions.Modal.VerifyModal;
import com.benaclejames.gumcord.Interactions.SelectMenu.PaginatedSelectMenu;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.List;

public class ButtonHandler extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        // Ensure the button clicked has the id "verifybutton"
        if (!event.getComponentId().equals("verifybutton")) return;

        List<Pair<String, String>> userNewRoles = InteractionHandler.getMemberNewRoles(event.getGuild(), event.getMember());

        // If the user has all available roles, tell them and return
        if (userNewRoles.isEmpty()) {
            event.reply("You already have all possible roles for this server!").setEphemeral(true).queue();
            return;
        }

        // However, if we only have a single option, we can skip the menu and just verify the user for that role
        if (userNewRoles.size() == 1) {
            var firstElement = userNewRoles.get(0);
            String firstId = firstElement.component2();
            String firstAlias = firstElement.component1();
            event.replyModal(new VerifyModal(firstId, firstAlias)).queue();
            return;
        }

        // We've got this far, so now we need to build a selector. Since discord caps the amount of options in a dropdown
        // at 25, we might need to create multiple dropdowns

        PaginatedSelectMenu selectMenu = new PaginatedSelectMenu("verifyselector");

        for (Pair<String, String> item : userNewRoles) {
            selectMenu.addOption(item);
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Select a Product")
                .setDescription("Pick the role you'd like to verify!");

        if (selectMenu.getMaxPages() > 1) {
            embed.addField("Page", String.format("%d of %d", selectMenu.getCurrentPage(), selectMenu.getMaxPages()), true);
        }

        var reply = event.replyEmbeds(embed.build())
                .setComponents(selectMenu.build())
                .setEphemeral(true);

        reply.queue();
    }
}
