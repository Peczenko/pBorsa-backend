package com.pborsa.api.controller;

import com.pborsa.api.config.openapi.ApiResponseDoc;
import com.pborsa.api.config.openapi.model.ApiErrorResponseDoc;
import com.pborsa.api.config.openapi.model.OrderDetailListResponseDoc;
import com.pborsa.api.config.openapi.model.OrderDetailResponseDoc;
import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.strategy.OrderDetailDto;
import com.pborsa.api.security.FirebaseUserPrincipal;
import com.pborsa.api.service.security.SecurityService;
import com.pborsa.api.service.trading.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for order-related endpoints.
 * Handles HTTP requests for order data.
 *
 * Security:
 * - All endpoints require authentication
 * - Regular users can only access their own orders (enforced by resolveTargetUserId)
 * - Admin users can access any user's orders via the userId path parameter
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order query endpoints")
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderQueryService orderQueryService;
    private final SecurityService securityService;

    /**
     * Gets all orders for a strategy.
     *
     * @param userId     User ID from path (admin can query any user, regular users only themselves)
     * @param strategyId Strategy ID
     * @param principal  Authenticated user principal
     * @return List of orders for the strategy
     */
    @GetMapping("/{userId}/strategy/{strategyId}")
    @Operation(summary = "Get orders by strategy", description = "Gets all orders for a specific strategy. Regular users can only access their own orders; admins can access any user's orders.")
    @ApiResponseDoc(code = "200", description = "Orders retrieved successfully", implementation = OrderDetailListResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<List<OrderDetailDto>>> getOrdersByStrategy(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Strategy ID") @PathVariable Long strategyId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        List<OrderDetailDto> orders = orderQueryService.getOrdersByStrategyId(targetUserId, strategyId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Gets order details by order ID.
     *
     * @param userId    User ID from path (admin can query any user, regular users only themselves)
     * @param orderId   Order ID
     * @param principal Authenticated user principal
     * @return Order details
     */
    @GetMapping("/{userId}/{orderId}")
    @Operation(summary = "Get order by ID", description = "Gets order details by order ID. Regular users can only access their own orders; admins can access any user's orders.")
    @ApiResponseDoc(code = "200", description = "Order retrieved successfully", implementation = OrderDetailResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Order not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<OrderDetailDto>> getOrderDetail(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Order ID (UUID)") @PathVariable UUID orderId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        return orderQueryService.getOrderDetail(targetUserId, orderId)
                .map(order -> ResponseEntity.ok(ApiResponse.success(order)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order not found")));
    }
}
