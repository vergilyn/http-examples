package com.vergilyn.examples;

import java.io.InputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.testng.annotations.Test;

/**
 * 验证：调用`httpClient.close()`后，正在传输数据的连接是否会异常。
 * <p>
 *   结果：会导致传输提早关闭。
 *   但异常描述以为是connection close/shutdown，但实际是o.a.h.TruncatedChunkException: Truncated chunk ( expected size: x; actual size: x) 数据丢包。
 * </p>
 */
public class HttpClientCloseTest extends AbstractHttpClientTestng {

    @Override
    protected HttpClientConfig getConfig() {
        return new HttpClientConfig(-1, -1, -1, 1, 1);
    }

    @Test
    public void validate(){
        HttpGet httpGet = new HttpGet(ROUTE_BAIDU);
        InputStream inputStream = null;
        int count = 0;
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            byte[] buffer = new byte[1024];

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // org.apache.http.ConnectionClosedException: Premature end of chunk coded message body: closing chunk expected
            // httpClient.close();

            inputStream = response.getEntity().getContent();
            int len;
            while (-1 != (len = inputStream.read(buffer))) {
                System.out.write(buffer, 0, len);
                count += len;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // org.apache.http.TruncatedChunkException: Truncated chunk ( expected size: xxxx; actual size: xxxx)
        } finally {
            safeCloseResponse(inputStream);
            System.out.println("\r\ncount >>>> " + count);
        }
    }
}
