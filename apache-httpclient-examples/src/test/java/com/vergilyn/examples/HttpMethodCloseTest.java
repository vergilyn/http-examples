package com.vergilyn.examples;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.execchain.MainClientExec;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 验证：method.close()是关闭连接，还是把连接返回给conn-manager。
 * <p>
 *   response、method的区别：正常情况下都是一样的，调用的是ConnectionHolder#
 * </p>
 */
@Slf4j
public class HttpMethodCloseTest extends AbstractHttpClientTestng {
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
            ((CloseableHttpResponse) response).close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * connection关闭不可复用，但conn-pool未shutdown。
     * <ol>
     *   <li>ConnectionHolder#cancel()</li>
     *   <li>ConnectionHolder#abortConnection(): 终止连接</li>
     * </ol>
     *
     * <p>
     *   备注：{@linkplain MainClientExec#execute(org.apache.http.conn.routing.HttpRoute, org.apache.http.client.methods.HttpRequestWrapper, org.apache.http.client.protocol.HttpClientContext, org.apache.http.client.methods.HttpExecutionAware) MainClientExec#execute()}
     * 可知Cancellable可能是ConnectionHolder，在异常情况下还可能是ConnectionRequest
     * </p>
     */
    @Test(singleThreaded = true, invocationCount = 2)
    public void releaseConnection() {
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
}
