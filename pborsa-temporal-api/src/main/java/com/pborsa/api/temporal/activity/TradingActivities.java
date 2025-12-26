package com.pborsa.api.temporal.activity;

import com.pborsa.api.domain.dto.account.AccountInfoDto;
import com.pborsa.api.domain.dto.account.PositionDto;
import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.Collection;
import java.util.List;

/**
 * Temporal activities interface for trading operations.
 * Each method represents an atomic, retriable operation.
 */
@ActivityInterface
public interface TradingActivities {

    /**
     * Places an order.
     */
    @ActivityMethod
    OrderResponse placeOrder(String userId, TradingApiOrderRequest tradingApiOrderRequest);


    /**
     * Cancels an order.
     */
    @ActivityMethod
    boolean cancelOrder(String userId, String orderId);

    /**
     * Gets an order by ID.
     */
    @ActivityMethod
    OrderResponse getOrder(String userId, String orderId);

    /**
     * Gets all open orders.
     */
    @ActivityMethod
    List<OrderResponse> getOpenOrders(String userId);

    /**
     * Gets account information.
     */
    @ActivityMethod
    AccountInfoDto getAccountInfo(String userId);

    /**
     * Gets all positions.
     */
    @ActivityMethod
    List<PositionDto> getPositions(String userId);

    /**
     * Closes a position.
     */
    @ActivityMethod
    OrderResponse closePosition(String userId, String symbol);

    /**
     * Gets the latest quotes for symbols.
     */
    @ActivityMethod
    List<StockQuoteDto> getQuotes(String userId, Collection<String> symbols);

    /**
     * Validates that the user can trade.
     */
    @ActivityMethod
    boolean validateTradingAllowed(String userId);
}

