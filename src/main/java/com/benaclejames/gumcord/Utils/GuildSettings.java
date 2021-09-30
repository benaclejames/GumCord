package com.benaclejames.gumcord.Utils;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import net.dv8tion.jda.api.entities.Guild;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class GuildSettings {
    public static HashMap<Long, GuildSettings> CachedGuildSettings = new HashMap<>();

    private Long CmdChannel;

    public Long getCmdChannel() {
        return CmdChannel;
    }

    public void setCmdChannel(Long newCmdChannel) {
        //TODO: Upload to dynamo
        CmdChannel = newCmdChannel;
    }

    public GuildSettings(Map<String, Object> dynamoResult) {
        CmdChannel = dynamoResult.get("CmdChannel") == null ? null : ((BigDecimal)dynamoResult.get("CmdChannel")).longValueExact();
    }

    public static GuildSettings GetGuildSettings(Guild guild) {
        long guildId = guild.getIdLong();

        // If we have this guild cached
        if (CachedGuildSettings.get(guildId) != null)
            return CachedGuildSettings.get(guildId);

        Map<String, Object> dynamoResult = DynamoHelper.GetGuildSettings(guildId);
        if (dynamoResult == null) return null;

        GuildSettings newSettings = new GuildSettings(dynamoResult);
        CachedGuildSettings.put(guildId, newSettings);

        return newSettings;
    }
}
