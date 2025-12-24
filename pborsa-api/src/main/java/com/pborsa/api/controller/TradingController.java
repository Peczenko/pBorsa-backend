package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.trading.OrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.service.trading.OrderService;
import com.pborsa.api.service.trading.PositionService;
import com.pborsa.api.service.trading.TradingFacade;
import com.pborsa.api.temporal.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for trading operations.
 */
@RestController
@RequestMapping("/api/v1/trading")
@RequiredArgsConstructor
@Slf4j
public class TradingController {

    private final TradingFacade tradingFacade;
    private final OrderService orderService;
    private final PositionService positionService;
    private final WorkflowService workflowService;

    // ==================== Orders ====================

    /**
     * Places a new order.
     */
    @PostMapping("/{userId}/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @PathVariable String userId,
            @Valid @RequestBody OrderRequest request
    ) {
        log.info("Placing order for user {}: {} {} {} @ {}",
                userId, request.side(), request.quantity(), request.symbol(), request.type());
        OrderResponse order = orderService.placeOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Order placed successfully"));
    }

    /**
     * Places an order asynchronously.
     */
    @PostMapping("/{userId}/orders/async")
    public CompletableFuture<ResponseEntity<ApiResponse<OrderResponse>>> placeOrderAsync(
            @PathVariable String userId,
            @Valid @RequestBody OrderRequest request
    ) {
        return orderService.placeOrderAsync(userId, request)
                .thenApply(order -> ResponseEntity.ok(ApiResponse.success(order, "Order placed successfully")));
    }

    /**
     * Places an order using Temporal workflow.
     */
    @PostMapping("/{userId}/orders/workflow")
    public ResponseEntity<ApiResponse<Map<String, String>>> placeOrderWithWorkflow(
            @PathVariable String userId,
            @Valid @RequestBody OrderRequest request
    ) {
        log.info("Placing order via workflow for user {}", userId);
        String workflowId = workflowService.startTradeAsync(userId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("workflowId", workflowId), "Workflow started"));
    }

    /**
     * Gets an order by ID.
     */
    @GetMapping("/{userId}/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable String userId,
            @PathVariable String orderId
    ) {
        OrderResponse order = orderService.getOrder(userId, orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * Gets all orders.
     */
    @GetMapping("/{userId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @PathVariable String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") Integer limit
    ) {
        List<OrderResponse> orders = orderService.getOrders(userId, status, limit, null, null, false);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Gets open orders.
     */
    @GetMapping("/{userId}/orders/open")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOpenOrders(@PathVariable String userId) {
        List<OrderResponse> orders = orderService.getOpenOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Cancels an order.
     */
    @DeleteMapping("/{userId}/orders/{orderId}")
    public ResponseEntity<ApiResponse<Boolean>> cancelOrder(
            @PathVariable String userId,
            @PathVariable String orderId
    ) {
        log.info("Cancelling order {} for user {}", orderId, userId);
        boolean cancelled = orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok(ApiResponse.success(cancelled, "Order cancelled successfully"));
    }

    /**
     * Cancels all open orders.
     */
    @DeleteMapping("/{userId}/orders")
    public ResponseEntity<ApiResponse<Integer>> cancelAllOrders(@PathVariable String userId) {
        log.info("Cancelling all orders for user {}", userId);
        int count = orderService.cancelAllOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(count, count + " orders cancelled"));
    }

    // ==================== Quick Trading ====================

    /**
     * Quick market buy.
     */
    @PostMapping("/{userId}/buy/{symbol}")
    public ResponseEntity<ApiResponse<OrderResponse>> marketBuy(
            @PathVariable String userId,
            @PathVariable String symbol,
            @RequestParam BigDecimal quantity
    ) {
        OrderResponse order = tradingFacade.marketBuy(userId, symbol, quantity);
        return ResponseEntity.ok(ApiResponse.success(order, "Buy order placed"));
    }

    /**
     * Quick market sell.
     */
    @PostMapping("/{userId}/sell/{symbol}")
    public ResponseEntity<ApiResponse<OrderResponse>> marketSell(
            @PathVariable String userId,
            @PathVariable String symbol,
            @RequestParam BigDecimal quantity
    ) {
        OrderResponse order = tradingFacade.marketSell(userId, symbol, quantity);
        return ResponseEntity.ok(ApiResponse.success(order, "Sell order placed"));
    }

    /**
     * Limit buy.
     */
    @PostMapping("/{userId}/limit-buy/{symbol}")
    public ResponseEntity<ApiResponse<OrderResponse>> limitBuy(
            @PathVariable String userId,
            @PathVariable String symbol,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal price
    ) {
        OrderResponse order = tradingFacade.limitBuy(userId, symbol, quantity, price);
        return ResponseEntity.ok(ApiResponse.success(order, "Limit buy order placed"));
    }

    /**
     * Limit sell.
     */
    @PostMapping("/{userId}/limit-sell/{symbol}")
    public ResponseEntity<ApiResponse<OrderResponse>> limitSell(
            @PathVariable String userId,
            @PathVariable String symbol,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal price
    ) {
        OrderResponse order = tradingFacade.limitSell(userId, symbol, quantity, price);
        return ResponseEntity.ok(ApiResponse.success(order, "Limit sell order placed"));
    }

    // ==================== Position Management ====================

    /**
     * Closes a position.
     */
    @DeleteMapping("/{userId}/positions/{symbol}")
    public ResponseEntity<ApiResponse<OrderResponse>> closePosition(
            @PathVariable String userId,
            @PathVariable String symbol
    ) {
        log.info("Closing position {} for user {}", symbol, userId);
        OrderResponse order = positionService.closePosition(userId, symbol);
        return ResponseEntity.ok(ApiResponse.success(order, "Position close order placed"));
    }

    /**
     * Closes a partial position.
     */
    @DeleteMapping("/{userId}/positions/{symbol}/partial")
    public ResponseEntity<ApiResponse<OrderResponse>> closePartialPosition(
            @PathVariable String userId,
            @PathVariable String symbol,
            @RequestParam BigDecimal quantity
    ) {
        OrderResponse order = positionService.closePartialPosition(userId, symbol, quantity);
        return ResponseEntity.ok(ApiResponse.success(order, "Partial position close order placed"));
    }

    /**
     * Liquidates all positions.
     */
    @DeleteMapping("/{userId}/positions")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> liquidateAll(
            @PathVariable String userId,
            @RequestParam(defaultValue = "true") boolean cancelOrders
    ) {
        log.warn("Liquidating all positions for user {}", userId);
        List<OrderResponse> orders = positionService.closeAllPositions(userId, cancelOrders);
        return ResponseEntity.ok(ApiResponse.success(orders, "All positions liquidated"));
    }

    // ==================== Dashboard ====================

    /**
     * Gets trading dashboard.
     */
    @GetMapping("/{userId}/dashboard")
    public CompletableFuture<ResponseEntity<ApiResponse<TradingFacade.TradingDashboard>>> getDashboard(
            @PathVariable String userId
    ) {
        return tradingFacade.getDashboard(userId)
                .thenApply(dashboard -> ResponseEntity.ok(ApiResponse.success(dashboard)));
    }
}
