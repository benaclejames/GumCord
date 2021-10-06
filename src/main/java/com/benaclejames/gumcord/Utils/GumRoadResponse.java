package com.benaclejames.gumcord.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Duration;
import java.time.LocalDateTime;

public class GumRoadResponse {
    public Boolean success;
    public int uses;
    public PurchaseData purchase;

    public boolean IsValid() {
        return success && !purchase.refunded;
    }

    public boolean ExceedsTimestamp(long maxTime) {
        LocalDateTime time = LocalDateTime.parse(purchase.sale_timestamp.substring(0, purchase.sale_timestamp.length()-1));
        LocalDateTime currTime = LocalDateTime.now();
        Duration diff = Duration.between(time, currTime);
        long hours = (diff.getSeconds()/3600);
        return hours > maxTime;
    }

    public GumRoadResponse() {}
}

@JsonIgnoreProperties(ignoreUnknown = true)
class PurchaseData {
    public Boolean refunded = true;
    public String sale_timestamp;
}
