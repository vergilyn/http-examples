package com.vergilyn.examples;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

/**
 * @author VergiLyn
 * @date 2019-12-16
 */
@Slf4j
public abstract class AbstractTestng {
    protected HttpClient httpClient;
    protected long beginTimestamp;

    @BeforeTest
    protected void before() {
        beginTimestamp = System.currentTimeMillis();
        httpClient = HttpClients.custom()
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(40)
                .build();
        log.info(">>>> invoke begin! <<<<");
    }

    @AfterTest
    protected void after(){
        log.info(">>>> invoke finish, exec: {} ms <<<<", System.currentTimeMillis() - beginTimestamp);
    }
}
