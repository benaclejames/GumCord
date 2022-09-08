package com.benaclejames.gumcord.Dynamo.TableTypes;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GumRole {
    public Long RoleId;
    public Long MaxKeyAge;
    public String OODAdditionalInfo;
    @JsonIgnore
    public Role RoleLiteral;


    public void attachGuildLiteral(Guild guildLiteral) {
        // Attempt to find the role in the guild
        RoleLiteral = guildLiteral.getRoleById(RoleId);
    }

    public Long getRoleId() {return RoleId;}
    public void setRoleId(Long roleId) {RoleId = roleId;}

    public Long getMaxKeyAge() {return MaxKeyAge;}
    public void setMaxKeyAge(Long maxKeyAge) {MaxKeyAge = maxKeyAge;}

    public String getOODAdditionalInfo() {return OODAdditionalInfo;}
    public void setOODAdditionalInfo(String oodAdditionalInfo) {OODAdditionalInfo = oodAdditionalInfo;}
}