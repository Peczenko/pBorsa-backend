package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.dto.market.StockTradeDto;
import com.pborsa.api.service.mapper.MarketDataMapper;
import lombok.RequiredArgsConstructor;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class TradeBatcher {

    private final MarketDataMapper marketDataMapper;

    public void forEachBatch(List<StockTrade> trades,
                             String symbol,
                             int batchSize,
                             Consumer<List<StockTradeDto>> batchConsumer) {
        if (trades == null || trades.isEmpty()) {
            return;
        }
        Objects.requireNonNull(batchConsumer, "batchConsumer");

        List<StockTradeDto> chunk = new ArrayList<>(batchSize);
        for (StockTrade trade : trades) {
            chunk.add(marketDataMapper.toStockTradeDto(trade, symbol));
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
