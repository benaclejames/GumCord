package com.benaclejames.gumcord;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class SetupHandler extends ListenerAdapter {
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        DynamoHelper.GetServer(event.getGuild());
        System.out.println("Joined " + event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {

    }

    public static void updateGuildCommands(Guild guild, GumServer gumGuild) {
        if (gumGuild == null)
            gumGuild = DynamoHelper.GetServer(guild);

        if (gumGuild == null)
            return;

        var products = new OptionData(OptionType.STRING, "product_id", "Product Name", true);

        // Consolidate the choices into a list. If an alias exists for the product, use that instead
        for (var product : gumGuild.getRoles().keySet()) {
            // Check to see if we have an available alias by finding it by value
            String alias = gumGuild.getAliases().entrySet().stream()
                    .filter(entry -> entry.getValue().equals(product))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(product);

            try {
                products.addChoice(alias, product);
            }
            catch (IllegalArgumentException e) {
                System.out.println("Illegal Argument Exception: " + e.getMessage());
                break;
            }
        }

        var unlinkProduct = Commands.slash("unlinkrole", "Unlinks a Gumroad product from a role")
                .addOptions(products)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setGuildOnly(true);

        var getMemberInfo = Commands.slash("getmemberinfo", "Gets verification information for the given Discord user")
                        .addOption(OptionType.USER, "member", "Member", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                                        .setGuildOnly(false);

        var addRole = Commands.slash("addrole", "Adds a role to a Gumroad product")
                .addOptions(products)
                .addOption(OptionType.ROLE, "role", "Role to add", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setGuildOnly(true);


        guild.updateCommands().addCommands(unlinkProduct, getMemberInfo).queue();
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        GumServer gumGuild = DynamoHelper.GetServer(event.getGuild());
        System.out.println("Guild Ready: " + event.getGuild().getName());

        updateGuildCommands(event.getGuild(), gumGuild);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        var spawnVerify = Commands.slash("spawnverify", "Spawns a button to allow user to verify their purchases")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS))
                .setGuildOnly(true);

        var linkRole = Commands.slash("linkrole", "Links a Gumroad product to a Discord role")
                .addOption(OptionType.STRING, "product_id", "Product Id", true)
                .addOption(OptionType.ROLE, "role", "Role to link", true)
                .addOption(OptionType.STRING, "alias", "Alias to use in the verification selection", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setGuildOnly(true);

        Main.jda.updateCommands().addCommands(spawnVerify, linkRole).queue();
        System.out.println("Commands registered. Bot Ready!");
    }
}
