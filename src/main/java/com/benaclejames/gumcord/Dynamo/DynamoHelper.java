package com.benaclejames.gumcord.Dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main endpoint for DynamoDB
 */
public final class DynamoHelper {
    private static final DynamoDB dynamo = new DynamoDB(AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").build());
    private static final Table table = dynamo.getTable("GumCord");

    public static GumRole GetGumroadRoleInfo(long serverId, String gumroadId) {
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#role", "Roles");
        nameMap.put("#gum", gumroadId);

        Item dynamoResult = table.getItem("DiscordId", serverId, "#role.#gum.RoleId", nameMap);

        if (dynamoResult.asMap().size() <= 0) return null;  // If no role was found

        return new GumRole((LinkedHashMap<String, BigDecimal>)dynamoResult.getMap("Roles").get(gumroadId));
    }

    public static String GetGumroadIdFromAlias(long serverId, String alias) {
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#a", "Aliases");
        nameMap.put("#aname", alias);

        Item dynamoResult = table.getItem("DiscordId", serverId, "#a.#aname", nameMap);

        if (dynamoResult.asMap().size() <= 0) return null;

        return (String)dynamoResult.getMap("Aliases").get(alias);
    }

    public static void AppendUsedToken(long serverId, String gumroadId, String token, long userId) {
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#gum", gumroadId);
        nameMap.put("#tok", token);

        HashMap<String, Object> valueMap = new HashMap<>();
        valueMap.put(":u", userId);

        HashMap<String, Long> newMap = new HashMap<>();
        newMap.put(token, userId);

        try {
            table.updateItem("DiscordId", serverId, "set UsedTokens.#gum.#tok = :u", nameMap, valueMap);
        } catch (AmazonDynamoDBException dbException) {
            if (Objects.equals(dbException.getErrorCode(), "ValidationException")) {
                nameMap.remove("#tok");
                valueMap.remove(":u");
                valueMap.put(":newMap", newMap);
                table.updateItem("DiscordId", serverId, "set UsedTokens.#gum = :newMap", nameMap, valueMap);
            }
        }
    }

    public static Long AlreadyUsedToken(long serverId, String gumroadId, String token) {
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#tok", token);
        nameMap.put("#gum", gumroadId);

        Item dynamoResult = table.getItem("DiscordId", serverId, "UsedTokens.#gum.#tok", nameMap);

        if (dynamoResult.asMap().size() <= 0) return null;

        BigDecimal returnedNum = (BigDecimal)((HashMap<String, Object>) dynamoResult.getMap("UsedTokens").get(gumroadId)).get(token);
        return returnedNum.longValueExact();
    }

    public static Map<String, Object> GetGuildSettings(long serverId) {
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#gs", "GuildSettings");

        Item dynamoResult = table.getItem("DiscordId", serverId, "#gs", nameMap);

        if (dynamoResult.asMap().size() <= 0) return null;
        return dynamoResult.getMap("GuildSettings");
    }
}
