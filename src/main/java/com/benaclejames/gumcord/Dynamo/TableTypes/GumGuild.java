package com.benaclejames.gumcord.Dynamo.TableTypes;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import com.benaclejames.gumcord.Utils.AdminChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class GuildSettings {
    public Long CmdChannel = null;
    public AdminChannel AdminChannel = null;

    public GuildSettings() {}

    public GuildSettings(Map<String, Object> dynamoResult, Guild guild) {
        CmdChannel = dynamoResult.containsKey("CmdChannel") ? ((BigDecimal)dynamoResult.get("CmdChannel")).longValueExact() : null;

        if (!dynamoResult.containsKey("AdminChannel")) return;

        long adminChannelId = ((BigDecimal)dynamoResult.get("AdminChannel")).longValueExact();
        MessageChannel channel = guild.getTextChannelById(adminChannelId);

        if (channel == null) return;

        AdminChannel = new AdminChannel(channel);
    }
}

public class GumGuild {
    public static HashMap<Long, GuildSettings> CachedGuildSettings = new HashMap<>();

    private final GuildSettings settings;

    public Long getCmdChannel() {
        return settings.CmdChannel;
    }

    public AdminChannel getAdminChannel() {
        return settings.AdminChannel;
    }

    public GumGuild(Guild guild) {
        long id = guild.getIdLong();

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

        settings = new GuildSettings(dynamoResult, guild);
        CachedGuildSettings.put(id, settings);
    }
}
