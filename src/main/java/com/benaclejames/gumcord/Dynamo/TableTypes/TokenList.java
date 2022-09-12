package com.benaclejames.gumcord.Dynamo.TableTypes;

import java.util.HashMap;
import java.util.Map;

public class TokenList {

    private String Id;
    private Map<String, Long> Tokens = new HashMap<>();

    public Map<String, Long> getTokens() {return Tokens;}
    public void setTokens(Map<String, Long> pendingTokens) {
        Tokens = pendingTokens;}

    public String getId() {return Id;}
    public void setId(String id) {Id = id;}
}
