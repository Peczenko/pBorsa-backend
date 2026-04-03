package com.pborsa.api.strategy.execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.model.Sort;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBarsResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Fetches historical bar (OHLCV) data from Alpaca with rate limit handling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlpacaBarFetcher {

    private final AlpacaRateLimitHandler rateLimitHandler;

    /**
     * Fetches a page of historical bars from Alpaca.
     *
     * @param client      Alpaca API client
     * @param executionId execution ID for logging
     * @param symbol      stock symbol
     * @param start       start timestamp
     * @param end         end timestamp
     * @param timeframe   bar timeframe (e.g., "5Min", "15Min", "1Hour", "1Day")
     * @param pageLimit   maximum bars per page
     * @param pageToken   pagination token (null for first page)
     * @param stockFeed   data feed source (IEX or SIP)
     * @param checkpoint  checkpoint callback for heartbeats
     * @return page of bars with next page token
     */
    public BarPage fetchPage(
            AlpacaAPI client,
            String executionId,
            String symbol,
            Instant start,
            Instant end,
            String timeframe,
            int pageLimit,
            String pageToken,
            StockFeed stockFeed,
            Runnable checkpoint
    ) throws ApiException {
        StockBarsResp resp = rateLimitHandler.executeWithRetry(
                () -> client.marketData().stock().stockBars(
                        symbol,
                        timeframe,
                        OffsetDateTime.ofInstant(start, ZoneOffset.UTC),
                        OffsetDateTime.ofInstant(end, ZoneOffset.UTC),
                        (long) pageLimit,
                        null,
                        null,
                        stockFeed,
                        null,
                        pageToken,
                        Sort.ASC
                ),
                executionId,
                checkpoint
        );

        return new BarPage(extractBars(resp, symbol), resp.getNextPageToken());
    }

    private List<StockBar> extractBars(StockBarsResp resp, String symbol) {
        if (resp == null) {
            return Collections.emptyList();
        }
        Map<String, List<StockBar>> barsMap = resp.getBars();
        return barsMap.getOrDefault(symbol, Collections.emptyList());
    }
}
