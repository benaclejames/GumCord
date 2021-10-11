package com.benaclejames.gumcord.Dynamo.TableTypes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

public class GumRole {
    public Long RoleId;
    public Long MaxKeyAge;

    public GumRole(LinkedHashMap<String, BigDecimal> tableMap) {
        RoleId = tableMap.get("RoleId").longValueExact();
        MaxKeyAge = tableMap.containsKey("MaxKeyAge") ? tableMap.get("MaxKeyAge").longValueExact() : null;
    }
}