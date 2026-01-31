package com.pborsa.api.service.backtest;

import com.pborsa.api.config.backtest.BacktestProperties;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.service.mapper.MarketDataMapper;
import com.pborsa.api.service.strategy.AlpacaBarFetcher;
import com.pborsa.api.service.strategy.BarPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for fetching historical data for backtesting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BacktestDataFetcher {

    private final UserCredentialsService credentialsService;
    private final AlpacaClientFactory alpacaClientFactory;
    private final AlpacaBarFetcher barFetcher;
    private final MarketDataMapper marketDataMapper;
    private final BacktestProperties backtestProperties;

    /**
     * Fetches historical bars before the testing start date (for context).
     *
     * @param userId       User ID for credentials
     * @param symbol       Stock symbol
     * @param testingStart Start of testing period
     * @param heartbeat    Heartbeat callback for long-running fetches
     * @return List of bars before testing start
     */
    public List<StockBarDto> fetchHistoryBeforeStart(
            Long userId,
            String symbol,
            Instant testingStart,
            Runnable heartbeat
    ) throws ApiException {
        ZonedDateTime startZdt = ZonedDateTime.ofInstant(testingStart, ZoneOffset.UTC);
        Instant lookbackStart = startZdt.minus(backtestProperties.getHistoryLookbackPeriod()).toInstant();

        log.info("Fetching history before start for symbol {} from {} to {}",
                symbol, lookbackStart, testingStart);

        return fetchBars(userId, symbol, lookbackStart, testingStart, heartbeat);
    }

    /**
     * Fetches historical bars for the testing period.
     *
     * @param userId       User ID for credentials
     * @param symbol       Stock symbol
     * @param testingStart Start of testing period
     * @param testingEnd   End of testing period
     * @param heartbeat    Heartbeat callback for long-running fetches
     * @return List of bars for testing period
     */
    public List<StockBarDto> fetchHistoryTestingRange(
            Long userId,
            String symbol,
            Instant testingStart,
            Instant testingEnd,
            Runnable heartbeat
    ) throws ApiException {
        log.info("Fetching history for testing range symbol {} from {} to {}",
                symbol, testingStart, testingEnd);

        return fetchBars(userId, symbol, testingStart, testingEnd, heartbeat);
    }

    private List<StockBarDto> fetchBars(
            Long userId,
            String symbol,
            Instant start,
            Instant end,
            Runnable heartbeat
    ) throws ApiException {
        AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
        AlpacaAPI client = alpacaClientFactory.getOrCreateClient(credentials);

        String normalizedSymbol = symbol.toUpperCase();
        List<StockBarDto> allBars = new ArrayList<>();
        String nextPageToken = null;
        int totalFetched = 0;
        int maxBars = backtestProperties.getMaxBarsPerRequest();

        do {
            BarPage page = barFetcher.fetchPage(
                    client,
                    "backtest",
                    normalizedSymbol,
                    start,
                    end,
                    backtestProperties.getTimeframe(),
                    backtestProperties.getPageLimit(),
                    nextPageToken,
                    backtestProperties.getStockFeed(),
                    heartbeat != null ? heartbeat : () -> {}
            );

            for (StockBar bar : page.bars()) {
                allBars.add(marketDataMapper.toStockBarDto(bar, normalizedSymbol));
                totalFetched++;

                if (totalFetched >= maxBars) {
                    log.warn("Reached max bars limit ({}) for backtest fetch. Truncating.", maxBars);
                    return allBars;
                }
            }

            nextPageToken = page.nextPageToken();

            if (heartbeat != null) {
                heartbeat.run();
            }

            log.debug("Fetched {} bars (total: {}), nextPageToken: {}",
                    page.bars().size(), totalFetched, nextPageToken);

        } while (nextPageToken != null && !nextPageToken.isBlank());

        log.info("Completed fetching {} bars for symbol {}", totalFetched, normalizedSymbol);
        return allBars;
    }
}
