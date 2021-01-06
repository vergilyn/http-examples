package com.vergilyn.examples.upload;

import java.io.File;
import java.io.IOException;

import com.vergilyn.examples.AbstractHttpClientTestng;
import com.vergilyn.examples.ProviderURLBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

/**
 * @author vergilyn
 * @date 2021/1/6
 *
 * @see <a href="http://hc.apache.org/httpcomponents-client-4.5.x/httpmime/examples/org/apache/http/examples/entity/mime/ClientMultipartFormPost.java">ClientMultipartFormPost</a>
 */
public class HttpMimeUploadTest extends AbstractHttpClientTestng {

	@Test
	public void testPostFileForm() throws IOException {
		String url = ProviderURLBuilder.URL_UPLOAD_FILE_FORM;

		HttpPost post = new HttpPost(url);

		File file = ResourceUtils.getFile("classpath:local_file.txt");

		HttpEntity reqEntity = MultipartEntityBuilder.create()
					.addBinaryBody("file", file)
					// .addBinaryBody(String name, InputStream stream)
					// .addBinaryBody(String name, byte[] b)
					.addTextBody("filename", "param_filename")
					.addTextBody("number", "1024")
					.build();

		post.setEntity(reqEntity);

		CloseableHttpResponse response = httpClient.execute(post);
		printResponse(response);
	}
}
