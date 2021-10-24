package com.benaclejames.gumcord.Utils;

import com.benaclejames.gumcord.Dynamo.DynamoHelper;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

public class GumGuildUtility {
    public static HashMap<Long, GumGuild> CachedGuilds = new HashMap<>();

    public static GumGuild GetGumGuild(Guild guild) {
        long id = guild.getIdLong();

        // If we already have the guild settings cached
        if (CachedGuilds.containsKey(id))
            return CachedGuilds.get(id);

        // We don't have settings cached, get them from dynamo and create a new GuildSettings object to store them
        Map<String, Object> dynamoResult = DynamoHelper.GetGuildSettings(id);
        if (dynamoResult == null)     // Worst case scenario, we get null values and create an empty GuildSettings
            return new GumGuild();

        GumGuild newGuild = new GumGuild(dynamoResult, guild);
        CachedGuilds.put(id, newGuild);

        return newGuild;
    }
}
