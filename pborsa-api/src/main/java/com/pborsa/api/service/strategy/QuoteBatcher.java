package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.service.mapper.MarketDataMapper;
import lombok.RequiredArgsConstructor;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class QuoteBatcher {

    private final MarketDataMapper marketDataMapper;

    public void forEachBatch(List<StockQuote> quotes,
                             String symbol,
                             int batchSize,
                             Consumer<List<StockQuoteDto>> batchConsumer) {
        if (quotes == null || quotes.isEmpty()) {
            return;
        }
        Objects.requireNonNull(batchConsumer, "batchConsumer");

        List<StockQuoteDto> chunk = new ArrayList<>(batchSize);
        for (StockQuote quote : quotes) {
            chunk.add(marketDataMapper.toStockQuoteDto(quote, symbol));
            if (chunk.size() >= batchSize) {
                batchConsumer.accept(List.copyOf(chunk));
                chunk.clear();
            }
        }
        if (!chunk.isEmpty()) {
            batchConsumer.accept(List.copyOf(chunk));
        }
    }
}
