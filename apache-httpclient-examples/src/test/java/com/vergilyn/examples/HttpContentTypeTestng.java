package com.vergilyn.examples;

import java.io.IOException;

import com.vergilyn.examples.controller.ContentTypeController;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

/**
 * @author vergilyn
 * @date 2020-03-30
 */
@Slf4j
public class HttpContentTypeTestng extends AbstractHttpClientTestng {

    @Test(description = "测试 spring MVC 是否支持自定义 contentType/contentLength，通过 `response.setContentType(...)`")
    public void modifyByResponseSetHeader(){
        String contentType = MediaType.TEXT_HTML_VALUE;
        Long contentLength = 409839163L;

        String url = ProviderURLBuilder.urlModifyByResponseSetHeader();
        HttpGet httpGet = new HttpGet(url);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            Header actualContentType = response.getFirstHeader("content-type");
            Header actualContentLength = response.getFirstHeader("content-length");

            System.out.printf("response >>>>> %s \r\n", EntityUtils.toString(response.getEntity()));
            System.out.printf("expect >>>> content-type: %s, content-length: %d \r\n", contentType, contentLength);
            System.out.printf("actual >>>> content-type: %s, content-length: %s",
                    actualContentType.getValue(),
                    actualContentLength == null ? "null" : actualContentLength.getName());

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /** except: image/jpeg, actual: image/jpeg
     *  但是 response-body 取不到正确的值...
     */
    @Test(description = "测试 spring MVC 是否支持自定义 contentType/contentLength，通过 `@RequestMapping(producers={})`")
    public void modifyByRequestMappingProduces(){

        String url = ProviderURLBuilder.urlModifyByRequestMappingProduces();
        HttpGet httpGet = new HttpGet(url);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            Header actualContentType = response.getFirstHeader("content-type");

            System.out.printf("response >>>>> %s \r\n", EntityUtils.toString(response.getEntity()));  // QUESTION 为什么是 null ？
            System.out.printf("expect >>>> content-type: %s \r\n", ContentTypeController.EXCEPT_CONTENT_TYPE);
            System.out.printf("actual >>>> content-type: %s", actualContentType.getValue());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
