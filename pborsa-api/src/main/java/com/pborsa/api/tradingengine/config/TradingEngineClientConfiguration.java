package com.pborsa.api.tradingengine.config;

import com.pborsa.api.tradingengine.BacktestClient;
import com.pborsa.api.tradingengine.GrpcBacktestClient;
import com.pborsa.api.tradingengine.GrpcLiveBarClient;
import com.pborsa.api.tradingengine.GrpcTradingEngineClient;
import com.pborsa.api.tradingengine.LiveBarClient;
import com.pborsa.api.tradingengine.NoopBacktestClient;
import com.pborsa.api.tradingengine.NoopLiveBarClient;
import com.pborsa.api.tradingengine.NoopTradingEngineClient;
import com.pborsa.api.tradingengine.TradingEngineClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates trading engine client beans (gRPC or noop).
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

    @Bean
    public LiveBarClient liveBarClient() {
        if (!properties.isEnabled()) {
            log.info("Live bar gRPC client disabled via configuration.");
            return new NoopLiveBarClient();
        }
        String target = normalizeTarget(properties.getAddress());
        log.info("Creating live bar gRPC client targeting {}", target);
        return new GrpcLiveBarClient(target);
    }

    @Bean
    public BacktestClient backtestClient() {
        if (!properties.isEnabled()) {
            log.info("Backtest gRPC client disabled via configuration.");
            return new NoopBacktestClient();
        }
        String target = normalizeTarget(properties.getAddress());
        log.info("Creating backtest gRPC client targeting {}", target);
        return new GrpcBacktestClient(target);
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
