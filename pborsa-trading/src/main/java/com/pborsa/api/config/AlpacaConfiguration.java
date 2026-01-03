package com.pborsa.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Alpaca API integration.
 */
@Configuration
@ConfigurationProperties(prefix = "alpaca")
@Validated
@Getter
@Setter
public class AlpacaConfiguration {

    /**
     * Default settings for new users.
     */
    private DefaultSettings defaultSettings = new DefaultSettings();

    /**
     * Market data configuration.
     */
    private MarketData marketData = new MarketData();

    @Getter
    @Setter
    public static class DefaultSettings {
        /**
         * Default endpoint type: PAPER or LIVE.
         */
        private String endpointType = "PAPER";

        /**
         * Default data source: IEX (free) or SIP (paid).
         */
        private String dataSource = "IEX";
    }

    @Getter
    @Setter
    public static class MarketData {
        /**
         * WebSocket source for streaming: IEX or SIP.
         */
        private String websocketSource = "IEX";

        /**
         * Polling interval for market data in milliseconds.
         */
        private long pollingInterval = 1000;

        /**
         * Maximum symbols per subscription.
         */
        private int maxSymbolsPerSubscription = 100;

        /**
         * Connection timeout in milliseconds.
         */
        private long connectionTimeout = 5000;

        /**
         * Read timeout in milliseconds.
         */
        private long readTimeout = 30000;
    }

    /**
     * Checks if the default endpoint is paper trading.
     */
    public boolean isDefaultPaperTrading() {
        return "PAPER".equalsIgnoreCase(defaultSettings.getEndpointType());
    }

    /**
     * Checks if using IEX data source (free tier).
     */
    public boolean isUsingIexDataSource() {
        return "IEX".equalsIgnoreCase(defaultSettings.getDataSource());
    }
}

