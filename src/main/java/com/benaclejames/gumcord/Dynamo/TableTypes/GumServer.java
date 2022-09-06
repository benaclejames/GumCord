package com.benaclejames.gumcord.Dynamo.TableTypes;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.benaclejames.gumcord.Utils.AdminChannel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

@DynamoDBTable(tableName = "GumCord")
public class GumServer {
    public Guild guild;
    private Long DiscordId;
    private Map<String, String> Aliases;
    private GuildSettings GuildSettings;
    private Map<String, GumRole> Roles;
    private Map<String, TokenList> PendingTokens;
    private Map<String, TokenList> UsedTokens;

    public GumServer(Guild guildLiteral) {guild = guildLiteral; DiscordId = guild.getIdLong();}
    public GumServer(){}

    @DynamoDBIgnore
    public AdminChannel getAdminChannel() {
        return new AdminChannel(guild, guild.getTextChannelById(GuildSettings.AdminChannel));
    }

    @DynamoDBHashKey(attributeName = "DiscordId")
    public Long getDiscordId() {return DiscordId;}
    public void setDiscordId(Long discordId) {DiscordId = discordId;}

    @DynamoDBAttribute(attributeName = "Aliases")
    public Map<String, String> getAliases() {return Aliases;}
    public void setAliases(Map<String, String> aliases) {Aliases = aliases;}

    @DynamoDBTypeConverted(converter = GuildSettingsConverter.class)
    @DynamoDBAttribute(attributeName = "GuildSettings")
    public GuildSettings getGuildSettings() {return GuildSettings;}
    public void setGuildSettings(GuildSettings guildSettings) {GuildSettings = guildSettings;}

    @DynamoDBTypeConverted(converter = GumRolesConverter.class)
    @DynamoDBAttribute(attributeName = "Roles")
    public Map<String, GumRole> getRoles() {return Roles;}
    public void setRoles(Map<String, GumRole> roles) {Roles = roles;}

    @DynamoDBTypeConverted(converter = TokenListConverter.class)
    @DynamoDBAttribute(attributeName = "PendingTokens")
    public Map<String, TokenList> getPendingTokens() {return PendingTokens;}
    public void setPendingTokens(Map<String, TokenList> pendingTokens) {PendingTokens = pendingTokens;}

    @DynamoDBTypeConverted(converter = TokenListConverter.class)
    @DynamoDBAttribute(attributeName = "UsedTokens")
    public Map<String, TokenList> getUsedTokens() {return UsedTokens;}
    public void setUsedTokens(Map<String, TokenList> usedTokens) {UsedTokens = usedTokens;}

    static public class TokenListConverter implements DynamoDBTypeConverter<Map<String, Map<String, Long>>, Map<String, TokenList>> {

        @Override
        public Map<String, Map<String, Long>> convert(Map<String, TokenList> tokenList) {
            Map<String, Map<String, Long>> result = new HashMap<>();
            for (Map.Entry<String, TokenList> entry : tokenList.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getTokens());
            }
            return result;
        }

        @Override
        public Map<String, TokenList> unconvert(Map<String, Map<String, Long>> stringMapMap) {
            Map<String, TokenList> tokenListMap = new HashMap<>();
            for (Map.Entry<String, Map<String, Long>> entry : stringMapMap.entrySet()) {
                TokenList tokenList = new TokenList();
                tokenList.setId(entry.getKey());
                tokenList.setTokens(entry.getValue());
                tokenListMap.put(entry.getKey(), tokenList);
            }
            return tokenListMap;
        }
    }

    static public class GumRolesConverter implements DynamoDBTypeConverter<Map<String, Map<String, AttributeValue>>, Map<String, GumRole>> {
        private ObjectMapper mapper = new ObjectMapper();

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
                GumRole role = new GumRole();
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

    static public class GuildSettingsConverter implements DynamoDBTypeConverter<Map<String, AttributeValue>, GuildSettings> {

        @Override
        public Map<String, AttributeValue> convert(GuildSettings object) {
            ObjectMapper mapper = new ObjectMapper();

            Item item = new Item()
                    .withNumber("AdminChannel", object.getAdminChannel())
                    .withNumber("CmdChannel", object.getCmdChannel())
                    .withString("OODAdditionalInfo", object.getOODAdditionalInfo());

            return ItemUtils.toAttributeValues(item);
        }

        @Override
        public GuildSettings unconvert(Map<String, AttributeValue> object) {
            GuildSettings settings = new GuildSettings();
            try {
                ObjectMapper mapper = new ObjectMapper();
                String item = ItemUtils.toItem(object).toJSON();
                JsonNode jsonNode = mapper.readTree(item);
                settings = mapper.convertValue(jsonNode, GuildSettings.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return settings;
        }
    }
}
