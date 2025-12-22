package com.pborsa.api.temporal.activity;

import com.pborsa.api.domain.dto.account.AccountInfoDto;
import com.pborsa.api.domain.dto.account.PositionDto;
import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.domain.dto.trading.OrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.service.market.MarketDataService;
import com.pborsa.api.service.trading.AccountService;
import com.pborsa.api.service.trading.OrderService;
import com.pborsa.api.service.trading.PositionService;
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
    public OrderResponse placeOrder(String userId, OrderRequest orderRequest) {
        log.info("Activity: Placing order for user {} symbol {}", userId, orderRequest.symbol());
        return orderService.placeOrder(userId, orderRequest);
    }

    @Override
    public boolean cancelOrder(String userId, String orderId) {
        log.info("Activity: Cancelling order {} for user {}", orderId, userId);
        return orderService.cancelOrder(userId, orderId);
    }

    @Override
    public OrderResponse getOrder(String userId, String orderId) {
        log.debug("Activity: Getting order {} for user {}", orderId, userId);
        return orderService.getOrder(userId, orderId);
    }

    @Override
    public List<OrderResponse> getOpenOrders(String userId) {
        log.debug("Activity: Getting open orders for user {}", userId);
        return orderService.getOpenOrders(userId);
    }

    @Override
    public AccountInfoDto getAccountInfo(String userId) {
        log.debug("Activity: Getting account info for user {}", userId);
        return accountService.getAccountInfo(userId);
    }

    @Override
    public List<PositionDto> getPositions(String userId) {
        log.debug("Activity: Getting positions for user {}", userId);
        return positionService.getAllPositions(userId);
    }

    @Override
    public OrderResponse closePosition(String userId, String symbol) {
        log.info("Activity: Closing position {} for user {}", symbol, userId);
        return positionService.closePosition(userId, symbol);
    }

    @Override
    public List<StockQuoteDto> getQuotes(String userId, Collection<String> symbols) {
        log.debug("Activity: Getting quotes for user {} symbols {}", userId, symbols);
        return marketDataService.getLatestQuotes(userId, symbols);
    }

    @Override
    public boolean validateTradingAllowed(String userId) {
        log.debug("Activity: Validating trading for user {}", userId);
        return accountService.canTrade(userId);
    }
}
