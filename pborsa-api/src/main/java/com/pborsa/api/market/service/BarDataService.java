package com.pborsa.api.market.service;

import com.pborsa.api.market.config.BarDataProperties;
import com.pborsa.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.domain.dto.market.StockBarDto;
import com.pborsa.domain.exception.AlpacaException;
import com.pborsa.trading.alpaca.AlpacaClientFactory;
import com.pborsa.trading.credentials.UnifiedCredentialsService;
import com.pborsa.trading.mapper.MarketDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.model.Sort;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockAdjustment;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBarsResp;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for fetching bar (OHLCV) data for UI charts and analytics.
 * Delegates validation to {@link BarDataValidator} and time range
 * calculations to {@link BarTimeRangeCalculator}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BarDataService {

    private final AlpacaClientFactory clientFactory;
    private final UnifiedCredentialsService credentialsService;
    private final MarketDataMapper marketDataMapper;
    private final BarDataProperties barDataProperties;
    private final BarDataValidator validator;
    private final BarTimeRangeCalculator timeRangeCalculator;

    /**
     * Fetches historical bars for a symbol.
     */
    public List<StockBarDto> getHistoricalBars(
            Long userId,
            String symbol,
            String timeframe,
            Instant start,
            Instant end,
            Integer limit
    ) {
        validator.validateSymbol(symbol);
        String validatedTimeframe = validator.validateAndNormalizeTimeframe(timeframe);
        int validatedLimit = validator.validateLimit(limit);

        BarTimeRangeCalculator.TimeRange timeRange =
                timeRangeCalculator.calculateTimeRangeForBars(start, end, validatedTimeframe, validatedLimit);

        log.debug("Fetching historical bars: symbol={}, timeframe={}, start={}, end={}, limit={}",
                symbol, validatedTimeframe, timeRange.start(), timeRange.end(), validatedLimit);

        try {
            AlpacaAPI client = getClient(userId);
            StockBarsResp response = fetchBarsFromAlpaca(
                    client, symbol, validatedTimeframe, timeRange, validatedLimit, Sort.DESC
            );

            List<StockBarDto> bars = extractBars(response, symbol);

            if (bars.isEmpty()) {
                log.warn("No bars returned for symbol={}, timeframe={}, start={}, end={}. Feed: {}",
                        symbol, validatedTimeframe, timeRange.start(), timeRange.end(),
                        barDataProperties.getStockFeed());
                timeRangeCalculator.logTradingHoursDiagnostics(timeRange.start(), timeRange.end());
            } else {
                Collections.reverse(bars);
            }

            return bars;
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
     */
    public StockBarDto getLatestBar(Long userId, String symbol, String timeframe) {
        validator.validateSymbol(symbol);
        String validatedTimeframe = validator.validateAndNormalizeTimeframe(timeframe);

        Instant end = Instant.now();
        Instant start = timeRangeCalculator.calculateStartForTimeframe(end, validatedTimeframe, 5);

        try {
            AlpacaAPI client = getClient(userId);
            StockBarsResp response = fetchBarsFromAlpaca(
                    client, symbol, validatedTimeframe,
                    new BarTimeRangeCalculator.TimeRange(start, end), 5, Sort.DESC
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
     */
    public Map<String, StockBarDto> getLatestBars(Long userId, Collection<String> symbols, String timeframe) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyMap();
        }

        String validatedTimeframe = validator.validateAndNormalizeTimeframe(timeframe);
        Instant end = Instant.now();
        Instant start = timeRangeCalculator.calculateStartForTimeframe(end, validatedTimeframe, 5);

        Map<String, StockBarDto> result = new HashMap<>();

        try {
            AlpacaAPI client = getClient(userId);

            String symbolsParam = String.join(",", symbols);
            StockBarsResp response = fetchBarsFromAlpaca(
                    client, symbolsParam, validatedTimeframe,
                    new BarTimeRangeCalculator.TimeRange(start, end), 5 * symbols.size(), Sort.DESC
            );

            if (response != null && response.getBars() != null) {
                for (String symbol : symbols) {
                    List<StockBar> symbolBars = response.getBars().get(symbol);
                    if (symbolBars != null && !symbolBars.isEmpty()) {
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
     */
    public List<StockBarDto> getBarsForChart(Long userId, String symbol, String timeframe, int barCount) {
        validator.validateSymbol(symbol);
        String validatedTimeframe = validator.validateAndNormalizeTimeframe(timeframe);
        int validatedCount = Math.min(Math.max(barCount, 1), barDataProperties.getMaxBarsPerRequest());

        Instant end = Instant.now();
        Instant start = timeRangeCalculator.calculateStartForTimeframe(end, validatedTimeframe, validatedCount);

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
            BarTimeRangeCalculator.TimeRange timeRange,
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
}
