package com.pborsa.api.service.strategy;

import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;

import java.util.List;

public record BarPage(List<StockBar> bars, String nextPageToken) {
}
