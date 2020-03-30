package com.vergilyn.examples;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author VergiLyn
 * @date 2019-12-12
 */
@SpringBootApplication
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ProviderApplication.class);

        Properties properties = new Properties();
        // properties.setProperty("spring.application.name", "provider-application");
        properties.setProperty("server.port", ProviderURLBuilder.APPLICATION_PORT + "");
        properties.setProperty("server.tomcat.connection-timeout", "1d");

        application.setDefaultProperties(properties);

        application.run(args);
    }

}
