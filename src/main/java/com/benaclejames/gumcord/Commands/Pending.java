package com.benaclejames.gumcord.Commands;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Dynamo.TableTypes.TokenList;
import com.benaclejames.gumcord.Utils.GumGuild;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;

public class Pending implements GumCommand {

    @Override
    public void Invoke(Message msg, String[] commandArgs, GumGuild guild) {
        // Check the message was sent in the admin channel
        if (guild.getAdminChannel().channelLiteral != msg.getTextChannel() || commandArgs.length != 1) return;

        // Get gumroad literal
        String gumroadId = DynamoHelper.GetGumroadIdFromAlias(guild.guild.getIdLong(), commandArgs[0]);
        if (gumroadId == null)
            gumroadId = commandArgs[0];

        // Get all pending tokens
        TokenList pendingTokens = guild.GetTokenList(gumroadId, "PendingTokens");
        HashMap<String, Long> allPendingTokens = pendingTokens.GetAllItems();

        for (String key : allPendingTokens.keySet()) {

        }
    }
}
