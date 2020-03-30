package com.vergilyn.examples;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 * @author VergiLyn
 * @date 2019-12-24
 */
@Slf4j
public class HttpGetTest extends AbstractHttpClientTestng {

    /**
     * 不仅限以下的URI的2种方式。
     * @see HttpGet#setParams(HttpParams) 已Deprecated，但始终没明白javadocs所说的替代方式
     */
    @Test(threadPoolSize = 1, invocationCount = 1)
    public void httpGetAddParameter(){
        String url = ProviderURLBuilder.urlHello();

        HttpGet httpGet = null;
        CloseableHttpResponse httpResponse = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            /*// URI方式一
            uriBuilder.addParameter("ms", "500");*/

            // URI方式二
            List<NameValuePair> params = Lists.newArrayList();
            params.add(new BasicNameValuePair("ms", "500"));
            uriBuilder.addParameters(params);

            httpGet = new HttpGet(uriBuilder.build());
            httpResponse = httpClient.execute(httpGet);

            System.out.println(EntityUtils.toString(httpResponse.getEntity()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (httpResponse != null){
                IOUtils.closeQuietly(httpResponse);
            }
        }
    }
}
