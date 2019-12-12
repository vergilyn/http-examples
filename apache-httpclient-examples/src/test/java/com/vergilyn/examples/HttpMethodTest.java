package com.vergilyn.examples;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 * @author VergiLyn
 * @date 2019-08-01
 */
@Slf4j
public class HttpMethodTest extends AbstractHttpClientTestng {

    @Test(threadPoolSize = 4, invocationCount = 4)
    public void postForm(){
        HttpPost post = new HttpPost("http://127.0.0.1:8080/store/assist.html");

        List<NameValuePair> params = Lists.newArrayList();
        params.add(new BasicNameValuePair("applyId", "2622"));
        params.add(new BasicNameValuePair("openId", "oLoVt0w6cCGhshaI_IUrEQ0KWpso"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
        post.setEntity(entity);

        try {
            CloseableHttpResponse execute = httpClient.execute(post);
            System.out.println(EntityUtils.toString(execute.getEntity()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }
}
