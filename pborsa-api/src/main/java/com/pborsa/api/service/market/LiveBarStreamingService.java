package com.pborsa.api.service.market;

import com.pborsa.api.client.tradingengine.LiveBarClient;
import com.pborsa.api.config.market.BarDataProperties;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.repository.UserStrategyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for streaming live bar updates to the trading engine.
 *
 * <p>Runs on a configurable schedule (default: every minute) and:
 * <ol>
 *   <li>Collects all symbols from active strategies</li>
 *   <li>Fetches latest bars for those symbols</li>
 *   <li>Sends them to the trading engine via gRPC</li>
 * </ol>
 *
 * <p>Design considerations:
 * <ul>
 *   <li>Efficient: Batch fetches bars for all symbols in one call</li>
 *   <li>Resilient: Failures are logged but don't stop the scheduler</li>
 *   <li>Configurable: Interval and enabled flag via properties</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LiveBarStreamingService {

    private final BarDataService barDataService;
    private final LiveBarClient liveBarClient;
    private final BarDataProperties barDataProperties;
    private final UserStrategyRepository userStrategyRepository;

    /**
     * Scheduled task to stream live bars to trading engine.
     * Runs at fixed rate configured via bar.data.streaming-interval.
     */
    @Scheduled(fixedRateString = "#{@barDataProperties.streamingIntervalSeconds * 1000}")
    public void streamLiveBars() {
        if (!barDataProperties.isStreamingEnabled()) {
            log.trace("Bar streaming is disabled");
            return;
        }

        if (!liveBarClient.isEnabled()) {
            log.trace("Live bar client is disabled");
            return;
        }

        try {
            streamBarsToTradingEngine();
        } catch (Exception e) {
            log.error("Error streaming live bars to trading engine", e);
            // Don't rethrow - let scheduler continue
        }
    }

    /**
     * Main streaming logic - can also be called manually for testing.
     */
    public void streamBarsToTradingEngine() {
        // Get all active strategy symbols and their strategy IDs
        Map<String, List<Long>> symbolToStrategies = getActiveSymbolsWithStrategies();

        if (symbolToStrategies.isEmpty()) {
            log.debug("No active strategies, skipping bar streaming");
            return;
        }

        Set<String> symbols = symbolToStrategies.keySet();
        log.info("Streaming live bars for {} symbols to trading engine", symbols.size());

        // Get user ID for API access (use any active user)
        Long userId = getAnyActiveUserId();
        if (userId == null) {
            log.warn("No active user found for bar streaming");
            return;
        }

        // Fetch latest bars for all symbols
        String timeframe = barDataProperties.getDefaultTimeframe();
        Map<String, StockBarDto> latestBars = barDataService.getLatestBars(userId, symbols, timeframe);

        if (latestBars.isEmpty()) {
            log.debug("No bars fetched for active symbols");
            return;
        }

        // Collect all strategy IDs that are interested
        List<Long> allStrategyIds = latestBars.keySet()
                        .stream()
                        .map(symbolToStrategies::get)
                        .filter(Objects::nonNull)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());


        // Send to trading engine
        LiveBarClient.LiveBarResult result = liveBarClient.sendLiveBars(
                latestBars, timeframe, allStrategyIds
        );

        if (result.success()) {
            log.info("Successfully sent {} bars to trading engine", result.barsProcessed());
        } else {
            log.warn("Failed to send bars to trading engine: {}", result.message());
        }
    }

    /**
     * Gets a map of symbol -> list of strategy IDs for all active strategies.
     */
    private Map<String, List<Long>> getActiveSymbolsWithStrategies() {
        try {
            List<Object[]> results = userStrategyRepository.findActiveSymbolsWithStrategyIds();
            Map<String, List<Long>> symbolToStrategies = new HashMap<>();

            for (Object[] row : results) {
                String symbol = (String) row[0];
                Long strategyId = (Long) row[1];

                symbolToStrategies
                        .computeIfAbsent(symbol.toUpperCase(), k -> new ArrayList<>())
                        .add(strategyId);
            }

            return symbolToStrategies;
        } catch (Exception e) {
            log.error("Error fetching active symbols with strategies", e);
            return Map.of();
        }
    }

    private Long getAnyActiveUserId() {
        return userStrategyRepository.findAnyActiveUserId().orElse(null);
    }

    /**
     * Manually trigger bar streaming (for testing or admin use).
     *
     * @return result of the streaming operation
     */
    public LiveBarClient.LiveBarResult triggerManualStreaming() {
        if (!liveBarClient.isEnabled()) {
            return LiveBarClient.LiveBarResult.failure("Live bar client is disabled");
        }

        streamBarsToTradingEngine();
        return LiveBarClient.LiveBarResult.success(0);
    }
}
