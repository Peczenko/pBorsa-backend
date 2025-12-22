package com.pborsa.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "com.pborsa.api")
@EnableCaching
@ConfigurationPropertiesScan
public class TradingWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradingWorkerApplication.class, args);
    }
}

