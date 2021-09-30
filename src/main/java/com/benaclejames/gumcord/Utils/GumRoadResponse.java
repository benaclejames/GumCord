package com.benaclejames.gumcord.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class GumRoadResponse {
    public Boolean success;
    public int uses;
    public PurchaseData purchase;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class PurchaseData {
    public Boolean refunded;
}
