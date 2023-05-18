package com.benaclejames.gumcord.Dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.benaclejames.gumcord.Dynamo.TableTypes.GumServer;
import net.dv8tion.jda.api.entities.Guild;

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

    public static void SaveServer(GumServer server) {
        mapper.save(server);
    }
}
