package com.pborsa.api.domain.dto.market;

/**
 * DTO representing a supported stock symbol and display name.
 */
public record StockInfoDto(
        String symbol,
        String name
) {
}
