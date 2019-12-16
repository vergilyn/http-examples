package com.vergilyn.examples;

import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.testng.annotations.Test;

/**
 * 验证：调用`httpClient.close()`后，正在传输数据的连接是否会异常？
 * <p>
 *   结果：会，因为`httpClient.close()`实际是 `connectManager.shutdown()`关闭连接池。
 *   ```
 *   08:37:40.443 [Thread-0] DEBUG org.apache.http.impl.conn.PoolingHttpClientConnectionManager - Connection manager is shutting down
 *   08:37:40.443 [Thread-0] DEBUG org.apache.http.impl.conn.DefaultManagedHttpClientConnection - http-outgoing-0: Close connection
 *   08:37:40.443 [Thread-0] DEBUG org.apache.http.impl.conn.PoolingHttpClientConnectionManager - Connection manager shut down
 *   08:37:42.385 [main] DEBUG org.apache.http.impl.execchain.MainClientExec - Connection discarded
 *   08:37:42.386 [main] DEBUG org.apache.http.impl.conn.PoolingHttpClientConnectionManager - Connection released: [id: 0][route: {}->http://www.baidu.com:80][total kept alive: 0; route allocated: 0 of 1; total allocated: 0 of 1]
 *   08:37:42.394 [main] ERROR com.vergilyn.examples.HttpClientCloseTest - Truncated chunk (expected size: 1,142; actual size: 1,034)
 *   ```
 *
 * </p>
 */
@Slf4j
public class ValidHttpClientCloseTest extends AbstractHttpClientTestng {

    @Override
    protected HttpClientConfig getConfig() {
        return new HttpClientConfig(-1, -1, -1, 1, 1);
    }

    @Test
    public void validate(){
        int count = 0;
        int len;
        byte[] buffer = new byte[1024];
        HttpGet httpGet = null;
        InputStream inputStream = null;
        try {
            httpGet = new HttpGet(ROUTE_BAIDU);
            CloseableHttpResponse response = httpClient.execute(httpGet);

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    httpClient.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }).start();

            // org.apache.http.ConnectionClosedException: Premature end of chunk coded message body: closing chunk expected
            // httpClient.close();

            inputStream = response.getEntity().getContent();

            while (-1 != (len = inputStream.read(buffer))) {
                System.out.write(buffer, 0, len);
                count += len;
                Thread.sleep(800);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // org.apache.http.TruncatedChunkException: Truncated chunk ( expected size: xxxx; actual size: xxxx)
        } finally {
            safeCloseResponse(inputStream);
            System.out.println("\r\ncount >>>> " + count);
        }
    }
}
