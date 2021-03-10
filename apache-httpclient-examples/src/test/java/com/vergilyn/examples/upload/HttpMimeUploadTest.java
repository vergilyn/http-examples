package com.vergilyn.examples.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.vergilyn.examples.AbstractHttpClientTestng;
import com.vergilyn.examples.ProviderURLBuilder;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

/**
 * @author vergilyn
 * @date 2021/1/6
 *
 * @see <a href="http://hc.apache.org/httpcomponents-client-4.5.x/httpmime/examples/org/apache/http/examples/entity/mime/ClientMultipartFormPost.java">ClientMultipartFormPost</a>
 */
public class HttpMimeUploadTest extends AbstractHttpClientTestng {

	private static final String _FILED_FILE = "file";
	private final MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
													.addTextBody("filename", "param_filename")
													.addTextBody("number", "1024");

	@Test(description = "success")
	public void addFile() throws FileNotFoundException {
		File file = ResourceUtils.getFile("classpath:local_file.txt");
		entityBuilder.addBinaryBody(_FILED_FILE, file);
	}

	@Test(description = "success")
	public void add(){
		String str = "string-content";
		// entityBuilder.addBinaryBody(_FILED_FILE, str.getBytes(StandardCharsets.UTF_8));  // error
		// avoid create temp-file
		entityBuilder.addBinaryBody(_FILED_FILE, str.getBytes(StandardCharsets.UTF_8), ContentType.DEFAULT_BINARY, "");
	}

	@AfterTest
	public void request() {
		String url = ProviderURLBuilder.URL_UPLOAD_FILE_FORM;
		HttpPost post = new HttpPost(url);

		post.setEntity(entityBuilder.build());

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(post);
			printResponse(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
