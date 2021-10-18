package com.benaclejames.gumcord.Utils;

import com.benaclejames.gumcord.Dynamo.TableTypes.TokenList;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class GuildSettings {
    public Long CmdChannel = null;
    public com.benaclejames.gumcord.Utils.AdminChannel AdminChannel = null;

    public GuildSettings() {}

    public GuildSettings(Map<String, Object> dynamoResult, Guild guild) {
        // Get CMD Channel
        CmdChannel = dynamoResult.containsKey("CmdChannel") ? ((BigDecimal)dynamoResult.get("CmdChannel")).longValueExact() : null;

        // Get Admin Channel
        if (dynamoResult.containsKey("AdminChannel")) {
            long adminChannelId = ((BigDecimal) dynamoResult.get("AdminChannel")).longValueExact();
            MessageChannel channel = guild.getTextChannelById(adminChannelId);

            if (channel != null)
                AdminChannel = new AdminChannel(guild, channel);
        }
    }
}

public class GumGuild {
    private GuildSettings settings = new GuildSettings();
    public HashMap<String, HashMap<String, TokenList>> TokenLists = new HashMap<>();    // Embedded HashMap. GumRoadID -> TokenListType -> TokenList

    public TokenList GetTokenList(long guildId, String gumRoadId, String tokenListType) {
        if (!TokenLists.containsKey(gumRoadId))
            TokenLists.put(gumRoadId, new HashMap<>());

        if (!TokenLists.get(gumRoadId).containsKey(tokenListType))
            TokenLists.get(gumRoadId).put(tokenListType, new TokenList(tokenListType, gumRoadId, guildId));

        return TokenLists.get(gumRoadId).get(tokenListType);
    }

    public Long getCmdChannel() {
        return settings.CmdChannel;
    }

    public AdminChannel getAdminChannel() {
        return settings.AdminChannel;
    }

    public GumGuild() {}

    public GumGuild(Map<String, Object> dynamoResult, Guild guild) {
        settings = new GuildSettings(dynamoResult, guild);
    }
}