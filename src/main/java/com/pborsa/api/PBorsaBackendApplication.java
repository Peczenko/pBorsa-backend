package com.pborsa.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * pBorsa Trading Backend Application.
 * 
 * Features:
 * - Alpaca API integration for trading and market data
 * - Multi-user support with cached API credentials
 * - Real-time WebSocket market data streaming
 * - Temporal workflow engine for reliable trade execution
 * - Async/Future support for non-blocking operations
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan
public class PBorsaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PBorsaBackendApplication.class, args);
    }
}
