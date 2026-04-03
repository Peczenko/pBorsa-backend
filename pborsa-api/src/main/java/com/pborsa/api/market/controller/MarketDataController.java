package com.pborsa.api.market.controller;

import com.pborsa.api.shared.controller.ApiResponse;
import com.pborsa.domain.dto.market.StockBarDto;
import com.pborsa.trading.market.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Controller for market data operations.
 *
 * <p><b>Note:</b> This controller is deprecated for UI bar operations.
 * Use {@code /api/v1/bars} endpoints (BarDataController) instead.
 *
 * <p>Quote and trade endpoints have been removed.
 * For real-time data needs, use the bar streaming service.
 */
@RestController
@RequestMapping("/api/v1/market-data")
@RequiredArgsConstructor
@Slf4j
public class MarketDataController {

    private final MarketDataService marketDataService;

    /**
     * Gets historical bars.
     *
     * @deprecated Use {@code /api/v1/bars/{userId}/{symbol}/historical} instead.
     *             This endpoint will be removed in a future version.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/{userId}/bars/{symbol}")
    public ResponseEntity<ApiResponse<List<StockBarDto>>> getBars(
            @PathVariable Long userId,
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1") int timeframe,
            @RequestParam(defaultValue = "DAY") String period,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "100") Integer limit
    ) {
        log.warn("Deprecated endpoint /api/v1/market-data/{}/bars/{} called. " +
                "Use /api/v1/bars/{}/{}/historical instead.", userId, symbol, userId, symbol);

        ZonedDateTime startTime = start != null ? ZonedDateTime.parse(start) : ZonedDateTime.now().minusDays(30);
        ZonedDateTime endTime = end != null ? ZonedDateTime.parse(end) : ZonedDateTime.now();

        List<StockBarDto> bars = marketDataService.getHistoricalBars(
                userId, symbol, timeframe, period, startTime, endTime, limit);
        return ResponseEntity.ok(ApiResponse.success(bars));
    }
}
