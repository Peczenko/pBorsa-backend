package com.pborsa.trading.temporal.activity;

import com.pborsa.temporal.activity.TradingActivities;
import com.pborsa.domain.dto.account.AccountInfoDto;
import com.pborsa.domain.dto.account.PositionDto;
import com.pborsa.domain.dto.market.StockQuoteDto;
import com.pborsa.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.domain.dto.trading.OrderResponse;
import com.pborsa.trading.market.MarketDataService;
import com.pborsa.trading.account.AccountService;
import com.pborsa.trading.account.OrderService;
import com.pborsa.trading.account.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of trading activities for Temporal workflows.
 * Each activity is idempotent and retriable.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradingActivitiesImpl implements TradingActivities {

    private final OrderService orderService;
    private final AccountService accountService;
    private final PositionService positionService;
    private final MarketDataService marketDataService;

    @Override
    public OrderResponse placeOrder(Long userId, TradingApiOrderRequest tradingApiOrderRequest) {
        log.info("Activity: Placing order for user {} symbol {}", userId, tradingApiOrderRequest.symbol());
        return orderService.placeOrder(userId, tradingApiOrderRequest);
    }

    @Override
    public boolean cancelOrder(Long userId, String orderId) {
        log.info("Activity: Cancelling order {} for user {}", orderId, userId);
        return orderService.cancelOrder(userId, orderId);
    }

    @Override
    public OrderResponse getOrder(Long userId, String orderId) {
        log.debug("Activity: Getting order {} for user {}", orderId, userId);
        return orderService.getOrder(userId, orderId);
    }

    @Override
    public List<OrderResponse> getOpenOrders(Long userId) {
        log.debug("Activity: Getting open orders for user {}", userId);
        return orderService.getOpenOrders(userId);
    }

    @Override
    public AccountInfoDto getAccountInfo(Long userId) {
        log.debug("Activity: Getting account info for user {}", userId);
        return accountService.getAccountInfo(userId);
    }

    @Override
    public List<PositionDto> getPositions(Long userId) {
        log.debug("Activity: Getting positions for user {}", userId);
        return positionService.getAllPositions(userId);
    }

    @Override
    public OrderResponse closePosition(Long userId, String symbol) {
        log.info("Activity: Closing position {} for user {}", symbol, userId);
        return positionService.closePosition(userId, symbol);
    }

    @Override
    public List<StockQuoteDto> getQuotes(Long userId, Collection<String> symbols) {
        log.debug("Activity: Getting quotes for user {} symbols {}", userId, symbols);
        return marketDataService.getLatestQuotes(userId, symbols);
    }

    @Override
    public boolean validateTradingAllowed(Long userId) {
        log.debug("Activity: Validating trading for user {}", userId);
        return accountService.canTrade(userId);
    }
}
