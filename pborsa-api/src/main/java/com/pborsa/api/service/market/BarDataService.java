package com.pborsa.api.service.market;

import com.pborsa.api.config.market.BarDataProperties;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.exception.AlpacaException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UnifiedCredentialsService;
import com.pborsa.api.service.mapper.MarketDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.model.Sort;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockAdjustment;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBarsResp;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service for fetching bar (OHLCV) data for UI charts and analytics.
 * Provides clean API for historical bars, latest bars, and multi-symbol fetching.
 *
 * <p>Design principles:
 * <ul>
 *   <li>Single responsibility: Only handles bar data fetching</li>
 *   <li>Configurable: All settings via BarDataProperties</li>
 *   <li>Validated: Input validation with clear error messages</li>
 *   <li>Efficient: Batch fetching for multiple symbols</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BarDataService {

    private final AlpacaClientFactory clientFactory;
    private final UnifiedCredentialsService credentialsService;
    private final MarketDataMapper marketDataMapper;
    private final BarDataProperties barDataProperties;

    /**
     * Fetches historical bars for a symbol.
     *
     * @param userId    user ID for credentials (null for system credentials)
     * @param symbol    stock symbol
     * @param timeframe bar timeframe (e.g., "1Min", "5Min", "1Hour")
     * @param start     start time (inclusive)
     * @param end       end time (inclusive)
     * @param limit     maximum number of bars (null for default)
     * @return list of bars sorted by timestamp ascending
     */
    public List<StockBarDto> getHistoricalBars(
            Long userId,
            String symbol,
            String timeframe,
            Instant start,
            Instant end,
            Integer limit
    ) {
        validateSymbol(symbol);
        String validatedTimeframe = validateAndNormalizeTimeframe(timeframe);
        TimeRange timeRange = validateAndNormalizeTimeRange(start, end);
        int validatedLimit = validateLimit(limit);

        log.debug("Fetching historical bars: symbol={}, timeframe={}, start={}, end={}, limit={}",
                symbol, validatedTimeframe, timeRange.start(), timeRange.end(), validatedLimit);

        try {
            AlpacaAPI client = getClient(userId);
            StockBarsResp response = fetchBarsFromAlpaca(
                    client, symbol, validatedTimeframe, timeRange, validatedLimit, Sort.ASC
            );

            return extractBars(response, symbol);
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch historical bars for symbol: {}", symbol, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.MARKET_DATA_ERROR,
                    "Failed to fetch historical bars: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Fetches the latest (most recent) bar for a symbol.
     *
     * @param userId    user ID for credentials
     * @param symbol    stock symbol
     * @param timeframe bar timeframe
     * @return the latest bar, or null if not available
     */
    public StockBarDto getLatestBar(Long userId, String symbol, String timeframe) {
        validateSymbol(symbol);
        String validatedTimeframe = validateAndNormalizeTimeframe(timeframe);

        // Fetch last few bars and return the most recent
        Instant end = Instant.now();
        Instant start = calculateStartForTimeframe(end, validatedTimeframe, 5);

        try {
            AlpacaAPI client = getClient(userId);
            StockBarsResp response = fetchBarsFromAlpaca(
                    client, symbol, validatedTimeframe, new TimeRange(start, end), 5, Sort.DESC
            );

            List<StockBarDto> bars = extractBars(response, symbol);
            return bars.isEmpty() ? null : bars.getFirst();
        } catch (Exception e) {
            log.error("Failed to fetch latest bar for symbol: {}", symbol, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.MARKET_DATA_ERROR,
                    "Failed to fetch latest bar: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Fetches the latest bars for multiple symbols.
     *
     * @param userId    user ID for credentials
     * @param symbols   collection of stock symbols
     * @param timeframe bar timeframe
     * @return map of symbol to latest bar
     */
    public Map<String, StockBarDto> getLatestBars(Long userId, Collection<String> symbols, String timeframe) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyMap();
        }

        String validatedTimeframe = validateAndNormalizeTimeframe(timeframe);
        Instant end = Instant.now();
        Instant start = calculateStartForTimeframe(end, validatedTimeframe, 5);

        Map<String, StockBarDto> result = new HashMap<>();

        try {
            AlpacaAPI client = getClient(userId);

            // Fetch bars for all symbols in one batch call
            String symbolsParam = String.join(",", symbols);
            StockBarsResp response = fetchBarsFromAlpaca(
                    client, symbolsParam, validatedTimeframe, new TimeRange(start, end), 5 * symbols.size(), Sort.DESC
            );

            // Extract latest bar for each symbol
            if (response != null && response.getBars() != null) {
                for (String symbol : symbols) {
                    List<StockBar> symbolBars = response.getBars().get(symbol);
                    if (symbolBars != null && !symbolBars.isEmpty()) {
                        // First bar is most recent due to DESC sort
                        result.put(symbol, marketDataMapper.toStockBarDto(symbolBars.getFirst(), symbol));
                    }
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch latest bars for symbols: {}", symbols, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.MARKET_DATA_ERROR,
                    "Failed to fetch latest bars: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Fetches bars for chart display with sensible defaults.
     * Automatically calculates time range based on timeframe and bar count.
     *
     * @param userId    user ID for credentials
     * @param symbol    stock symbol
     * @param timeframe bar timeframe
     * @param barCount  number of bars to fetch
     * @return list of bars for chart rendering
     */
    public List<StockBarDto> getBarsForChart(Long userId, String symbol, String timeframe, int barCount) {
        validateSymbol(symbol);
        String validatedTimeframe = validateAndNormalizeTimeframe(timeframe);
        int validatedCount = Math.min(Math.max(barCount, 1), barDataProperties.getMaxBarsPerRequest());

        Instant end = Instant.now();
        Instant start = calculateStartForTimeframe(end, validatedTimeframe, validatedCount);

        return getHistoricalBars(userId, symbol, validatedTimeframe, start, end, validatedCount);
    }

    // ==================== Helper Methods ====================

    private AlpacaAPI getClient(Long userId) {
        AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
        return clientFactory.getOrCreateClient(credentials);
    }

    private StockBarsResp fetchBarsFromAlpaca(
            AlpacaAPI client,
            String symbol,
            String timeframe,
            TimeRange timeRange,
            int limit,
            Sort sort
    ) throws Exception {
        StockAdjustment adjustment = barDataProperties.isUseAdjusted()
                ? StockAdjustment.SPLIT
                : StockAdjustment.RAW;

        return client.marketData().stock().stockBars(
                symbol,
                timeframe,
                OffsetDateTime.ofInstant(timeRange.start(), ZoneOffset.UTC),
                OffsetDateTime.ofInstant(timeRange.end(), ZoneOffset.UTC),
                (long) limit,
                adjustment,
                null,
                barDataProperties.getStockFeed(),
                null,
                null,
                sort
        );
    }

    private List<StockBarDto> extractBars(StockBarsResp response, String symbol) {
        if (response == null || response.getBars() == null) {
            return Collections.emptyList();
        }

        List<StockBar> bars = response.getBars().get(symbol);
        if (bars == null || bars.isEmpty()) {
            return Collections.emptyList();
        }

        List<StockBarDto> result = new ArrayList<>(bars.size());
        for (StockBar bar : bars) {
            result.add(marketDataMapper.toStockBarDto(bar, symbol));
        }
        return result;
    }

    private void validateSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol is required");
        }
    }

    private String validateAndNormalizeTimeframe(String timeframe) {
        String normalized = timeframe != null ? timeframe : barDataProperties.getDefaultTimeframe();

        if (!barDataProperties.isTimeframeSupported(normalized)) {
            throw new IllegalArgumentException(
                    "Unsupported timeframe: " + normalized +
                            ". Supported: " + barDataProperties.getSupportedTimeframes()
            );
        }

        return normalized;
    }

    private TimeRange validateAndNormalizeTimeRange(Instant start, Instant end) {
        Instant now = Instant.now();
        Instant effectiveEnd = (end != null) ? end : now;
        Instant effectiveStart = (start != null) ? start : effectiveEnd.minus(barDataProperties.getDefaultLookback());

        // Validate max lookback
        Duration lookback = Duration.between(effectiveStart, effectiveEnd);
        if (lookback.compareTo(barDataProperties.getMaxLookback()) > 0) {
            effectiveStart = effectiveEnd.minus(barDataProperties.getMaxLookback());
            log.warn("Lookback period exceeded maximum. Adjusted start to: {}", effectiveStart);
        }

        // Ensure start is before end
        if (effectiveStart.isAfter(effectiveEnd)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        return new TimeRange(effectiveStart, effectiveEnd);
    }

    private int validateLimit(Integer limit) {
        if (limit == null) {
            return barDataProperties.getDefaultBarsLimit();
        }
        return Math.min(Math.max(limit, 1), barDataProperties.getMaxBarsPerRequest());
    }

    private Instant calculateStartForTimeframe(Instant end, String timeframe, int barCount) {
        Duration barDuration = parseTimeframeDuration(timeframe);
        return end.minus(barDuration.multipliedBy(barCount));
    }

    private Duration parseTimeframeDuration(String timeframe) {
        Objects.requireNonNull(timeframe, "timeframe");

        // Parse timeframe like "1Min", "5Min", "1Hour", "1Day"
        String normalized = timeframe.trim();

        if (normalized.endsWith("Min")) {
            int minutes = Integer.parseInt(normalized.replace("Min", ""));
            return Duration.ofMinutes(minutes);
        } else if (normalized.endsWith("Hour")) {
            int hours = Integer.parseInt(normalized.replace("Hour", ""));
            return Duration.ofHours(hours);
        } else if (normalized.endsWith("Day")) {
            int days = Integer.parseInt(normalized.replace("Day", ""));
            return Duration.ofDays(days);
        } else if (normalized.endsWith("Week")) {
            int weeks = Integer.parseInt(normalized.replace("Week", ""));
            return Duration.ofDays(weeks * 7L);
        }

        // Default to 1 minute if unknown
        return Duration.ofMinutes(1);
    }

    /**
     * Internal record for validated time range.
     */
    private record TimeRange(Instant start, Instant end) {}
}
