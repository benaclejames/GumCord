package com.benaclejames.gumcord.Commands;

import com.benaclejames.gumcord.Dynamo.TableTypes.GumGuild;
import net.dv8tion.jda.api.entities.Message;

public interface GumCommand {
    void Invoke(Message msg, String[] commandArgs, GumGuild guild);
}
