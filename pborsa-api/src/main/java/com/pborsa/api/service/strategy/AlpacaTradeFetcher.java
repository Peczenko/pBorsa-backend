package com.pborsa.api.service.strategy;

import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTradesResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AlpacaTradeFetcher {

    private static final int RATE_LIMIT_STATUS_CODE = 429;
    private static final long BASE_BACKOFF_MILLIS = 1000L;
    private static final long MAX_BACKOFF_MILLIS = 60_000L;

    public TradePage fetchPage(AlpacaAPI client,
                               String executionId,
                               String symbol,
                               Instant start,
                               Instant end,
                               int pageLimit,
                               String pageToken,
                               Runnable checkpoint) throws ApiException {
        Runnable checkpointRunner = checkpoint != null ? checkpoint : () -> {};
        int rateLimitAttempts = 0;

        while (true) {
            checkpointRunner.run();
            try {
                StockTradesResp resp = client.marketData().stock().stockTrades(
                        symbol,
                        OffsetDateTime.ofInstant(start, ZoneOffset.UTC),
                        OffsetDateTime.ofInstant(end, ZoneOffset.UTC),
                        (long) pageLimit,
                        null,
                        StockFeed.IEX,
                        null,
                        pageToken,
                        Sort.ASC
                );
                rateLimitAttempts = 0;
                return new TradePage(extractTrades(resp, symbol), resp.getNextPageToken());
            } catch (ApiException e) {
                if (e.getCode() == RATE_LIMIT_STATUS_CODE) {
                    rateLimitAttempts++;
                    long backoffMillis = resolveRateLimitBackoffMillis(e, rateLimitAttempts);
                    log.warn("Execution {} rate limited by Alpaca. Backing off {} ms before retrying pageToken={}",
                            executionId, backoffMillis, pageToken);
                    sleepWithCheckpoint(backoffMillis, checkpointRunner);
                    continue;
                }
                throw e;
            }
        }
    }

    private List<StockTrade> extractTrades(StockTradesResp resp, String symbol) {
        if (resp == null) {
            return Collections.emptyList();
        }
        Map<String, List<StockTrade>> tradesMap = resp.getTrades();
        return tradesMap.getOrDefault(symbol, Collections.emptyList());
    }

    private long resolveRateLimitBackoffMillis(ApiException exception, int attempt) {
        Long retryAfterSeconds = extractRetryAfterSeconds(exception);
        if (retryAfterSeconds != null && retryAfterSeconds > 0) {
            return Math.min(MAX_BACKOFF_MILLIS, retryAfterSeconds * 1000L);
        }
        long backoff = BASE_BACKOFF_MILLIS * (1L << Math.min(attempt - 1, 5));
        return Math.min(MAX_BACKOFF_MILLIS, backoff);
    }

    private Long extractRetryAfterSeconds(ApiException exception) {
        Map<String, List<String>> headers = exception.getResponseHeaders();
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("Retry-After")) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    try {
                        return Long.parseLong(values.getFirst());
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private void sleepWithCheckpoint(long totalMillis, Runnable checkpointRunner) {
        long remaining = totalMillis;
        while (remaining > 0) {
            checkpointRunner.run();
            long sleepMillis = Math.min(remaining, 1000L);
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while backing off after rate limit", e);
            }
            remaining -= sleepMillis;
        }
    }

    public record TradePage(List<StockTrade> trades, String nextPageToken) {
    }
}
