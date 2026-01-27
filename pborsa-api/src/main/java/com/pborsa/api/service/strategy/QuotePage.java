package com.pborsa.api.service.strategy;

import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;

import java.util.List;

public record QuotePage(List<StockQuote> quotes, String nextPageToken) {
}
