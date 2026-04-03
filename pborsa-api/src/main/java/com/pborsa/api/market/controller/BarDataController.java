package com.pborsa.api.market.controller;

import com.pborsa.api.market.config.BarDataProperties;
import com.pborsa.api.shared.config.openapi.ApiResponseDoc;
import com.pborsa.api.shared.config.openapi.model.ApiErrorResponseDoc;
import com.pborsa.api.shared.config.openapi.model.BarDataConfigResponseDoc;
import com.pborsa.api.shared.config.openapi.model.StockBarListResponseDoc;
import com.pborsa.api.shared.config.openapi.model.StockBarMapResponseDoc;
import com.pborsa.api.shared.config.openapi.model.StockBarResponseDoc;
import com.pborsa.api.shared.controller.ApiResponse;
import com.pborsa.domain.dto.market.StockBarDto;
import com.pborsa.api.market.service.BarDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for bar (OHLCV) data for UI charts.
 *
 * <p>Provides clean, focused API for frontend chart rendering:
 * <ul>
 *   <li>Historical bars for initial chart load</li>
 *   <li>Latest bar for periodic updates (polling)</li>
 *   <li>Multi-symbol latest bars for dashboards</li>
 *   <li>Configuration endpoint for supported timeframes</li>
 * </ul>
 *
 * <p>Design decisions:
 * <ul>
 *   <li>REST polling preferred over WebSocket (1 req/min is acceptable overhead)</li>
 *   <li>All timestamps in ISO-8601 format</li>
 *   <li>Configurable limits prevent abuse</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/bars")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bar Data", description = "OHLCV bar data for charts")
public class BarDataController {

    private final BarDataService barDataService;
    private final BarDataProperties barDataProperties;

