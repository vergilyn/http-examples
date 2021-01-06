package com.vergilyn.examples.controller;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.alibaba.fastjson.serializer.SerializerFeature.PrettyFormat;

@RestController
@RequestMapping("/upload")
@Slf4j
public class UploadController {

	@RequestMapping("/file-form")
	public String fileForm(MultipartFile file, String filename, Integer number){

		Map<String, Object> rs = Maps.newHashMap();
		rs.put("content-type", file.getContentType());
		rs.put("filename", filename);
		rs.put("number", number);

		try(InputStream inputStream = file.getInputStream()) {
			rs.put("file-content", IOUtils.toString(inputStream, StandardCharsets.UTF_8));
		}catch (Exception e){
			e.printStackTrace();
		}

		return JSON.toJSONString(rs, PrettyFormat);
	}
}
