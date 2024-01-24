package com.benaclejames.gumcord.Dynamo.TableTypes;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

@DynamoDBTable(tableName = "Gumcord-Guilds")
public class GumServer {
    @DynamoDBIgnore
    public Guild guild;
    private Long GuildId;
    private Map<String, String> Aliases = new HashMap<>();
    private Map<String, GumRole> Roles = new HashMap<>();

    public GumServer(Guild guildLiteral) {guild = guildLiteral; GuildId = guild.getIdLong();}
    public GumServer(){}

    @DynamoDBIgnore
    public void attachGuildLiteral(Guild guildLiteral) {
        guild = guildLiteral;

        // Attach the guild to each role
        for (GumRole role : Roles.values()) {
            role.attachGuildLiteral(guildLiteral);
        }
    }

    @DynamoDBHashKey(attributeName = "GuildId")
    public Long getGuildId() {return GuildId;}
    public void setGuildId(Long discordId) {GuildId = discordId;}

    @DynamoDBAttribute(attributeName = "Aliases")
    public Map<String, String> getAliases() {return Aliases;}
    public void setAliases(Map<String, String> aliases) {Aliases = aliases;}

    @DynamoDBTypeConverted(converter = GumRolesConverter.class)
    @DynamoDBAttribute(attributeName = "Roles")
    public Map<String, GumRole> getRoles() {return Roles;}
    public void setRoles(Map<String, GumRole> roles) {Roles = roles;}

    static public class GumRolesConverter implements DynamoDBTypeConverter<Map<String, Map<String, AttributeValue>>, Map<String, GumRole>> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public Map<String, Map<String, AttributeValue>> convert(Map<String, GumRole> stringGumRoleMap) {
            mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

            Map<String, Map<String, AttributeValue>> returnMap = new HashMap<>();
            for (Map.Entry<String, GumRole> entry : stringGumRoleMap.entrySet()) {
                returnMap.put(entry.getKey(), ItemUtils.toAttributeValues(Item.fromJSON(mapper.valueToTree(entry.getValue()).toString())));
            }
            return returnMap;
        }

        @Override
        public Map<String, GumRole> unconvert(Map<String, Map<String, AttributeValue>> stringMapMap) {
            Map<String, GumRole> result = new HashMap<>();
            for (String key : stringMapMap.keySet()) {
                GumRole role;
                try {
                    Map<String, AttributeValue> values = stringMapMap.get(key);
                    ObjectMapper mapper = new ObjectMapper();
                    String item = ItemUtils.toItem(values).toJSON();
                    JsonNode jsonNode = mapper.readTree(item);
                    role = mapper.convertValue(jsonNode, GumRole.class);
                    result.put(key, role);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }
}
