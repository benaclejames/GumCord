package com.benaclejames.gumcord.Dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumPurchase;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.Map;

/**
 * Main endpoint for DynamoDB
 */
public final class DynamoHelper {
    private static final AmazonDynamoDB dynamo = AmazonDynamoDBClientBuilder.standard().withRegion("eu-west-2").build();
    private static final DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

    public static GumServer CreateServer(Guild guildLiteral) {
        GumServer server = new GumServer(guildLiteral);
        mapper.save(server);
        return server;
    }

    public static GumPurchase CreatePurchase(String product, User user, String key) {
        GumPurchase purchase = new GumPurchase(product, user);
        purchase.setKey(key);
        mapper.save(purchase);
        return purchase;
    }

    public static GumServer GetServer(Guild guildLiteral) {
        GumServer server = new GumServer(guildLiteral);
        DynamoDBQueryExpression<GumServer> queryExpression = new DynamoDBQueryExpression<GumServer>()
                .withHashKeyValues(server);

        var queryResponse = mapper.query(GumServer.class, queryExpression);
        GumServer foundServer = queryResponse.size() > 0 ? queryResponse.get(0) : CreateServer(guildLiteral);

        if (foundServer != null) {
            foundServer.attachGuildLiteral(guildLiteral);
        }

        return foundServer;
    }

    public static GumPurchase GetPurchase(String product, User user) {
        GumPurchase purchase = new GumPurchase(product, user);
        DynamoDBQueryExpression<GumPurchase> queryExpression = new DynamoDBQueryExpression<GumPurchase>()
                .withHashKeyValues(purchase);

        var queryResponse = mapper.query(GumPurchase.class, queryExpression);
        // If we don't have a purchase, return null. We never want to automatically create one unless
        // we can verify it beforehand
        GumPurchase foundPurchase = queryResponse.size() > 0 ? queryResponse.get(0) : null;

        return foundPurchase;
    }

    public static GumPurchase GetPurchaseByKey(String key) {
        DynamoDBQueryExpression<GumPurchase> queryExpression = new DynamoDBQueryExpression<GumPurchase>()
                .withIndexName("Key-index") // Specify the secondary index name
                .withConsistentRead(false) // Set to true if you need a consistent read
                .withKeyConditionExpression("#attr = :key") // Use expression attribute name
                .withExpressionAttributeNames(Map.of("#attr", "Key")) // Map reserved keyword to expression attribute name
                .withExpressionAttributeValues(Map.of(":key", new AttributeValue().withS(key)));

        var queryResponse = mapper.query(GumPurchase.class, queryExpression);
        // If we don't have a purchase, return null. We never want to automatically create one unless
        // we can verify it beforehand
        GumPurchase foundPurchase = queryResponse.size() > 0 ? queryResponse.get(0) : null;

        return foundPurchase;
    }


    public static void SaveServer(GumServer server) {
        mapper.save(server);
    }
}
