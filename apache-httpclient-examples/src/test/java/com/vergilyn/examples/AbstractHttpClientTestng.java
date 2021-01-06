/**
 * Copyright (c)  2019 CQLIVING, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * CQLIVING, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CQLIVING.
 */

package com.vergilyn.examples;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

@Slf4j
public abstract class AbstractHttpClientTestng {
    protected HttpClientConfig getConfig(){
        return new HttpClientConfig(-1, -1, -1, 2, 5);
    }
    protected static final String ROUTE_QQ = "http://www.qq.com";
    protected static final String ROUTE_BAIDU = "http://www.baidu.com";
    protected static final String ROUTE_SINA = "http://www.sina.com";
    protected static final String ROUTE_CNBLOGS = "http://www.cnblogs.com";
    protected CloseableHttpClient httpClient = null;

    @BeforeTest
    public void before(){
        HttpClientConfig config = getConfig() == null ? this.getConfig() : getConfig();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(config.connRequestTimeout)
                .setConnectTimeout(config.connTimeout)
                .setSocketTimeout(config.socketTimeout)
                .setMaxRedirects(0)
                .build();

        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(config.maxConnPerRoute)
                .setMaxConnTotal(config.maxConnTotal)
                .setConnectionTimeToLive(10086, TimeUnit.SECONDS)

                //  可以看到在HttpClientBuilder进行build的时候，如果指定了开启清理功能，会创建一个连接池清理线程并运行它。
                // .evictIdleConnections(maxIdleTime, unit)     // 指定是否清理空闲连接
                // .evictExpiredConnections()   // 指定是否要清理过期连接，默认不启动清理线程(false)

                .build();

    }

    protected void printResponse(CloseableHttpResponse response) throws IOException {
        System.out.println(EntityUtils.toString(response.getEntity()));
    }

    @AfterTest
    public void after(){
        try {
            httpClient.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void safeCloseResponse(Closeable stream) {
        if (stream == null){
            return;
        }

        try {
            stream.close();
        } catch (IOException e) {
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    protected class HttpClientConfig{
        /** httpclient使用连接池来管理连接，这个时间就是从连接池获取连接的超时时间 */
        private int connRequestTimeout;
        /** 连接建立时间，即三次握手完成时间 */
        private int connTimeout;
        /** 连接建立开始传递数据后，数据包之间的最大等待间隔 */
        private int socketTimeout;
        /** 每一个路由的连接上限 */
        private int maxConnPerRoute;
        /** 连接池中连接上限 */
        private int maxConnTotal;

    }
}
