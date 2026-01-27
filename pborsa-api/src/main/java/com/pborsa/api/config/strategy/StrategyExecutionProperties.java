package com.pborsa.api.config.strategy;

import lombok.Getter;
import lombok.Setter;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.Period;

/**
 * Configuration properties for strategy execution.
 */
@ConfigurationProperties(prefix = "strategy.execution")
@Getter
@Setter
public class StrategyExecutionProperties {

    /**
     * Lookback period for historical data (e.g., P3M for 3 months).
     */
    private Period lookbackPeriod = Period.ofMonths(3);

    /**
     * End offset from current time (e.g., PT15M for 15 minutes).
     */
    private Duration endOffset = Duration.ofMinutes(15);

    /**
     * Stock feed source (IEX or SIP).
     */
    private StockFeed stockFeed = StockFeed.IEX;

    /**
     * Maximum number of records per API page request.
     */
    private int pageLimit = 10000;

    /**
     * Number of records per batch sent to trading engine.
     */
    private int batchSize = 1000;
}
