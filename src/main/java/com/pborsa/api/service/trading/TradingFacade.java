package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.account.AccountInfoDto;
import com.pborsa.api.domain.dto.account.PositionDto;
import com.pborsa.api.domain.dto.market.MarketDataSnapshot;
import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.domain.dto.trading.*;
import com.pborsa.api.service.market.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Facade service that provides a unified interface for trading operations.
 * Simplifies interaction by combining account, order, position, and market data services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradingFacade {

    private final AccountService accountService;
    private final OrderService orderService;
    private final PositionService positionService;
    private final MarketDataService marketDataService;

    // ==================== Quick Trading Methods ====================

    /**
     * Places a market buy order.
     */
    public OrderResponse marketBuy(String userId, String symbol, BigDecimal quantity) {
        log.info("User {} market buy {} shares of {}", userId, quantity, symbol);
        OrderRequest request = OrderRequest.marketBuy(symbol, quantity);
        return orderService.placeOrder(userId, request);
    }

    /**
     * Places a market sell order.
     */
    public OrderResponse marketSell(String userId, String symbol, BigDecimal quantity) {
        log.info("User {} market sell {} shares of {}", userId, quantity, symbol);
        OrderRequest request = OrderRequest.marketSell(symbol, quantity);
        return orderService.placeOrder(userId, request);
    }

    /**
     * Places a limit buy order.
     */
    public OrderResponse limitBuy(String userId, String symbol, BigDecimal quantity, BigDecimal limitPrice) {
        log.info("User {} limit buy {} shares of {} at {}", userId, quantity, symbol, limitPrice);
        OrderRequest request = OrderRequest.limitBuy(symbol, quantity, limitPrice);
        return orderService.placeOrder(userId, request);
    }

    /**
     * Places a limit sell order.
     */
    public OrderResponse limitSell(String userId, String symbol, BigDecimal quantity, BigDecimal limitPrice) {
        log.info("User {} limit sell {} shares of {} at {}", userId, quantity, symbol, limitPrice);
        OrderRequest request = OrderRequest.limitSell(symbol, quantity, limitPrice);
        return orderService.placeOrder(userId, request);
    }

    // ==================== Async Trading Methods ====================

    /**
     * Async market buy.
     */
    @Async("tradingExecutor")
    public CompletableFuture<OrderResponse> marketBuyAsync(String userId, String symbol, BigDecimal quantity) {
        return CompletableFuture.completedFuture(marketBuy(userId, symbol, quantity));
    }

    /**
     * Async market sell.
     */
    @Async("tradingExecutor")
    public CompletableFuture<OrderResponse> marketSellAsync(String userId, String symbol, BigDecimal quantity) {
        return CompletableFuture.completedFuture(marketSell(userId, symbol, quantity));
    }

    // ==================== Position Management ====================

    /**
     * Gets all positions with market data.
     */
    public List<PositionDto> getPositionsWithCurrentPrices(String userId) {
        return positionService.getAllPositions(userId);
    }

    /**
     * Closes a position and returns the order.
     */
    public OrderResponse closePosition(String userId, String symbol) {
        return positionService.closePosition(userId, symbol);
    }

    /**
     * Liquidates all positions.
     */
    public List<OrderResponse> liquidateAll(String userId) {
        log.warn("User {} liquidating all positions", userId);
        return positionService.closeAllPositions(userId, true);
    }

    // ==================== Order Management ====================

    /**
     * Gets open orders.
     */
    public List<OrderResponse> getOpenOrders(String userId) {
        return orderService.getOpenOrders(userId);
    }

    /**
     * Cancels an order.
     */
    public boolean cancelOrder(String userId, String orderId) {
        return orderService.cancelOrder(userId, orderId);
    }

    /**
     * Cancels all orders.
     */
    public int cancelAllOrders(String userId) {
        return orderService.cancelAllOrders(userId);
    }

    // ==================== Account Info ====================

    /**
     * Gets account summary.
     */
    public AccountInfoDto getAccountSummary(String userId) {
        return accountService.getAccountInfo(userId);
    }

    /**
     * Gets buying power.
     */
    public BigDecimal getBuyingPower(String userId) {
        return accountService.getBuyingPower(userId);
    }

    /**
     * Checks if trading is allowed.
     */
    public boolean canTrade(String userId) {
        return accountService.canTrade(userId);
    }

    // ==================== Market Data ====================

    /**
     * Gets market data snapshot for symbols.
     */
    public MarketDataSnapshot getMarketSnapshot(String userId, Collection<String> symbols) {
        return marketDataService.getMarketDataSnapshot(userId, symbols);
    }

    /**
     * Gets latest quote for a symbol.
     */
    public StockQuoteDto getQuote(String userId, String symbol) {
        return marketDataService.getLatestQuote(userId, symbol);
    }

    // ==================== Combined Operations ====================

    /**
     * Gets a complete trading dashboard with account, positions, and open orders.
     */
    @Async("tradingExecutor")
    public CompletableFuture<TradingDashboard> getDashboard(String userId) {
        CompletableFuture<AccountInfoDto> accountFuture = accountService.getAccountInfoAsync(userId);
        CompletableFuture<List<PositionDto>> positionsFuture = positionService.getAllPositionsAsync(userId);
        
        return CompletableFuture.allOf(accountFuture, positionsFuture)
                .thenApply(v -> TradingDashboard.builder()
                        .account(accountFuture.join())
                        .positions(positionsFuture.join())
                        .openOrders(getOpenOrders(userId))
                        .build());
    }

    /**
     * Trading dashboard record combining account info, positions, and orders.
     */
    @lombok.Builder
    public record TradingDashboard(
            AccountInfoDto account,
            List<PositionDto> positions,
            List<OrderResponse> openOrders
    ) {
        public BigDecimal totalPortfolioValue() {
            if (positions == null) return BigDecimal.ZERO;
            return positions.stream()
                    .map(PositionDto::marketValue)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public BigDecimal totalUnrealizedPnL() {
            if (positions == null) return BigDecimal.ZERO;
            return positions.stream()
                    .map(PositionDto::unrealizedPnL)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public int positionCount() {
            return positions != null ? positions.size() : 0;
        }

        public int openOrderCount() {
            return openOrders != null ? openOrders.size() : 0;
        }
    }
}

