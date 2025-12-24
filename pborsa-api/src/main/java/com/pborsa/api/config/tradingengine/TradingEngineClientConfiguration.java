package com.pborsa.api.config.tradingengine;

import com.pborsa.api.client.tradingengine.GrpcTradingEngineClient;
import com.pborsa.api.client.tradingengine.NoopTradingEngineClient;
import com.pborsa.api.client.tradingengine.TradingEngineClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates trading engine client bean (gRPC or noop).
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class TradingEngineClientConfiguration {

    private final TradingEngineProperties properties;

    @Bean
    public TradingEngineClient tradingEngineClient() {
        if (!properties.isEnabled()) {
            log.info("Trading engine gRPC client disabled via configuration.");
            return new NoopTradingEngineClient();
        }
        String target = normalizeTarget(properties.getAddress());
        log.info("Creating trading engine gRPC client targeting {}", target);
        return new GrpcTradingEngineClient(target);
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
