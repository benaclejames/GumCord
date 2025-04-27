package com.benaclejames.gumcord.dynamo;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.benaclejames.gumcord.dynamo.TableTypes.GumPurchase;
import com.benaclejames.gumcord.dynamo.TableTypes.GumServer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.Map;

/**
 * Main endpoint for DynamoDB
 */
public final class DynamoHelper {
    private static final AmazonDynamoDB dynamo = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
    private static final DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

    private DynamoHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static GumServer createServer(Guild guildLiteral) {
        GumServer server = new GumServer(guildLiteral);
        mapper.save(server);
        return server;
    }

    public static GumPurchase createPurchase(String product, User user, String key) {
        GumPurchase purchase = new GumPurchase(product, user);
        purchase.setKey(key);
        mapper.save(purchase);
        return purchase;
    }

    public static GumServer getServer(Guild guildLiteral) {
        GumServer server = new GumServer(guildLiteral);
        DynamoDBQueryExpression<GumServer> queryExpression = new DynamoDBQueryExpression<GumServer>()
                .withHashKeyValues(server);

        var queryResponse = mapper.query(GumServer.class, queryExpression);
        GumServer foundServer = !queryResponse.isEmpty() ? queryResponse.get(0) : createServer(guildLiteral);

        if (foundServer != null) {
            foundServer.attachGuildLiteral(guildLiteral);
        }

        return foundServer;
    }

    public static GumPurchase getPurchase(String product, User user) {
        GumPurchase purchase = new GumPurchase(product, user);
        DynamoDBQueryExpression<GumPurchase> queryExpression = new DynamoDBQueryExpression<GumPurchase>()
                .withHashKeyValues(purchase);

        var queryResponse = mapper.query(GumPurchase.class, queryExpression);
        // If we don't have a purchase, return null. We never want to automatically create one unless
        // we can verify it beforehand
        return !queryResponse.isEmpty() ? queryResponse.get(0) : null;
    }

    public static GumPurchase getPurchaseByKey(String key) {
        DynamoDBQueryExpression<GumPurchase> queryExpression = new DynamoDBQueryExpression<GumPurchase>()
                .withIndexName("Key-index") // Specify the secondary index name
                .withConsistentRead(false) // Set to true if you need a consistent read
                .withKeyConditionExpression("#attr = :key") // Use expression attribute name
                .withExpressionAttributeNames(Map.of("#attr", "Key")) // Map reserved keyword to expression attribute name
                .withExpressionAttributeValues(Map.of(":key", new AttributeValue().withS(key)));

        var queryResponse = mapper.query(GumPurchase.class, queryExpression);
        // If we don't have a purchase, return null. We never want to automatically create one unless
        // we can verify it beforehand

        return !queryResponse.isEmpty() ? queryResponse.get(0) : null;
    }


    public static void saveServer(GumServer server) {
        mapper.save(server);
    }
}
