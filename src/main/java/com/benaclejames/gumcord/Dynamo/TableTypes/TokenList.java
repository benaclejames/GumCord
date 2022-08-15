package com.benaclejames.gumcord.Dynamo.TableTypes;

import java.util.Map;

public class TokenList {

    private String Id;
    private Map<String, Long> Tokens;

    public Map<String, Long> getTokens() {return Tokens;}
    public void setTokens(Map<String, Long> pendingTokens) {
        Tokens = pendingTokens;}

    public String getId() {return Id;}
    public void setId(String id) {Id = id;}
}
