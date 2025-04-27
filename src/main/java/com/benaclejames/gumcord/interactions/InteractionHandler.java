package com.benaclejames.gumcord.interactions;

import com.benaclejames.gumcord.commands.LicenseVerifier;
import com.benaclejames.gumcord.dynamo.DynamoHelper;
import com.benaclejames.gumcord.dynamo.TableTypes.GumRole;
import com.benaclejames.gumcord.dynamo.TableTypes.GumServer;
import com.benaclejames.gumcord.interactions.modal.VerifyModal;
import com.benaclejames.gumcord.SetupHandler;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class InteractionHandler extends ListenerAdapter {
    private LicenseVerifier verifier;
    private SetupHandler setupHandler;
    private final Logger logger = LoggerFactory.getLogger(InteractionHandler.class);

    public InteractionHandler(SetupHandler setupHandler) {
        this.setupHandler = setupHandler;
        this.verifier = new LicenseVerifier();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        logger.trace("onSlashCommandInteraction");

        String productId = event.getOption("product_id", "", OptionMapping::getAsString);
        GumServer gumGuild = DynamoHelper.getServer(event.getGuild());

        switch (event.getFullCommandName()) {
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
                if (productId.startsWith("http") || productId.length() >= 100) {
                    event.reply("This is not your product ID. Read https://gumroad.com/help/article/76-license-keys.html to get help finding your product ID").setEphemeral(true).queue();
                    return;
                }

                Role role = event.getOption("role").getAsRole();
                GumServer server = DynamoHelper.getServer(event.getGuild());

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

                String alias = event.getOption("alias", "", OptionMapping::getAsString);

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
                newRole.setRoleIds(new Long[]{role.getIdLong()});
                server.getRoles().put(productId, newRole);
                server.getAliases().put(alias, productId);

                DynamoHelper.saveServer(server);
                setupHandler.updateGuildCommands(event.getGuild(), server);

                event.reply("Role linked Successfully!").setEphemeral(true).queue();
            }
            break;

            case "unlinkrole":
            {
                gumGuild.getRoles().remove(productId);
                gumGuild.getAliases().entrySet().removeIf(entry -> entry.getValue().equals(productId));

                DynamoHelper.saveServer(gumGuild);
                setupHandler.updateGuildCommands(event.getGuild(), gumGuild);

                event.reply("Role unlinked Successfully!").setEphemeral(true).queue();
            }
            break;

            case "verify":
            {
                var aliases = gumGuild.getAliases();
                var foundAlias = aliases.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(productId))
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (foundAlias.isEmpty()) {
                    event.reply("Alias not found. Refreshing guild commands...").queue();
                    setupHandler.updateGuildCommands(event.getGuild(), gumGuild);
                    break;
                }

                event.replyModal(new VerifyModal(productId, foundAlias.get())).queue();
                break;
            }

            case "addrole":
            {
                Role role = event.getOption("role").getAsRole();
                GumServer server = DynamoHelper.getServer(event.getGuild());

                break;
            }

            default:
                logger.warn("Unknown command: {}", event.getFullCommandName());
        }
    }

    private String getNameFromIdDropdown(StringSelectMenu selectDropdown, String id) {
        return selectDropdown.getOptions().stream().filter(option -> option.getValue().equals(id)).findFirst().get().getLabel();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().startsWith("verifyselector"))
            return;

        String productId = event.getValues().get(0);

        event.replyModal(new VerifyModal(productId, getNameFromIdDropdown(event.getInteraction().getSelectMenu(), productId))).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().startsWith("verifymodal_")) {
            String id = event.getModalId().replace("verifymodal_", "");
            String licenseKey = Objects.requireNonNull(event.getValue("key")).getAsString();

            GumServer gumGuild = DynamoHelper.getServer(event.getGuild());
            verifier.verifyLicense(event, id, licenseKey, gumGuild);
        }
    }

    public static List<Pair<String, String>> getMemberNewRoles(Guild guild, Member member) {
        List<Pair<String, String>> returnList = new ArrayList<>();

        // Attempt to get guild
        GumServer gumGuild = DynamoHelper.getServer(guild);
        if (gumGuild == null) return returnList;

        // Find all aliases that the user doesn't already have by finding the ID of each alias, then removing the ones that the user already has
        var aliases = gumGuild.getAliases();

        var userRoleIDs = member.getRoles().stream().map(Role::getIdLong).collect(Collectors.toCollection(HashSet::new));
        // Now create a list of gumroad IDs that corresponds to the IDs the user already has
        aliases.forEach((alias, id) -> {
            if (!userRoleIDs.containsAll(List.of(gumGuild.getRoles().get(id).RoleIds))) {
                returnList.add(new Pair<>(alias, id));
            }
        });

        return returnList;
    }
}
