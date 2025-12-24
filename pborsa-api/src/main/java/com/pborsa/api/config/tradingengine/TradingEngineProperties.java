package com.pborsa.api.config.tradingengine;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for trading engine gRPC client.
 */
@Configuration
@ConfigurationProperties(prefix = "trading.engine.grpc")
@Getter
@Setter
public class TradingEngineProperties {
    private boolean enabled = false;
    private String address = "localhost:9090";
    private int batchSize = 1000;
    private int pageLimit = 10000;
}
