package com.pborsa.api.tradingengine.config;

import com.pborsa.api.tradingengine.TradingEngineOrderStatusClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for trading engine order status notification client.
 * Creates a gRPC client bean for sending order status updates to the trading engine.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class TradingEngineOrderStatusClientConfiguration {

    private final TradingEngineProperties properties;

    @Bean
    @ConditionalOnProperty(name = "trading.engine.grpc.enabled", havingValue = "true")
    public TradingEngineOrderStatusClient tradingEngineOrderStatusClient() {
        String target = normalizeTarget(properties.getAddress());
        log.info("Creating trading engine order status client targeting {}", target);
        return new TradingEngineOrderStatusClient(target);
    }

    private String normalizeTarget(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("trading.engine.grpc.address must be set");
        }
        String trimmed = address.trim();
        if (trimmed.startsWith("static://")) {
            trimmed = trimmed.substring("static://".length());
        }
        if (trimmed.startsWith("dns://") && !trimmed.startsWith("dns:///")) {
            trimmed = "dns:///" + trimmed.substring("dns://".length());
        }
        if (!trimmed.contains("://")) {
            trimmed = "dns:///" + trimmed;
        }
        return trimmed;
    }
}

