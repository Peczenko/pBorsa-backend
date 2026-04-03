package com.pborsa.trading.config.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.pborsa.temporal.config.TaskQueues.MARKET_DATA_TASK_QUEUE;
import static com.pborsa.temporal.config.TaskQueues.TRADING_TASK_QUEUE;

/**
 * Configuration for Temporal ActivityOptions beans.
 * Used by both API (for workflow stubs) and worker (for activity execution).
 */
@Configuration
@Slf4j
public class TemporalActivityOptionsConfiguration {

    // Trading activity configuration
    @Value("${temporal.activities.trading.start-to-close-timeout:10m}")
    private String tradingActivityTimeout;

    @Value("${temporal.activities.trading.max-attempts:5}")
    private int tradingMaxAttempts;

    @Value("${temporal.activities.trading.initial-retry-interval:5s}")
    private String tradingInitialRetryInterval;

    @Value("${temporal.activities.trading.backoff-coefficient:2.0}")
    private double tradingBackoffCoefficient;

    // Market data activity configuration
    @Value("${temporal.activities.market-data.start-to-close-timeout:2m}")
    private String marketDataActivityTimeout;

    @Value("${temporal.activities.market-data.max-attempts:2}")
    private int marketDataMaxAttempts;

    @Value("${temporal.activities.market-data.initial-retry-interval:1s}")
    private String marketDataInitialRetryInterval;

    @Value("${temporal.activities.market-data.backoff-coefficient:1.5}")
    private double marketDataBackoffCoefficient;

    /**
     * Creates ActivityOptions bean for trading activities.
     * Configured with longer timeout and more retries for trading operations.
     */
    @Bean("tradingActivityOptions")
    public ActivityOptions tradingActivityOptions() {
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(parseDuration(tradingInitialRetryInterval))
                .setMaximumInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(tradingBackoffCoefficient)
                .setMaximumAttempts(tradingMaxAttempts)
                .build();

        return ActivityOptions.newBuilder()
                .setStartToCloseTimeout(parseDuration(tradingActivityTimeout))
                .setTaskQueue(TRADING_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();
    }

    /**
     * Creates ActivityOptions bean for market data activities.
     * Configured with shorter timeout and fewer retries for market data operations.
     */
    @Bean("marketDataActivityOptions")
    public ActivityOptions marketDataActivityOptions() {
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(parseDuration(marketDataInitialRetryInterval))
                .setMaximumInterval(Duration.ofSeconds(10))
                .setBackoffCoefficient(marketDataBackoffCoefficient)
                .setMaximumAttempts(marketDataMaxAttempts)
                .build();

        return ActivityOptions.newBuilder()
                .setStartToCloseTimeout(parseDuration(marketDataActivityTimeout))
                .setTaskQueue(MARKET_DATA_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();
    }

    /**
     * Parses duration string (e.g., "10m", "30s", "1h") to Duration.
     */
    private Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return Duration.ofMinutes(2);
        }
        
        durationStr = durationStr.trim().toLowerCase();
        if (durationStr.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        } else if (durationStr.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        } else if (durationStr.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        } else {
            // Try parsing as seconds
            return Duration.ofSeconds(Long.parseLong(durationStr));
        }
    }
}

