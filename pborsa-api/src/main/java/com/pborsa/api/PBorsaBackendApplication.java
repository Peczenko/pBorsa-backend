package com.pborsa.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.pborsa.api", "com.pborsa.trading"})
@EnableCaching
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = {"com.pborsa.api", "com.pborsa.trading"})
public class PBorsaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PBorsaBackendApplication.class, args);
    }
}

