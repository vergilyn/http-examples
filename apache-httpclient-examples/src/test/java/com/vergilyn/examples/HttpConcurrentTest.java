package com.vergilyn.examples;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 * @author VergiLyn
 * @date 2019-08-01
 */
public class HttpConcurrentTest extends AbstractHttpClientTestng {

    @Test(dataProvider = "data", threadPoolSize = 10, invocationCount = 3)
    public void post(String millis){
        HttpPost post = new HttpPost(ProviderURLBuilder.urlHello());

        List<NameValuePair> params = Lists.newArrayList();
        params.add(new BasicNameValuePair("ms", millis));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
        post.setEntity(entity);

        try {
            CloseableHttpResponse execute = httpClient.execute(post);
            System.out.println(EntityUtils.toString(execute.getEntity()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @DataProvider(name = "data", parallel = true)
    public Object[][] data(){
        return new Object[][]{{1000}, {2000}, {30000}};
    }
}
