package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.market.MarketDataSnapshot;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.domain.dto.market.StockTradeDto;
import com.pborsa.api.service.market.MarketDataService;
import com.pborsa.api.temporal.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for market data operations.
 */
@RestController
@RequestMapping("/api/v1/market-data")
@RequiredArgsConstructor
@Slf4j
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final WorkflowService workflowService;

    /**
     * Gets latest quotes for symbols.
     */
    @GetMapping("/{userId}/quotes")
    public ResponseEntity<ApiResponse<List<StockQuoteDto>>> getQuotes(
            @PathVariable String userId,
            @RequestParam Set<String> symbols
    ) {
        log.debug("Getting quotes for user {} symbols: {}", userId, symbols);
        List<StockQuoteDto> quotes = marketDataService.getLatestQuotes(userId, symbols);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    /**
     * Gets latest quote for a single symbol.
     */
    @GetMapping("/{userId}/quotes/{symbol}")
    public ResponseEntity<ApiResponse<StockQuoteDto>> getQuote(
            @PathVariable String userId,
            @PathVariable String symbol
    ) {
        StockQuoteDto quote = marketDataService.getLatestQuote(userId, symbol);
        return ResponseEntity.ok(ApiResponse.success(quote));
    }

    /**
     * Gets quotes asynchronously.
     */
    @GetMapping("/{userId}/quotes/async")
    public CompletableFuture<ResponseEntity<ApiResponse<List<StockQuoteDto>>>> getQuotesAsync(
            @PathVariable String userId,
            @RequestParam Set<String> symbols
    ) {
        return marketDataService.getLatestQuotesAsync(userId, symbols)
                .thenApply(quotes -> ResponseEntity.ok(ApiResponse.success(quotes)));
    }

    /**
     * Gets latest trades for symbols.
     */
    @GetMapping("/{userId}/trades")
    public ResponseEntity<ApiResponse<List<StockTradeDto>>> getTrades(
            @PathVariable String userId,
            @RequestParam Set<String> symbols
    ) {
        List<StockTradeDto> trades = marketDataService.getLatestTrades(userId, symbols);
        return ResponseEntity.ok(ApiResponse.success(trades));
    }

    /**
     * Gets historical bars.
     */
    @GetMapping("/{userId}/bars/{symbol}")
    public ResponseEntity<ApiResponse<List<StockBarDto>>> getBars(
            @PathVariable String userId,
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1") int timeframe,
            @RequestParam(defaultValue = "DAY") String period,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "100") Integer limit
    ) {
        ZonedDateTime startTime = start != null ? ZonedDateTime.parse(start) : ZonedDateTime.now().minusDays(30);
        ZonedDateTime endTime = end != null ? ZonedDateTime.parse(end) : ZonedDateTime.now();
        
        List<StockBarDto> bars = marketDataService.getHistoricalBars(
                userId, symbol, timeframe, period, startTime, endTime, limit);
        return ResponseEntity.ok(ApiResponse.success(bars));
    }

    /**
     * Gets a complete market data snapshot.
     */
    @GetMapping("/{userId}/snapshot")
    public ResponseEntity<ApiResponse<MarketDataSnapshot>> getSnapshot(
            @PathVariable String userId,
            @RequestParam Set<String> symbols
    ) {
        MarketDataSnapshot snapshot = marketDataService.getMarketDataSnapshot(userId, symbols);
        return ResponseEntity.ok(ApiResponse.success(snapshot));
    }

    /**
     * Gets snapshot asynchronously.
     */
    @GetMapping("/{userId}/snapshot/async")
    public CompletableFuture<ResponseEntity<ApiResponse<MarketDataSnapshot>>> getSnapshotAsync(
            @PathVariable String userId,
            @RequestParam Set<String> symbols
    ) {
        return marketDataService.getMarketDataSnapshotAsync(userId, symbols)
                .thenApply(snapshot -> ResponseEntity.ok(ApiResponse.success(snapshot)));
    }

    // ==================== Polling Workflows ====================

    /**
     * Starts market data polling workflow.
     */
    @PostMapping("/{userId}/polling/start")
    public ResponseEntity<ApiResponse<String>> startPolling(
            @PathVariable String userId,
            @RequestParam Set<String> symbols,
            @RequestParam(defaultValue = "5") int intervalSeconds
    ) {
        log.info("Starting market data polling for user {} symbols: {}", userId, symbols);
        String workflowId = workflowService.startMarketDataPolling(userId, symbols, intervalSeconds);
        return ResponseEntity.ok(ApiResponse.success(workflowId, "Polling started"));
    }

    /**
     * Adds symbols to polling.
     */
    @PostMapping("/{userId}/polling/symbols")
    public ResponseEntity<ApiResponse<Void>> addSymbolsToPolling(
            @PathVariable String userId,
            @RequestParam Set<String> symbols
    ) {
        workflowService.addSymbolsToPolling(userId, symbols);
        return ResponseEntity.ok(ApiResponse.success("Symbols added to polling"));
    }

    /**
     * Removes symbols from polling.
     */
    @DeleteMapping("/{userId}/polling/symbols")
    public ResponseEntity<ApiResponse<Void>> removeSymbolsFromPolling(
            @PathVariable String userId,
            @RequestParam Set<String> symbols
    ) {
        workflowService.removeSymbolsFromPolling(userId, symbols);
        return ResponseEntity.ok(ApiResponse.success("Symbols removed from polling"));
    }

    /**
     * Gets latest quotes from polling workflow.
     */
    @GetMapping("/{userId}/polling/quotes")
    public ResponseEntity<ApiResponse<List<StockQuoteDto>>> getPollingQuotes(@PathVariable String userId) {
        List<StockQuoteDto> quotes = workflowService.getLatestQuotesFromPolling(userId);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    /**
     * Stops polling workflow.
     */
    @PostMapping("/{userId}/polling/stop")
    public ResponseEntity<ApiResponse<Void>> stopPolling(@PathVariable String userId) {
        log.info("Stopping market data polling for user {}", userId);
        workflowService.stopMarketDataPolling(userId);
        return ResponseEntity.ok(ApiResponse.success("Polling stopped"));
    }
}
