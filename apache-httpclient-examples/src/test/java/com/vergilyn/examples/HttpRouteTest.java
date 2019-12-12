package com.vergilyn.examples;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.testng.annotations.Test;

/**
 * `maxConnPerRoute`、`maxConnTotal`衍生的问题：
 * <pre>
 *   maxConnTotal：整个连接池的大小
 *   maxConnPerRoute：是单个路由连接的最大数
 *
 *   比如，maxConnTotal =200，maxConnPerRoute =100，那么，如果只有一个路由的话，那么最大连接数也就是100了；
 *   如果有2个路由的话，那么它们分别最大的连接数是100，总数不能超过200。
 *
 * 验证 >>>>
 *   若有4个route，maxConnPerRoute=1，maxConnTotal=3。
 *   假设连接池中各个route的连接数为，route-qq=1，route-baidu=1，route-sina=1。
 *   此时访问route-cnblogs，会获取连接吗？
 *
 * 结果 >>>> 能获取连接并正确访问
 * </pre>
 * @date 2019/2/22
 */
@Slf4j
public class HttpRouteTest extends AbstractHttpClientTestng {

    @Override
    protected HttpClientConfig getConfig() {
        return new HttpClientConfig(-1, -1, -1, 1, 3);
    }

    @Test
    public void route(){
        for (int i = 0; i < getConfig().getMaxConnPerRoute(); i++){
            System.out.println("seq >>> " + i);
            request(ROUTE_QQ);
            request(ROUTE_BAIDU);
            request(ROUTE_SINA);
            System.out.println();
        }

        request(ROUTE_CNBLOGS);
    }

    private void request(String url){
        HttpGet httpGet = new HttpGet(url);
        try{
            CloseableHttpResponse response = httpClient.execute(httpGet);
            System.out.println("request >>>> " + url + " >>>> " + response.getHeaders("Connection")[0].getValue());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            httpGet.releaseConnection();
        }
    }
}
