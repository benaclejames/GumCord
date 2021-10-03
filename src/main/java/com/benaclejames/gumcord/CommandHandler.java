package com.benaclejames.gumcord;

import com.benaclejames.gumcord.Commands.GumCommand;
import com.benaclejames.gumcord.Commands.Verify;
import com.benaclejames.gumcord.Utils.GumGuild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;
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
        List<String> messageArgs = new ArrayList<>(List.of(msgContent.split(" ")));

        if (messageArgs.get(0).startsWith("?")) {
            GumCommand foundCommand = commands.get(messageArgs.get(0).substring(1).toLowerCase());
            messageArgs.remove(0);
            if (foundCommand != null){
                foundCommand.Invoke(msg, messageArgs.toArray(new String[0]));
                return;     // We will let the command handle deleting the message
            }
        }

        // If message is sent in bot only commands channel, and we didn't find a command for it
        if (event.getAuthor() != Main.jda.getSelfUser() && event.getChannel().getIdLong() == new GumGuild(event.getGuild().getIdLong()).getCmdChannel())
            event.getMessage().delete().submit();
    }
}
