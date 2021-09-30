package Utils;

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
    public static Boolean GetLicenseValid(String productPermalink, String license)
            throws IOException {
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpPost post = new HttpPost("https://api.gumroad.com/v2/licenses/verify");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("product_permalink", productPermalink));
        params.add(new BasicNameValuePair("license_key", license));
        params.add(new BasicNameValuePair("increment_uses_count", "false"));
        post.setEntity(new UrlEncodedFormEntity(params));

        CloseableHttpResponse resp = client.execute(post);
        if (resp.getStatusLine().getStatusCode() != 200) return false;

        BufferedReader rdr = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
        StringBuilder resultStr = new StringBuilder();
        String strBuf;
        while ((strBuf = rdr.readLine()) != null)
            resultStr.append(strBuf);

        client.close();

        ObjectMapper mapper = new ObjectMapper();
        GumRoadResponse gumRoadResponse = mapper.readValue(resultStr.toString(), GumRoadResponse.class);

        return gumRoadResponse.success && !gumRoadResponse.purchase.refunded;
    }
}
