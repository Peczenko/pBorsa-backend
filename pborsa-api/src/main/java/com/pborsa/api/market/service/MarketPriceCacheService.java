package com.pborsa.api.market.service;

import com.pborsa.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.strategy.repository.UserStrategyRepository;
import com.pborsa.trading.alpaca.AlpacaClientFactory;
import com.pborsa.trading.credentials.UserCredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockLatestTradesResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockLatestTradesRespSingle;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for caching current market prices.
 * Periodically refreshes prices for symbols with active strategies.
 * Uses latest trade prices from Alpaca.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketPriceCacheService {

    private static final String MARKET_PRICES_CACHE = "marketPrices";
    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final UserCredentialsService credentialsService;
    private final AlpacaClientFactory alpacaClientFactory;
    private final UserStrategyRepository userStrategyRepository;

    // In-memory cache for prices (backed by Caffeine if configured)
    private final Map<String, BigDecimal> priceCache = new ConcurrentHashMap<>();

    /**
     * Gets the current market price for a symbol.
     * Returns cached price if available, otherwise fetches fresh.
     *
     * @param symbol Stock symbol
     * @param userId User ID for API access (used to get Alpaca client)
     * @return Current price, or empty if unavailable
     */
    public Optional<BigDecimal> getCurrentPrice(String symbol, Long userId) {
        String normalizedSymbol = symbol.toUpperCase();

        // Check cache first
        BigDecimal cached = priceCache.get(normalizedSymbol);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Fetch fresh price
        return fetchLatestPrice(normalizedSymbol, userId);
    }

    /**
     * Gets the current market price using the in-memory cache only.
     * Does not fetch if not cached.
     *
     * @param symbol Stock symbol
     * @return Cached price, or empty if not cached
     */
    public Optional<BigDecimal> getCachedPrice(String symbol) {
        return Optional.ofNullable(priceCache.get(symbol.toUpperCase()));
    }

    /**
     * Fetches the latest trade price from Alpaca.
     *
     * @param symbol Normalized stock symbol
     * @param userId User ID for API access
     * @return Latest trade price, or empty if unavailable
     */
    private Optional<BigDecimal> fetchLatestPrice(String symbol, Long userId) {
        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = alpacaClientFactory.getOrCreateClient(credentials);

            StockLatestTradesRespSingle response = client.marketData().stock()
                    .stockLatestTradeSingle(symbol, StockFeed.IEX, null);

            if (response != null && response.getTrade() != null && response.getTrade().getP() != null) {
                BigDecimal price = BigDecimal.valueOf(response.getTrade().getP());
                priceCache.put(symbol, price);
                log.debug("Fetched latest price for {}: {}", symbol, price);
                return Optional.of(price);
            }
        } catch (ApiException e) {
            log.warn("Failed to fetch latest price for {}: {}", symbol, e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching latest price for {}", symbol, e);
        }

        return Optional.empty();
    }

    /**
     * Fetches prices for multiple symbols in a single API call.
     *
     * @param symbols Set of stock symbols
     * @param userId  User ID for API access
     * @return Map of symbol to price
     */
    public Map<String, BigDecimal> fetchPrices(Set<String> symbols, Long userId) {
        if (symbols == null || symbols.isEmpty()) {
            return Map.of();
        }

        Map<String, BigDecimal> prices = new HashMap<>();
        String symbolsParam = String.join(",", symbols);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = alpacaClientFactory.getOrCreateClient(credentials);

            StockLatestTradesResp response = client.marketData().stock()
                    .stockLatestTrades(symbolsParam, StockFeed.IEX, null);

            if (response != null && response.getTrades() != null) {
                for (Map.Entry<String, StockTrade> entry : response.getTrades().entrySet()) {
                    String symbol = entry.getKey();
                    StockTrade trade = entry.getValue();
                    if (trade != null && trade.getP() != null) {
                        BigDecimal price = BigDecimal.valueOf(trade.getP());
                        prices.put(symbol, price);
                        priceCache.put(symbol, price);
                    }
                }
            }
            log.debug("Fetched prices for {} symbols", prices.size());
        } catch (ApiException e) {
            log.warn("Failed to fetch batch prices: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching batch prices", e);
        }

        return prices;
    }

    /**
     * Scheduled task to refresh prices for all active strategy symbols.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = REFRESH_INTERVAL_MS)
    public void refreshActivePrices() {
        try {
            // Get all unique symbols from active strategies
            Set<String> activeSymbols = userStrategyRepository.findDistinctSymbolsByActiveStatus();

            if (activeSymbols.isEmpty()) {
                log.debug("No active strategies, skipping price refresh");
                return;
            }

            log.info("Refreshing prices for {} symbols", activeSymbols);

            // Get any user's credentials (we need credentials to access market data)
            // TODO: change system-level credentials
            Long anyUserId = userStrategyRepository.findAnyActiveUserId()
                    .orElse(null);

            if (anyUserId == null) {
                log.warn("No active user found for price refresh");
                return;
            }

            fetchPrices(activeSymbols, anyUserId);

        } catch (Exception e) {
            log.error("Error refreshing active prices", e);
        }
    }

    /**
     * Clears the price cache.
     */
    public void clearCache() {
        priceCache.clear();
        log.info("Price cache cleared");
    }

    /**
     * Gets the current cache size.
     */
    public int getCacheSize() {
        return priceCache.size();
    }
}

