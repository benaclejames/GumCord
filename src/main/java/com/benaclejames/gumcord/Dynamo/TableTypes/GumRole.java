package com.benaclejames.gumcord.Dynamo.TableTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GumRole {
    public Long RoleId;
    public Long[] RoleIds;
    public Long MaxKeyAge;
    public String OODAdditionalInfo;
    @JsonIgnore
    public Role[] RoleLiterals;


    public void attachGuildLiteral(Guild guildLiteral) {
        // Attempt to find the role in the guild
        RoleLiterals = Arrays.stream(RoleIds).map(guildLiteral::getRoleById).toArray(Role[]::new);
    }

    public Long getRoleId() {return RoleId;}
    public void setRoleId(Long roleId) {RoleId = roleId;}

    public Long[] getRoleIds() {return RoleIds;}
    public void setRoleIds(Long[] roleIds) {RoleIds = roleIds;}

    public Long getMaxKeyAge() {return MaxKeyAge;}
    public void setMaxKeyAge(Long maxKeyAge) {MaxKeyAge = maxKeyAge;}

    public String getOODAdditionalInfo() {return OODAdditionalInfo;}
    public void setOODAdditionalInfo(String oodAdditionalInfo) {OODAdditionalInfo = oodAdditionalInfo;}
}