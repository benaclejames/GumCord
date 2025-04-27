package com.benaclejames.gumcord.dynamo.TableTypes;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import net.dv8tion.jda.api.entities.User;

@DynamoDBTable(tableName = "Gumcord-Purchases")
public class GumPurchase {

    private String Product;
    private Long UserId;
    private String Key;

    public GumPurchase(String product, User user) {
        Product = product;
        UserId = user.getIdLong();
    }

    public GumPurchase() {
    }

    @DynamoDBHashKey(attributeName = "Product")
    public String getProduct() {return Product;}
    public void setProduct(String product) {Product = product;}

    @DynamoDBRangeKey(attributeName = "UserId")
    public Long getUserId() {return UserId;}
    public void setUserId(Long userId) {UserId = userId;}

    @DynamoDBAttribute(attributeName = "Key")
    public String getKey() {return Key;}
    public void setKey(String key) {Key = key;}
}
