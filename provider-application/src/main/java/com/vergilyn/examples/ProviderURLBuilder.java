package com.vergilyn.examples;

/**
 * @author VergiLyn
 * @date 2019-12-12
 */
public class ProviderURLBuilder {
    protected static final int APPLICATION_PORT = 19010;
    private static final String BASE_URL = "http://127.0.0.1:" + APPLICATION_PORT;
    private static final String URL_HELLO = BASE_URL + "/provider/hello";

    public static String urlHello(){
        return urlHello(0L);
    }

    public static String urlHello(long ms){
        return ms <= 0 ? URL_HELLO : URL_HELLO + "?ms=" + ms;
    }
}
