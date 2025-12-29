package com.pborsa.api.service.strategy;

import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;

import java.util.List;

public record TradePage(List<StockTrade> trades, String nextPageToken) {
}
