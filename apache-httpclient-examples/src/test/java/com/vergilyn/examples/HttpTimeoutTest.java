package com.vergilyn.examples;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

/**
 * @date 2019/2/20
 */
public class HttpTimeoutTest extends AbstractHttpClientTestng {
    private static final String BAIDU_URL = "http://www.baidu.com";
    private static final String QQ_URL = "http://www.qq.com";

    @Override
    protected AbstractHttpClientTestng.HttpClientConfig getConfig() {
        return new AbstractHttpClientTestng.HttpClientConfig(10000, 5000, 1000, 10, 10);
    }

    @Test(invocationCount = 20, threadPoolSize = 5)
    public void timeout() {
        requestCache(BAIDU_URL);
    }

    public long requestCache(String url){
        HttpGet httpGet = new HttpGet(url);
        try{
            long time = System.currentTimeMillis();
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String resultStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println("request-cache >>>> " + url + " >>>> " + (System.currentTimeMillis() - time));
            return System.currentTimeMillis() - time;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
        }
        return -1L;
    }

    public long request(String url){
        HttpGet httpGet = new HttpGet(url);
        try{
            long time = System.currentTimeMillis();
            CloseableHttpResponse response = HttpClients.createDefault().execute(httpGet);
            String resultStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println("request >>>> " + url + " >>>> " + (System.currentTimeMillis() - time));
            return System.currentTimeMillis() - time;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1L;
    }
}
