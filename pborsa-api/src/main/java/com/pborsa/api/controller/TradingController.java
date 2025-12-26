package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.service.trading.OrderService;
import com.pborsa.api.temporal.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for trading operations.
 */
@RestController
@RequestMapping("/api/v1/trading")
@RequiredArgsConstructor
@Slf4j
public class TradingController {

    private final OrderService orderService;
    private final WorkflowService workflowService;

    // ==================== Orders ====================
    @PostMapping("/{userId}/orders/workflow")
    public ResponseEntity<ApiResponse<Map<String, String>>> placeOrderWithWorkflow(
            @PathVariable String userId,
            @Valid @RequestBody TradingApiOrderRequest request
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

}
