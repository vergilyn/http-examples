package com.vergilyn.examples;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.testng.annotations.Test;

/**
 * 是否需要手动关闭：HttpGet.close()、HttpResponse.close()、HttpClient.close()？
 * <p>
 *   1. HttpResponse.close()需要，其实质是InputStream，使用完最好close。
 *   2. HttpGet.close()需要，并不会真正的关闭连接，而是交还给连接池。
 *   3. HttpClient.close() == connManager.shutdown()，系统关闭时需要。会立即断开当前正在执行数据传输的连接或可复用的连接，且连接池不可再使用。
 * </p>
 */
@Slf4j
public class HttpCloseTest extends AbstractHttpClientTestng {

    @Override
    protected HttpClientConfig getConfig() {
        return new HttpClientConfig(-1, -1, -1, 1, 3);
    }

    /**
     * <a href="http://hc.apache.org/httpcomponents-client-4.5.x/examples.html">examples</a>
     */
    @Test(invocationCount = 1, threadPoolSize = 5)
    public void releasedConn(){
        HttpGet method = null;
        try {
            method = new HttpGet(ROUTE_BAIDU);
            HttpResponse response = httpClient.execute(method);

            // 内部会调用: entity.getContent().close();
            // EntityUtils.toString(response.getEntity(), Charset.defaultCharset());

            // 未调用 entity.getContent().close()，所以需要自己关闭。
            String body = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

            System.out.println("responseBody >>>> " + body);

        } catch (IOException e) {
            log.error(e.getMessage(), e);

        }finally{
            method.releaseConnection(); // 把当前连接交还给conn-manager
            try {
                httpClient.close(); // Connection pool shut down!
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
