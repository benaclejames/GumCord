package com.benaclejames.gumcord;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nonnull;

public final class SetupHandler extends ListenerAdapter {
    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        DynamoHelper.GetServer(event.getGuild());
        System.out.println("Joined " + event.getGuild().getName());
    }

    public static void updateGuildCommands(Guild guild, GumServer gumGuild) {
        if (gumGuild == null)
            gumGuild = DynamoHelper.GetServer(guild);

        if (gumGuild == null)
            return;

        var products = new OptionData(OptionType.STRING, "product_id", "Product Name", true);
        var aliases = new OptionData(OptionType.STRING, "alias", "Alias", true);

        for (var product : gumGuild.getRoles().keySet()) {
            products.addChoice(product, product);
        }

        for (var alias : gumGuild.getAliases().keySet()) {
            aliases.addChoice(alias, alias);
        }

        var linkAlias = Commands.slash("linkalias", "Links a Gumroad product to a Discord role")
                .addOptions(products)
                .addOption(OptionType.STRING, "alias", "Alias to link", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setGuildOnly(true);

        var unlinkAlias = Commands.slash("unlinkalias", "Unlinks a Gumroad alias from a product")
                .addOptions(aliases)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setGuildOnly(true);

        var unlinkProduct = Commands.slash("unlinkrole", "Unlinks a Gumroad product from a role")
                .addOptions(products)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setGuildOnly(true);

        guild.updateCommands().addCommands(linkAlias).addCommands(unlinkAlias).addCommands(unlinkProduct).queue();
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        GumServer gumGuild = DynamoHelper.GetServer(event.getGuild());
        System.out.println("Guild Ready: " + event.getGuild().getName());

        updateGuildCommands(event.getGuild(), gumGuild);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        var spawnVerify = Commands.slash("spawnverify", "Spawns a button to allow user to verify their purchases")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS))
                .setGuildOnly(true);

        var linkRole = Commands.slash("linkrole", "Links a Gumroad product to a Discord role")
                .addOption(OptionType.STRING, "product_id", "Product Name", true)
                .addOption(OptionType.ROLE, "role", "Role to link", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setGuildOnly(true);

        Main.jda.updateCommands().addCommands(spawnVerify).addCommands(linkRole).queue();
        System.out.println("Commands registered. Bot Ready!");
    }
}
