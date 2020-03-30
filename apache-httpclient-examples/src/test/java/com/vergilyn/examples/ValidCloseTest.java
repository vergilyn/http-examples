package com.vergilyn.examples;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 验证：method.close()是关闭连接，还是把连接返回给conn-manager。
 * <p>
 *   response、method的区别：正常情况下都是一样的，调用的是ConnectionHolder#
 * </p>
 */
@Slf4j
public class ValidCloseTest extends AbstractHttpClientTestng {
    @Override
    protected HttpClientConfig getConfig() {
        return new HttpClientConfig(-1, -1, -1, 2, 2);
    }

    private HttpRequestBase method;
    private HttpResponse response;

    @BeforeMethod
    public void request(){
        try {
            method = new HttpGet(ROUTE_BAIDU);
            response = httpClient.execute(method);
            // ...
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * connection可以被复用，且conn-pool正常
     * 若不支持复用，则会关闭此连接；否则，保持连接状态，并交由conn-pool管理。
     * <ol>
     *     <li>ResponseEntityProxy#streamClosed(java.io.InputStream)</li>
     * </ol>
     */
    @Test(singleThreaded = true, invocationCount = 2)
    public void closeContent(){
        try {
            response.getEntity().getContent().close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * connection关闭不可复用，但conn-pool未shutdown。
     * <ol>
     *   <li>ConnectionHolder#releaseConnection(boolean)</li>
     *   <li>BHttpConnectionBase#close()</li>
     * </ol>
     */
    @Test(singleThreaded = true, invocationCount = 2)
    public void closeResponse() {
        try {
            ((CloseableHttpResponse)response).close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * connection关闭不可复用，但conn-pool未shutdown。
     * <ol>
     *   <li>AbstractExecutionAwareRequest#reset()</li>
     *   <li>ConnectionHolder#abortConnection()</li>
     * </ol>
     */
    @Test(singleThreaded = true, invocationCount = 2)
    public void releaseConnection() {
        // 等价于  method.reset();
        method.releaseConnection();
    }

    /**
     * 会关闭conn-pool中正在执行，或可复用的所有长连接，并且conn-pool shutdown。
     */
    @Test(singleThreaded = true, invocationCount = 2)
    public void closeClient() {
        try {
            httpClient.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {

        System.out.println();

    }
}
