package com.pborsa.api.service.market;

import com.pborsa.api.config.cache.CacheNames;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.market.MarketDataSnapshot;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.domain.dto.market.StockTradeDto;
import com.pborsa.api.exception.AlpacaException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.service.mapper.MarketDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBarsResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockAdjustment;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockLatestQuotesResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockLatestTradesResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import net.jacobpeterson.alpaca.openapi.marketdata.model.Sort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service for fetching market data from Alpaca.
 * Provides quotes, trades, and bar data for stocks.
 * Uses IEX exchange for free tier real-time data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final AlpacaClientFactory clientFactory;
    private final UserCredentialsService credentialsService;
    private final MarketDataMapper marketDataMapper;

    /**
     * Gets the latest quote for a symbol.
     * Cached for a short duration to reduce API calls.
     *
     * @param userId User ID
     * @param symbol Stock symbol
     * @return Latest quote data
     */
    @Cacheable(value = CacheNames.QUOTES, key = "#symbol")
    public StockQuoteDto getLatestQuote(String userId, String symbol) {
        log.debug("Fetching latest quote for symbol: {}", symbol);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Get latest quotes using OpenAPI StockApi - signature: (symbols, feed, feed)
            StockLatestQuotesResp response = client.marketData().stock()
                    .stockLatestQuotes(symbol, StockFeed.IEX, null);

            StockQuote quote = response.getQuotes().get(symbol);
            if (quote == null) {
                throw new AlpacaException(
                        AlpacaException.ErrorCode.SYMBOL_NOT_FOUND,
                        "No quote data found for symbol: " + symbol
                );
            }

            return marketDataMapper.toStockQuoteDto(quote, symbol);
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch quote for symbol: {}", symbol, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.MARKET_DATA_ERROR,
                    "Failed to fetch quote: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Async version of getLatestQuote.
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<StockQuoteDto> getLatestQuoteAsync(String userId, String symbol) {
        return CompletableFuture.supplyAsync(() -> getLatestQuote(userId, symbol));
    }

    /**
     * Gets the latest quotes for multiple symbols.
     *
     * @param userId  User ID
     * @param symbols Collection of stock symbols
     * @return List of quote data
     */
    public List<StockQuoteDto> getLatestQuotes(String userId, Collection<String> symbols) {
        log.debug("Fetching latest quotes for {} symbols", symbols.size());

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Get latest quotes using OpenAPI StockApi - signature: (symbols, feed, feed)
            String symbolsStr = String.join(",", symbols);
            StockLatestQuotesResp response = client.marketData().stock()
                    .stockLatestQuotes(symbolsStr, StockFeed.IEX, null);
            
            List<StockQuoteDto> result = new ArrayList<>();
            for (String symbol : symbols) {
                StockQuote quote = response.getQuotes().get(symbol);
                if (quote != null) {
                    result.add(marketDataMapper.toStockQuoteDto(quote, symbol));
                }
            }
            return result;
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch quotes for symbols: {}", symbols, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.MARKET_DATA_ERROR,
                    "Failed to fetch quotes: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Async version of getLatestQuotes.
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<List<StockQuoteDto>> getLatestQuotesAsync(String userId, Collection<String> symbols) {
        return CompletableFuture.supplyAsync(() -> getLatestQuotes(userId, symbols));
    }

    /**
     * Gets the latest trade for a symbol.
     *
     * @param userId User ID
     * @param symbol Stock symbol
     * @return Latest trade data
     */
    @Cacheable(value = CacheNames.TRADES, key = "#symbol")
    public StockTradeDto getLatestTrade(String userId, String symbol) {
        log.debug("Fetching latest trade for symbol: {}", symbol);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Get latest trades using OpenAPI StockApi - signature: (symbols, feed, feed)
            StockLatestTradesResp response = client.marketData().stock()
                    .stockLatestTrades(symbol, StockFeed.IEX, null);
            
            StockTrade trade = response.getTrades().get(symbol);
            if (trade == null) {
                throw new AlpacaException(
                        AlpacaException.ErrorCode.SYMBOL_NOT_FOUND,
                        "No trade data found for symbol: " + symbol
                );
            }

            return marketDataMapper.toStockTradeDto(trade, symbol);
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch trade for symbol: {}", symbol, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.MARKET_DATA_ERROR,
                    "Failed to fetch trade: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Async version of getLatestTrade.
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<StockTradeDto> getLatestTradeAsync(String userId, String symbol) {
        return CompletableFuture.supplyAsync(() -> getLatestTrade(userId, symbol));
    }

    /**
     * Gets the latest trades for multiple symbols.
     *
     * @param userId  User ID
     * @param symbols Collection of stock symbols
     * @return List of trade data
     */
    public List<StockTradeDto> getLatestTrades(String userId, Collection<String> symbols) {
        log.debug("Fetching latest trades for {} symbols", symbols.size());

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Get latest trades using OpenAPI StockApi - signature: (symbols, feed, feed)
            String symbolsStr = String.join(",", symbols);
            StockLatestTradesResp response = client.marketData().stock()
                    .stockLatestTrades(symbolsStr, StockFeed.IEX, null);
            
            List<StockTradeDto> result = new ArrayList<>();
            for (String symbol : symbols) {
                StockTrade trade = response.getTrades().get(symbol);
                if (trade != null) {
                    result.add(marketDataMapper.toStockTradeDto(trade, symbol));
                }
            }
            return result;
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch trades for symbols: {}", symbols, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.MARKET_DATA_ERROR,
                    "Failed to fetch trades: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Async version of getLatestTrades.
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<List<StockTradeDto>> getLatestTradesAsync(String userId, Collection<String> symbols) {
        return CompletableFuture.supplyAsync(() -> getLatestTrades(userId, symbols));
    }

    /**
     * Gets historical bar data for a symbol.
     *
     * @param userId    User ID
     * @param symbol    Stock symbol
     * @param timeframe Timeframe value (e.g., 1, 5, 15)
     * @param period    Period type (DAY, HOUR, MINUTE)
     * @param start     Start time
     * @param end       End time
     * @param limit     Maximum number of bars
     * @return List of bar data
     */
    public List<StockBarDto> getHistoricalBars(String userId, String symbol, int timeframe,
                                                String period, ZonedDateTime start, ZonedDateTime end, Integer limit) {
        log.debug("Fetching historical bars for symbol: {} from {} to {}", symbol, start, end);
        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            String timeframeString = buildTimeframe(timeframe, period);

            OffsetDateTime startTime = start.toOffsetDateTime();
            OffsetDateTime endTime = end.toOffsetDateTime();
            StockBarsResp response = client.marketData().stock().stockBars(
                    symbol,
                    timeframeString,
                    startTime,
                    endTime,
                    limit != null ? limit.longValue() : null,
                    StockAdjustment.RAW,
                    null,
                    StockFeed.IEX,
                    null,
                    null,
                    Sort.ASC
            );

            Map<String, List<StockBar>> bars = response.getBars();
            if (bars == null || bars.isEmpty()) {
                return Collections.emptyList();
            }

            List<StockBarDto> result = new ArrayList<>();
            for (StockBar bar : bars.getOrDefault(symbol, Collections.emptyList())) {
                result.add(marketDataMapper.toStockBarDto(bar, symbol));
            }
            return result;
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

    private String buildTimeframe(int timeframe, String period) {
        String normalized = period == null ? "MINUTE" : period.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("DAY")) {
            return timeframe + "Day";
        }
        if (normalized.contains("HOUR")) {
            return timeframe + "Hour";
        }
        return timeframe + "Min";
    }

    /**
     * Gets a complete market data snapshot for multiple symbols.
     *
     * @param userId  User ID
     * @param symbols Symbols to get data for
     * @return Market data snapshot with quotes and trades
     */
    public MarketDataSnapshot getMarketDataSnapshot(String userId, Collection<String> symbols) {
        log.debug("Getting market data snapshot for {} symbols", symbols.size());
        
        List<StockQuoteDto> quotes = getLatestQuotes(userId, symbols);
        List<StockTradeDto> trades = getLatestTrades(userId, symbols);
        
        return MarketDataSnapshot.builder()
                .quotes(quotes)
                .latestTrades(trades)
                .latestBars(Collections.emptyList())
                .snapshotTimestamp(Instant.now())
                .dataSource("ALPACA_IEX")
                .build();
    }

    /**
     * Async version of getMarketDataSnapshot.
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<MarketDataSnapshot> getMarketDataSnapshotAsync(String userId, Collection<String> symbols) {
        return CompletableFuture.supplyAsync(() -> getMarketDataSnapshot(userId, symbols));
    }
}
