package com.benaclejames.gumcord;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;

import javax.annotation.Nonnull;
import java.util.*;

public final class SetupHandler extends ListenerAdapter {
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        GumServer gumGuild = DynamoHelper.GetServer(event.getGuild());
        if (gumGuild == null)
            return;

        OptionData aliasOptions = new OptionData(OptionType.STRING, "alias", "Product Name", true);
        for (Map.Entry<String, String> entry : gumGuild.getAliases().entrySet()) {
            aliasOptions.addChoice(entry.getKey(), entry.getValue());
        }

        try {
            System.out.println("Registering slash commands for " + event.getGuild().getName());
            event.getGuild().updateCommands().addCommands(
                            Commands.slash("verify", "Verifies a purchase using Gumroad License Key")
                                .addOptions(aliasOptions)
                                .addOption(OptionType.STRING, "key", "Gumroad License Key", true)).
                    queue(null, new ErrorHandler()
                            .handle(ErrorResponse.MISSING_ACCESS, (e) -> System.out.println("Missing access to update commands in " + event.getGuild().getName())));
        }
        catch (Exception e) {
            // Print that we don't have perms to add commands for guild name
            System.out.println("Could not add commands for guild " + event.getGuild().getName());
        }
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        var command = Commands.slash("spawnverify", "Spawns a button to allow user to verify their purchases")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS))
                .setGuildOnly(true);

        Main.jda.updateCommands().addCommands(command).queue();
        System.out.println("Commands registered. Bot Ready!");
    }
}
