package com.pborsa.domain.constants;

import com.pborsa.domain.dto.market.StockInfoDto;

import java.util.List;

/**
 * Static list of available stocks supported by the application.
 */
public final class AvailableStocks {

    public static final List<StockInfoDto> AVAILABLE_STOCKS = List.of(
            new StockInfoDto("AAPL", "Apple Inc."),
            new StockInfoDto("MSFT", "Microsoft Corp."),
            new StockInfoDto("AMZN", "Amazon.com Inc."),
            new StockInfoDto("NVDA", "NVIDIA Corp."),
            new StockInfoDto("GOOGL", "Alphabet Inc. (Class A)"),
            new StockInfoDto("GOOG", "Alphabet Inc. (Class C)"),
            new StockInfoDto("TSLA", "Tesla Inc."),
            new StockInfoDto("META", "Meta Platforms Inc."),
            new StockInfoDto("BRK.B", "Berkshire Hathaway Inc. (Class B)"),
            new StockInfoDto("UNH", "UnitedHealth Group Inc."),
            new StockInfoDto("JNJ", "Johnson & Johnson"),
            new StockInfoDto("V", "Visa Inc."),
            new StockInfoDto("PG", "Procter & Gamble Co."),
            new StockInfoDto("JPM", "JPMorgan Chase & Co."),
            new StockInfoDto("MA", "Mastercard Inc."),
            new StockInfoDto("LLY", "Eli Lilly & Co."),
            new StockInfoDto("HD", "Home Depot Inc."),
            new StockInfoDto("WMT", "Walmart Inc."),
            new StockInfoDto("DIS", "Walt Disney Co."),
            new StockInfoDto("BAC", "Bank of America Corp."),
            new StockInfoDto("KO", "Coca-Cola Co."),
            new StockInfoDto("PFE", "Pfizer Inc."),
            new StockInfoDto("XOM", "Exxon Mobil Corp."),
            new StockInfoDto("CSCO", "Cisco Systems Inc."),
            new StockInfoDto("INTC", "Intel Corp."),
            new StockInfoDto("VZ", "Verizon Communications Inc."),
            new StockInfoDto("ADBE", "Adobe Inc."),
            new StockInfoDto("CMCSA", "Comcast Corp."),
            new StockInfoDto("NFLX", "Netflix Inc.")
    );

    private AvailableStocks() {
        // Utility class - prevent instantiation
    }
}