    /**
     * Gets historical bars for chart rendering.
     * Use this for initial chart load.
     *
     * @param userId    user ID for authentication
     * @param symbol    stock symbol (e.g., "AAPL")
     * @param timeframe bar timeframe (e.g., "1Min", "5Min", "1Hour", "1Day")
     * @param start     start time in ISO-8601 format (optional, defaults to lookback period)
     * @param end       end time in ISO-8601 format (optional, defaults to now)
     * @param limit     maximum number of bars (optional, defaults to 500)
     * @return list of bars sorted by timestamp ascending
     */
    @GetMapping("/{userId}/{symbol}/historical")
    @Operation(summary = "Get historical bars for chart",
            description = "Fetches historical OHLCV bars for a symbol. Use this for initial chart load. " +
                    "Bars are sorted by timestamp ascending.")
    @ApiResponseDoc(code = "200", description = "Historical bars retrieved successfully", implementation = StockBarListResponseDoc.class)
    @ApiResponseDoc(code = "400", description = "Invalid request parameters", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<List<StockBarDto>>> getHistoricalBars(
            @Parameter(description = "User ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "Stock symbol", example = "AAPL") @PathVariable String symbol,
            @Parameter(description = "Timeframe: 1Min, 5Min, 15Min, 30Min, 1Hour, 4Hour, 1Day, 1Week", example = "5Min")
            @RequestParam(defaultValue = "5Min") String timeframe,
            @Parameter(description = "Start time in ISO-8601 format", example = "2024-01-01T00:00:00Z")
            @RequestParam(required = false) Instant start,
            @Parameter(description = "End time in ISO-8601 format", example = "2024-01-31T23:59:59Z")
            @RequestParam(required = false) Instant end,
            @Parameter(description = "Maximum bars to return (default: 500)", example = "500")
            @RequestParam(required = false) Integer limit
    ) {
        log.debug("Getting historical bars: userId={}, symbol={}, timeframe={}", userId, symbol, timeframe);

        List<StockBarDto> bars = barDataService.getHistoricalBars(
                userId, symbol.toUpperCase(), timeframe, start, end, limit
        );

        return ResponseEntity.ok(ApiResponse.success(bars));
    }

    /**
     * Gets bars optimized for chart display.
     * Automatically calculates time range based on bar count.
     *
     * @param userId    user ID for authentication
     * @param symbol    stock symbol
     * @param timeframe bar timeframe
     * @param count     number of bars for the chart (default 200)
     * @return list of bars for chart rendering
     */
    @GetMapping("/{userId}/{symbol}/chart")
    @Operation(summary = "Get bars for chart display",
            description = "Fetches a specific number of bars optimized for chart display. " +
                    "Automatically calculates the appropriate time range based on the requested count.")
    @ApiResponseDoc(code = "200", description = "Chart bars retrieved successfully", implementation = StockBarListResponseDoc.class)
    @ApiResponseDoc(code = "400", description = "Invalid request parameters", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<List<StockBarDto>>> getBarsForChart(
            @Parameter(description = "User ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "Stock symbol", example = "AAPL") @PathVariable String symbol,
            @Parameter(description = "Timeframe: 1Min, 5Min, 15Min, 30Min, 1Hour, 4Hour, 1Day, 1Week", example = "5Min")
            @RequestParam(defaultValue = "5Min") String timeframe,
            @Parameter(description = "Number of bars to return for the chart", example = "200")
            @RequestParam(defaultValue = "200") int count
    ) {
        log.debug("Getting chart bars: userId={}, symbol={}, timeframe={}, count={}",
                userId, symbol, timeframe, count);

        List<StockBarDto> bars = barDataService.getBarsForChart(
                userId, symbol.toUpperCase(), timeframe, count
        );

        return ResponseEntity.ok(ApiResponse.success(bars));
    }

    /**
     * Gets the latest (most recent) bar for a symbol.
     * Use this for polling updates (e.g., every 30-60 seconds).
     *
     * @param userId    user ID for authentication
     * @param symbol    stock symbol
     * @param timeframe bar timeframe (should match chart timeframe)
     * @return the latest bar
     */
    @GetMapping("/{userId}/{symbol}/latest")
    @Operation(summary = "Get latest bar for polling updates",
            description = "Fetches the most recent bar for a symbol. Use this for polling updates (e.g., every 30-60 seconds). " +
                    "The timeframe should match the chart's timeframe.")
    @ApiResponseDoc(code = "200", description = "Latest bar retrieved successfully", implementation = StockBarResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<StockBarDto>> getLatestBar(
            @Parameter(description = "User ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "Stock symbol", example = "AAPL") @PathVariable String symbol,
            @Parameter(description = "Timeframe: 1Min, 5Min, 15Min, 30Min, 1Hour, 4Hour, 1Day, 1Week", example = "1Min")
            @RequestParam(defaultValue = "1Min") String timeframe
    ) {
        log.debug("Getting latest bar: userId={}, symbol={}, timeframe={}", userId, symbol, timeframe);

        StockBarDto bar = barDataService.getLatestBar(userId, symbol.toUpperCase(), timeframe);

        if (bar == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No bar data available"));
        }

        return ResponseEntity.ok(ApiResponse.success(bar));
    }

    /**
     * Gets latest bars for multiple symbols.
     * Use this for dashboard widgets showing multiple assets.
     *
     * @param userId    user ID for authentication
     * @param symbols   comma-separated list of symbols
     * @param timeframe bar timeframe
     * @return map of symbol to latest bar
     */
    @GetMapping("/{userId}/latest")
    @Operation(summary = "Get latest bars for multiple symbols",
            description = "Fetches the latest bar for multiple symbols in a single request. " +
                    "Use this for dashboard widgets showing multiple assets.")
    @ApiResponseDoc(code = "200", description = "Latest bars retrieved successfully", implementation = StockBarMapResponseDoc.class)
    @ApiResponseDoc(code = "400", description = "Invalid request parameters", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<Map<String, StockBarDto>>> getLatestBars(
            @Parameter(description = "User ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "Comma-separated list of symbols", example = "AAPL,GOOGL,MSFT")
            @RequestParam Set<String> symbols,
            @Parameter(description = "Timeframe: 1Min, 5Min, 15Min, 30Min, 1Hour, 4Hour, 1Day, 1Week", example = "1Min")
            @RequestParam(defaultValue = "1Min") String timeframe
    ) {
        log.debug("Getting latest bars: userId={}, symbols={}, timeframe={}", userId, symbols, timeframe);

        // Normalize symbols to uppercase
        Set<String> normalizedSymbols = symbols.stream()
                .map(String::toUpperCase)
                .collect(java.util.stream.Collectors.toSet());

        Map<String, StockBarDto> bars = barDataService.getLatestBars(userId, normalizedSymbols, timeframe);

        return ResponseEntity.ok(ApiResponse.success(bars));
    }

    /**
     * Gets available configuration for bar data.
     * Use this to populate UI dropdowns for timeframe selection.
     *
     * @return configuration including supported timeframes
     */
    @GetMapping("/config")
    @Operation(summary = "Get bar data configuration",
            description = "Returns the available configuration settings for bar data including " +
                    "supported timeframes, default values, and limits. Use this to populate UI dropdowns.")
    @ApiResponseDoc(code = "200", description = "Configuration retrieved successfully", implementation = BarDataConfigResponseDoc.class)
    public ResponseEntity<ApiResponse<BarDataConfig>> getConfig() {
        return ResponseEntity.ok(ApiResponse.success(new BarDataConfig(
                barDataProperties.getSupportedTimeframes(),
                barDataProperties.getDefaultTimeframe(),
                barDataProperties.getDefaultBarsLimit(),
                barDataProperties.getMaxBarsPerRequest(),
                barDataProperties.getStreamingIntervalSeconds()
        )));
    }

    /**
     * Configuration DTO for bar data settings.
     */
    public record BarDataConfig(
            List<String> supportedTimeframes,
            String defaultTimeframe,
            int defaultBarsLimit,
            int maxBarsPerRequest,
            long updateIntervalSeconds
    ) {}
}
