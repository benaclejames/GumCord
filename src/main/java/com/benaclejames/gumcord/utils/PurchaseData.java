package com.benaclejames.gumcord.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseData {
    public String seller_id;
    public String product_id;
    public String product_name;
    public String permalink;
    public String product_permalink;
    public String short_product_id;
    public String email;
    public Number price;
    public Number gumroad_fee;
    public String currency;
    public Boolean discover_fee_charged;
    public Boolean can_contact;
    public String referrer;
    public GumroadCard card;
    public Number order_number;
    public String sale_id;
    public String sale_timestamp;
    public Boolean refunded = true;
    public List<String> custom_fields;
    public String license_key;
    public String ip_country;
}

