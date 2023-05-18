package com.benaclejames.gumcord.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main endpoint for interfacing with GumRoad
 */
public class GumRoad {

    public static GumRoadResponse GetLicense(String productPermalink, String license) {
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpPost post = new HttpPost("https://api.gumroad.com/v2/licenses/verify");

        List<NameValuePair> params = new ArrayList<>();
        // If the permalink ends with "==", then its actually a product_id and we should use that instead
        params.add(new BasicNameValuePair(productPermalink.endsWith("==") ? "product_id" : "product_permalink", productPermalink));
        params.add(new BasicNameValuePair("license_key", license));
        params.add(new BasicNameValuePair("increment_uses_count", "false"));
        try {
            post.setEntity(new UrlEncodedFormEntity(params));


        CloseableHttpResponse resp = client.execute(post);
        if (resp.getStatusLine().getStatusCode() != 200) return new GumRoadResponse();

        BufferedReader rdr = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
        StringBuilder resultStr = new StringBuilder();
        String strBuf;
        while ((strBuf = rdr.readLine()) != null)
            resultStr.append(strBuf);

        client.close();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(resultStr.toString(), GumRoadResponse.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new GumRoadResponse();
    }
}
