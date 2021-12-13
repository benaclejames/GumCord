package com.benaclejames.gumcord.Dynamo.TableTypes;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

import static com.benaclejames.gumcord.Dynamo.DynamoHelper.table;

public class TokenList {
    private final HashMap<String, String> baseNameMap = new HashMap<>();
    private final Long serverId;

    public TokenList(String rootName, String gumroadId, Long serverId) {
        baseNameMap.put("#root", rootName);
        baseNameMap.put("#gum", gumroadId);
        this.serverId = serverId;
    }

    public void AppendToken(String token, long userId) {
        HashMap<String, String> tempRequestMap = (HashMap<String, String>) baseNameMap.clone();
        tempRequestMap.put("#tok", token);

        HashMap<String, Object> valueMap = new HashMap<>();
        valueMap.put(":u", userId);

        HashMap<String, Long> newMap = new HashMap<>();
        newMap.put(token, userId);

        try {
            table.updateItem("DiscordId", serverId, "set #root.#gum.#tok = :u", tempRequestMap, valueMap);
        } catch (AmazonDynamoDBException dbException) {
            if (Objects.equals(dbException.getErrorCode(), "ValidationException")) {
                tempRequestMap.remove("#tok");
                valueMap.remove(":u");
                valueMap.put(":newMap", newMap);
                table.updateItem("DiscordId", serverId, "set UsedTokens.#gum = :newMap", tempRequestMap, valueMap);
            }
        }
    }

    public Long GetTokenOwner(String token) {
        HashMap<String, String> tempRequestMap = (HashMap<String, String>) baseNameMap.clone();
        tempRequestMap.put("#token", token);

        Item dynamoResult = table.getItem("DiscordId", serverId, "#root.#gum.#token", tempRequestMap);

        if (dynamoResult.asMap().size() <= 0) return null;

        return ((LinkedHashMap<String, BigDecimal>)dynamoResult.getMap(tempRequestMap.get("#root")).get(tempRequestMap.get("#gum"))).get(token).longValueExact();
    }

    public HashMap<String, Long> GetAllItems() {
        HashMap<String, String> tempRequestMap = (HashMap<String, String>) baseNameMap.clone();

        Item dynamoResult = table.getItem("DiscordId", serverId, "#root.#gum", tempRequestMap);

        if (dynamoResult.asMap().size() <= 0) return null;

        return (LinkedHashMap<String, Long>)dynamoResult.getMap(tempRequestMap.get("#root")).get(tempRequestMap.get("#gum"));
    }
}
