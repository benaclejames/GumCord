import Commands.GumCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class CommandHandler extends ListenerAdapter {

    private final HashMap<String, GumCommand> commands = new HashMap<>();

    public CommandHandler() {
        commands.put("verify", new Commands.Verify());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message msg = event.getMessage();
        String msgContent = msg.getContentRaw();
        List<String> messageArgs = new ArrayList<>(List.of(msgContent.split(" ")));

        if (!messageArgs.get(0).startsWith("?")) return;

        GumCommand foundCommand = commands.get(messageArgs.get(0).substring(1).toLowerCase());
        messageArgs.remove(0);
        if (foundCommand != null)
            foundCommand.Invoke(msg, messageArgs.toArray(new String[0]));
    }
}
