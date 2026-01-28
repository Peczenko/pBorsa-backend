package com.pborsa.api.service.strategy;

import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Handles Alpaca API rate limiting with exponential backoff.
 * Reusable across different Alpaca API fetchers.
 */
@Component
@Slf4j
public class AlpacaRateLimitHandler {

    private static final int RATE_LIMIT_STATUS_CODE = 429;
    private static final long BASE_BACKOFF_MILLIS = 1000L;
    private static final long MAX_BACKOFF_MILLIS = 60_000L;
    private static final int MAX_BACKOFF_EXPONENT = 5;

    /**
     * Executes an API call with rate limit handling and retries.
     *
     * @param apiCall      the API call to execute
     * @param contextId    identifier for logging (e.g., execution ID)
     * @param checkpoint   optional checkpoint callback for heartbeats
     * @param <T>          return type of the API call
     * @return the result of the API call
     * @throws ApiException if the API call fails for non-rate-limit reasons
     */
    public <T> T executeWithRetry(
            ApiCallable<T> apiCall,
            String contextId,
            Runnable checkpoint
    ) throws ApiException {
        Runnable checkpointRunner = checkpoint != null ? checkpoint : () -> {};
        int rateLimitAttempts = 0;

        while (true) {
            checkpointRunner.run();
            try {
                return apiCall.call();
            } catch (ApiException e) {
                if (e.getCode() == RATE_LIMIT_STATUS_CODE) {
                    rateLimitAttempts++;
                    long backoffMillis = resolveBackoffMillis(e, rateLimitAttempts);
                    log.warn("Context {} rate limited by Alpaca. Backing off {} ms (attempt {})",
                            contextId, backoffMillis, rateLimitAttempts);
                    sleepWithCheckpoint(backoffMillis, checkpointRunner);
                    continue;
                }
                throw e;
            }
        }
    }

    private long resolveBackoffMillis(ApiException exception, int attempt) {
        Long retryAfterSeconds = extractRetryAfterSeconds(exception);
        if (retryAfterSeconds != null && retryAfterSeconds > 0) {
            return Math.min(MAX_BACKOFF_MILLIS, retryAfterSeconds * 1000L);
        }
        long backoff = BASE_BACKOFF_MILLIS * (1L << Math.min(attempt - 1, MAX_BACKOFF_EXPONENT));
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

    /**
     * Functional interface for API calls that may throw ApiException.
     */
    @FunctionalInterface
    public interface ApiCallable<T> {
        T call() throws ApiException;
    }
}
