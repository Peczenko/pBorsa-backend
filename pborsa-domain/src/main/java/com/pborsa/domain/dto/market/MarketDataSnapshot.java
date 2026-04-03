package com.pborsa.domain.dto.market;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * DTO representing a complete market data snapshot for multiple symbols.
 */
@Builder
public record MarketDataSnapshot(
        List<StockQuoteDto> quotes,
        List<StockTradeDto> latestTrades,
        List<StockBarDto> latestBars,
        Instant snapshotTimestamp,
        String dataSource
) {
    /**
     * Creates an empty snapshot.
     */
    public static MarketDataSnapshot empty() {
        return MarketDataSnapshot.builder()
                .quotes(List.of())
                .latestTrades(List.of())
                .latestBars(List.of())
                .snapshotTimestamp(Instant.now())
                .dataSource("NONE")
                .build();
    }

    /**
     * Checks if the snapshot contains any data.
     */
    public boolean isEmpty() {
        return (quotes == null || quotes.isEmpty())
                && (latestTrades == null || latestTrades.isEmpty())
                && (latestBars == null || latestBars.isEmpty());
    }

    /**
     * Returns the number of symbols in this snapshot.
     */
    public int symbolCount() {
        if (quotes == null) return 0;
        return quotes.size();
    }
}

