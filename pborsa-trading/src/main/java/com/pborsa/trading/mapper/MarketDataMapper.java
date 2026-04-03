package com.pborsa.trading.mapper;

import com.pborsa.domain.dto.market.StockBarDto;
import com.pborsa.domain.dto.market.StockQuoteDto;
import com.pborsa.domain.dto.market.StockTradeDto;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper for converting Alpaca market data models to DTOs.
 */
@Component
@Slf4j
public class MarketDataMapper {

    /**
     * Converts an Alpaca Quote object to StockQuoteDto.
     */
    public StockQuoteDto toStockQuoteDto(StockQuote quote, String symbol) {
        if (quote == null) {
            return null;
        }

        return StockQuoteDto.builder()
                .symbol(symbol)
                .askPrice(quote.getAp() != null ? BigDecimal.valueOf(quote.getAp()) : null)
                .askSize(quote.getAs() != null ? new BigDecimal(quote.getAs()) : null)
                .bidPrice(quote.getBp() != null ? BigDecimal.valueOf(quote.getBp()) : null)
                .bidSize(quote.getBs() != null ? new BigDecimal(quote.getBs()) : null)
                .timestamp(quote.getT() != null ? quote.getT().toInstant() : null)
                .bidExchange(quote.getBx())
                .askExchange(quote.getAx())
                .tape(quote.getZ() != null ? quote.getZ().getValue() : null)
                .conditions(quote.getC() != null && !quote.getC().isEmpty()
                        ? String.join(",", quote.getC())
                        : null)
                .build();
    }

    /**
     * Converts an Alpaca Trade object to StockTradeDto.
     */
    public StockTradeDto toStockTradeDto(StockTrade trade, String symbol) {
        if (trade == null) {
            return null;
        }

        return StockTradeDto.builder()
                .symbol(symbol)
                .price(BigDecimal.valueOf(trade.getP()))
                .size(new BigDecimal(trade.getS()))
                .exchange(trade.getX())
                .timestamp(trade.getT().toInstant())
                .tradeId(String.valueOf(trade.getI()))
                .tape(trade.getZ().getValue())
                .conditions(!trade.getC().isEmpty()
                        ? String.join(",", trade.getC())
                        : null)
                .build();
    }

    /**
     * Converts an Alpaca Bar object to StockBarDto.
     */
    public StockBarDto toStockBarDto(StockBar bar, String symbol) {
        if (bar == null) {
            return null;
        }

        return StockBarDto.builder()
                .symbol(symbol)
                .open(bar.getO() != null ? BigDecimal.valueOf(bar.getO()) : null)
                .high(bar.getH() != null ? BigDecimal.valueOf(bar.getH()) : null)
                .low(bar.getL() != null ? BigDecimal.valueOf(bar.getL()) : null)
                .close(bar.getC() != null ? BigDecimal.valueOf(bar.getC()) : null)
                .volume(bar.getV() != null ? bar.getV() : null)
                .timestamp(bar.getT() != null ? bar.getT().toInstant() : null)
                .vwap(bar.getVw() != null ? BigDecimal.valueOf(bar.getVw()) : null)
                .tradeCount(bar.getN() != null ? bar.getN() : null)
                .build();
    }

}
