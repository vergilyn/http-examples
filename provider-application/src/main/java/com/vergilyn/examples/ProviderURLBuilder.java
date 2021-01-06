package com.vergilyn.examples;

/**
 * @author vergilyn
 * @date 2019-12-12
 */
public class ProviderURLBuilder {
    protected static final int APPLICATION_PORT = 19010;

    private static final String URL_HOST = "http://127.0.0.1:" + APPLICATION_PORT;

    private static final String URL_SERVER_PROVIDER = URL_HOST + "/provider";
    private static final String URL_HELLO = URL_SERVER_PROVIDER + "/hello";

    private static final String URL_SERVER_CONTENT_TYPE = URL_HOST + "/content-type";
    private static final String URL_MODIFY_BY_RESPONSE_SET_HEADER = URL_SERVER_CONTENT_TYPE + "/modify-by-response-set-header";
    private static final String URL_MODIFY_BY_REQUEST_MAPPING_PRODUCES = URL_SERVER_CONTENT_TYPE + "/modify-by-request-mapping-produces";

    public static final String URL_UPLOAD_FILE_FORM = URL_HOST + "/upload/file-form";

    public static String urlHello(){
        return URL_HELLO;
    }

    public static String urlHello(long ms){
        return ms <= 0 ? URL_HELLO : URL_HELLO + "?ms=" + ms;
    }

    public static String urlModifyByResponseSetHeader(){
        return urlModifyByResponseSetHeader(null, null);
    }

    public static String urlModifyByResponseSetHeader(String contentType, Long contentLength){
        String url = URL_MODIFY_BY_RESPONSE_SET_HEADER + "?contentTyp=" + contentType;

        if (contentLength != null && contentLength > 0){
            url += "&contentLength=" + contentLength;
        }

        return url;
    }

    public static String urlModifyByRequestMappingProduces(){
        return URL_MODIFY_BY_REQUEST_MAPPING_PRODUCES;
    }
}
