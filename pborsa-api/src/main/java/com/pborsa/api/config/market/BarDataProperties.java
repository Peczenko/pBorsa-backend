package com.pborsa.api.config.market;

import lombok.Getter;
import lombok.Setter;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for bar data fetching and streaming.
 * Centralizes all bar-related configuration in one place.
 */
@Component
@ConfigurationProperties(prefix = "bar.data")
@Getter
@Setter
public class BarDataProperties {

    /**
     * Whether bar streaming to trading engine is enabled.
     */
    private boolean streamingEnabled = true;

    /**
     * Interval for sending live bars to trading engine.
     */
    private Duration streamingInterval = Duration.ofMinutes(1);

    /**
     * Default timeframe for bar data (e.g., "1Min", "5Min", "15Min", "1Hour", "1Day").
     */
    private String defaultTimeframe = "5Min";

    /**
     * Supported timeframes for UI selection.
     */
    private List<String> supportedTimeframes = List.of(
            "1Min", "5Min", "15Min", "30Min", "1Hour", "4Hour", "1Day", "1Week"
    );

    /**
     * Default lookback period for historical bars.
     */
    private Duration defaultLookback = Duration.ofDays(7);

    /**
     * Maximum lookback period allowed.
     */
    private Duration maxLookback = Duration.ofDays(365);

    /**
     * Maximum number of bars per request.
     */
    private int maxBarsPerRequest = 10000;

    /**
     * Default number of bars if not specified.
     */
    private int defaultBarsLimit = 500;

    /**
     * Stock feed source (IEX for free tier, SIP for paid).
     */
    private StockFeed stockFeed = StockFeed.IEX;

    /**
     * Cache duration for bar data.
     */
    private Duration cacheDuration = Duration.ofSeconds(30);

    /**
     * Whether to use raw or adjusted bar data.
     */
    private boolean useAdjusted = false;

    /**
     * Validates if a timeframe is supported.
     */
    public boolean isTimeframeSupported(String timeframe) {
        return supportedTimeframes.contains(timeframe);
    }

    /**
     * Gets streaming interval in seconds for scheduled tasks.
     */
    public long getStreamingIntervalSeconds() {
        return streamingInterval.toSeconds();
    }
}
