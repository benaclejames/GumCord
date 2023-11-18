package com.benaclejames.gumcord.Interactions.SelectMenu;

import com.benaclejames.gumcord.Interactions.InteractionHandler;
import kotlin.Pair;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Creates a select menu and the corresponding page buttons for cases where more than 25 elements are in the menu
public class PaginatedSelectMenu {
    private final String id;
    private final List<Pair<String, String>> items;
    private final int page;

    public PaginatedSelectMenu(String name, int page) {
        this.id = name + '_' + page;
        this.items = new ArrayList<>();
        this.page = page;
    }

    public PaginatedSelectMenu(String name) {
        this(name, 0);
    }

    public void addOption(@NotNull() String label, @NotNull String value) {
        this.addOption(new Pair<>(label, value));
    }

    public void addOption(@NotNull() Pair<String, String> pair) {
        this.items.add(pair);
    }

    public Collection<? extends LayoutComponent> build() {
        int offset = this.page * 25;

        // First, fill our list with as many elements as we can before hitting the 25 item limit
        SelectMenu.Builder selectMenu = SelectMenu.create(this.id).setMaxValues(1);

        int i;
        for (i = 0; i < Math.min(25, this.items.size() - offset); i++) {
            Pair<String, String> listElement = this.items.get(offset + i);
            selectMenu.addOption(listElement.getFirst(), listElement.getSecond());
        }

        // Now if we still have items left, we need to add in a forward button. And if our offset isn't 0, a back button.
        List<Button> navigationButtons = new ArrayList<>();
        if (offset != 0) {
            navigationButtons.add(Button.danger("verifyselector_" + (this.page - 1), "Previous Page")
                    .withEmoji(Emoji.fromUnicode("⬅")));
        }

        if (this.items.size() > offset + i) {
            navigationButtons.add(Button.success("verifyselector_" + (this.page + 1), "Next Page")
                    .withEmoji(Emoji.fromUnicode("➡")));
        }

        List<ActionRow> actionRows = new ArrayList<>(List.of(ActionRow.of(selectMenu.build())));
        if (!navigationButtons.isEmpty())
        {
            actionRows.add(ActionRow.of(navigationButtons));
        }

        return actionRows;
    }

    public static class PaginatedSelectMenuButtonHandler extends ListenerAdapter {
        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            String componentId = event.getComponentId();
            if (!componentId.startsWith("verifyselector_"))
                return;

            String[] parts = componentId.split("_");

            // First, we extract the number after the underscore
            int pageId = Integer.parseInt(parts[1]);

            Guild guild = event.getGuild();
            Member guildMember = event.getMember();

            // With these values, we can reconstruct the original list, filter out the roles the user already has
            // and then reconstruct the PaginatedSelectMenu at the specified page and edit the message
            List<Pair<String, String>> memberNewRoles = InteractionHandler.getMemberNewRoles(guild, guildMember);

            int listOffset = pageId * 25;

            if (memberNewRoles.size() < listOffset) {
                event.reply("Strange, the amount of roles available to you exceed the new list offset. Did something change?")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            // Construct our new message edit data to replace our previous list with
            PaginatedSelectMenu newMenu = new PaginatedSelectMenu("verifyselector", pageId);
            for (Pair<String, String> option : memberNewRoles) {
                newMenu.addOption(option);
            }

            var built = newMenu.build();
            MessageEditBuilder editBuilder = new MessageEditBuilder()
                    .setComponents(built);

            event.editMessage(editBuilder.build()).queue();
        }
    }
}
