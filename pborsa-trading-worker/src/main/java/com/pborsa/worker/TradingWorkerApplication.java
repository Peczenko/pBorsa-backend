package com.pborsa.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.pborsa.worker", "com.pborsa.trading"})
@EnableJpaRepositories(basePackages = {"com.pborsa.worker", "com.pborsa.trading"})
@EnableCaching
@ConfigurationPropertiesScan(basePackages = {"com.pborsa.worker", "com.pborsa.trading"})
public class TradingWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradingWorkerApplication.class, args);
    }
}

