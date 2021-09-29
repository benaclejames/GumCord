package Commands;

import net.dv8tion.jda.api.entities.Message;

public interface GumCommand {
    void Invoke(Message msg, String[] commandArgs);
}
