package com.benaclejames.gumcord;

import com.benaclejames.gumcord.Commands.GumCommand;
import com.benaclejames.gumcord.Commands.Verify;
import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public final class CommandHandler extends ListenerAdapter {

    private final HashMap<String, GumCommand> commands = new HashMap<>();

    public CommandHandler() {
        commands.put("verify", new Verify());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;      // Ignore stinky bot spam

        Message msg = event.getMessage();
        String msgContent = msg.getContentRaw();
        var split = msgContent.split(" ");
        List<String> messageArgs = new ArrayList<>(Arrays.asList(split));
        GumServer gumGuild = event.isFromGuild() ? DynamoHelper.GetServer(event.getGuild()) : null;

        if (messageArgs.get(0).startsWith("?")) {
            GumCommand foundCommand = commands.get(messageArgs.get(0).substring(1).toLowerCase());
            messageArgs.remove(0);
            if (foundCommand != null){
                foundCommand.Invoke(msg, messageArgs.toArray(new String[0]), gumGuild);
                return;     // We will let the command handle deleting the message
            }
        }

        // If message is sent in bot only commands channel, and we didn't find a command for it
        if (event.getAuthor() != Main.jda.getSelfUser() && event.getChannelType().isGuild() && gumGuild.getGuildSettings().getCmdChannel() != null && event.getChannel().getIdLong() == gumGuild.getGuildSettings().getCmdChannel())
            event.getMessage().delete().submit();
    }

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
            event.getGuild().updateCommands().addCommands(
                            Commands.slash("verify", "Verifies a purchase using Gumroad License Key")
                                .addOptions(aliasOptions)
                                .addOption(OptionType.STRING, "key", "Gumroad License Key", true)).
                    queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.MISSING_ACCESS));
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
