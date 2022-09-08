package com.benaclejames.gumcord.Dynamo.TableTypes;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.benaclejames.gumcord.Utils.AdminChannel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.dv8tion.jda.api.entities.Guild;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GuildSettings {
    public Long AdminChannel;
    public String OODAdditionalInfo;
    @DynamoDBIgnore
    public AdminChannel adminChannel;

    @DynamoDBIgnore
    public void attachGuildLiteral(Guild guildLiteral) {
        if (AdminChannel != null)
            adminChannel = new AdminChannel(guildLiteral, guildLiteral.getTextChannelById(AdminChannel));
    }

    public Long getAdminChannel() {return AdminChannel;}
    public void setAdminChannel(Long adminChannel) {AdminChannel = adminChannel;}

    public String getOODAdditionalInfo() {return OODAdditionalInfo;}
    public void setOODAdditionalInfo(String oodAdditionalInfo) {OODAdditionalInfo = oodAdditionalInfo;}
}
