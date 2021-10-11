package com.benaclejames.gumcord.Dynamo.TableTypes;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class GuildSettings {
    public Long CmdChannel = null;
    public Long AdminChannel = null;

    public GuildSettings() {}

    public GuildSettings(Map<String, Object> dynamoResult) {
        CmdChannel = dynamoResult.containsKey("CmdChannel") ? ((BigDecimal)dynamoResult.get("CmdChannel")).longValueExact() : null;
        AdminChannel = dynamoResult.containsKey("AdminChannel") ? ((BigDecimal)dynamoResult.get("AdminChannel")).longValueExact() : null;
    }
}

public class GumGuild {
    public static HashMap<Long, GuildSettings> CachedGuildSettings = new HashMap<>();

    private final GuildSettings settings;

    public Long getCmdChannel() {
        return settings.CmdChannel;
    }

    public Long getAdminChannel() {
        return settings.AdminChannel;
    }

    public GumGuild(long id) {
        // If we already have the guild settings cached
        if (CachedGuildSettings.containsKey(id)) {
            settings = CachedGuildSettings.get(id);
            return;
        }

        // We don't have settings cached, get them from dynamo and create a new GuildSettings object to store them
        Map<String, Object> dynamoResult = DynamoHelper.GetGuildSettings(id);
        if (dynamoResult == null) {     // Worst case scenario, we get null values and create an empty GuildSettings
            settings = new GuildSettings();
            return;
        }

        settings = new GuildSettings(dynamoResult);
        CachedGuildSettings.put(id, settings);
    }
}
