package com.pborsa.api.config.backtest;

import lombok.Getter;
import lombok.Setter;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Period;

/**
 * Configuration properties for backtesting.
 */
@ConfigurationProperties(prefix = "backtest")
@Getter
@Setter
public class BacktestProperties {

    /**
     * Lookback period for historical data before testing start (e.g., P1Y for 1 year).
     * This data provides context to the strategy.
     */
    private Period historyLookbackPeriod = Period.ofYears(1);

    /**
     * Stock feed source (IEX or SIP).
     */
    private StockFeed stockFeed = StockFeed.IEX;

    /**
     * Bar timeframe for historical data.
     * Valid values: 1Min, 5Min, 15Min, 30Min, 1Hour, 4Hour, 1Day, 1Week, 1Month
     */
    private String timeframe = "5Min";

    /**
     * Maximum number of records per API page request.
     */
    private int pageLimit = 10000;

    /**
     * Maximum total bars to fetch per request (to prevent memory issues).
     */
    private int maxBarsPerRequest = 100000;
}
