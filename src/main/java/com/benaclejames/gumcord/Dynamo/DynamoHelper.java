package com.benaclejames.gumcord.Dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumRole;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main endpoint for DynamoDB
 */
public final class DynamoHelper {
    private static final DynamoDB dynamo = new DynamoDB(AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").build());
    public static final Table table = dynamo.getTable("GumCord");

    public static GumRole GetGumroadRoleInfo(long serverId, String gumroadId) {
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#role", "Roles");
        nameMap.put("#gum", gumroadId);

        Item dynamoResult = table.getItem("DiscordId", serverId, "#role.#gum.RoleId, #role.#gum.MaxKeyAge, #role.#gum.OODAdditionalInfo", nameMap);

        if (dynamoResult.asMap().size() <= 0) return null;  // If no role was found

        return new GumRole((LinkedHashMap<String, Object>)dynamoResult.getMap("Roles").get(gumroadId));
    }

    public static String GetGumroadIdFromAlias(long serverId, String alias) {
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#a", "Aliases");
        nameMap.put("#aname", alias);

        Item dynamoResult = table.getItem("DiscordId", serverId, "#a.#aname", nameMap);

        if (dynamoResult.asMap().size() <= 0) return null;

        return (String)dynamoResult.getMap("Aliases").get(alias);
    }

    public static Map<String, Object> GetGuildSettings(long serverId) {
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#gs", "GuildSettings");

        Item dynamoResult = table.getItem("DiscordId", serverId, "#gs", nameMap);

        if (dynamoResult.asMap().size() <= 0) return null;
        return dynamoResult.getMap("GuildSettings");
    }
}
