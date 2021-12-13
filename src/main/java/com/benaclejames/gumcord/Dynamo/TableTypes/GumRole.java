package com.benaclejames.gumcord.Dynamo.TableTypes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

public class GumRole {
    public Long RoleId;
    public Long MaxKeyAge;
    public String OODAdditionalInfo;

    public GumRole(LinkedHashMap<String, BigDecimal> tableMap) {
        RoleId = tableMap.get("RoleId").longValueExact();
        MaxKeyAge = tableMap.containsKey("MaxKeyAge") ? tableMap.get("MaxKeyAge").longValueExact() : null;
        OODAdditionalInfo = tableMap.containsKey("OODAdditionalInfo") ? tableMap.get("OODAdditionalInfo").toString() : null;
    }
}