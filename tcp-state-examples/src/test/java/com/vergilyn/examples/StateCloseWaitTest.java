package com.vergilyn.examples;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author VergiLyn
 * @date 2019-12-12
 */
@Slf4j
public class StateCloseWaitTest {
    private HttpClient httpClient;
    private long beginTimestamp;

    @Test
    public void httpClient() {
        try {
            HttpGet httpGet = new HttpGet(ProviderURLBuilder.urlHello());
            HttpResponse data = httpClient.execute(httpGet);

            System.out.println(EntityUtils.toString(data.getEntity()));

            // Thread.sleep(20000);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Test
    public void original() {
        try {
            java.net.URL url = new URL(ProviderURLBuilder.urlHello());

            //打开和url之间的连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("connection", "Keep-Alive");

            conn.connect();

            //获取URLConnection对象对应的输入流
            InputStream is = conn.getInputStream();
            //构造一个字符流缓存
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str;
            while ((str = br.readLine()) != null) {
                str = new String(str.getBytes(), StandardCharsets.UTF_8);//解决中文乱码问题
                System.out.println(str);
            }
            //关闭流
            is.close();
            //断开连接，最好写上，disconnect是在底层tcp socket链接空闲时才切断。如果正在被其他线程使用就不切断。
            //固定多线程的话，如果不disconnect，链接会增多，直到收发不出信息。写上disconnect后正常一些。
            conn.disconnect();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @BeforeTest
    public void before() {
        beginTimestamp = System.currentTimeMillis();
        httpClient = HttpClients.custom()
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(40)
                .build();
        log.info(">>>> invoke begin! <<<<");
    }

    @AfterTest
    public void after(){
        log.info(">>>> invoke finish, exec: {} ms <<<<", System.currentTimeMillis() - beginTimestamp);
    }
}
