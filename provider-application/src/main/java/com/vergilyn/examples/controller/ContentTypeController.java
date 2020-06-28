package com.vergilyn.examples.controller;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author vergilyn
 * @date 2020-03-30
 */
@Controller
@RequestMapping("/content-type")
@Slf4j
public class ContentTypeController {
    public static final String EXCEPT_CONTENT_TYPE = MediaType.IMAGE_JPEG_VALUE;
    public static final String ACTUAL_CONTENT_TYPE = MediaType.TEXT_HTML_VALUE;

    @RequestMapping("/modify-by-response-set-header")
    @ResponseBody
    public String modifyByResponseSetHeader(HttpServletResponse response,
                                            String contentType, Long contentLength){
        String thisMethod = Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("invoke {} parameter >>>> contentType: {}, contentLength: {}",
                thisMethod, contentType, contentLength);

        if (StringUtils.isNoneBlank(contentType)){
            response.setContentType(contentType);
            response.setHeader("custom-content-type", contentType);
        }

        if (contentLength != null){
            response.setContentLengthLong(contentLength);
            response.setHeader("custom-content-length", contentLength + "");
        }

        String result = String.format("expect >>>> content-type: %s, content-length: %d", contentType, contentLength);
        log.info("invoke {} (before-return) result >>>> {}", thisMethod, result);

        return result;
    }

    /**
     * chrome 请求会报 404，但后端无异常信息。<br/>
     * apache-http，请求正常，且 except == actual！
     */
    @RequestMapping(value = "/modify-by-request-mapping-produces", produces = {EXCEPT_CONTENT_TYPE})
    public String modifyByRequestMappingProduces(HttpServletResponse response){
        log.info("invoke {} >>>> ", Thread.currentThread().getStackTrace()[1].getMethodName());

        response.setContentType(EXCEPT_CONTENT_TYPE);

        return String.format("expect >>>> content-type: %s", EXCEPT_CONTENT_TYPE);
    }
}
