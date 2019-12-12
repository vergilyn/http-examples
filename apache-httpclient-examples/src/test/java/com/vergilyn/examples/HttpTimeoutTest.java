package com.vergilyn.examples;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

/**
 * @date 2019/2/20
 */
@Slf4j
public class HttpTimeoutTest extends AbstractHttpClientTestng {
    @Override
    protected AbstractHttpClientTestng.HttpClientConfig getConfig() {
        return new AbstractHttpClientTestng.HttpClientConfig(10000, 5000, 1000, 10, 10);
    }

    @Test(invocationCount = 20, threadPoolSize = 5)
    public void timeout() {
        requestCache(ROUTE_BAIDU);
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
            log.error(e.getMessage(), e);
        }
        return -1L;
    }
}
