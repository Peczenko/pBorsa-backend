package com.pborsa.api.controller;

import com.pborsa.api.config.openapi.ApiResponseDoc;
import com.pborsa.api.config.openapi.model.ApiErrorResponseDoc;
import com.pborsa.api.config.openapi.model.OrderHistoryListResponseDoc;
import com.pborsa.api.config.openapi.model.OrderHistoryResponseDoc;
import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.strategy.OrderHistoryDto;
import com.pborsa.api.security.FirebaseUserPrincipal;
import com.pborsa.api.service.security.SecurityService;
import com.pborsa.api.service.trading.OrderHistoryQueryService;
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
 * Controller for order history endpoints.
 * Handles HTTP requests for order history data.
 *
 * Security:
 * - All endpoints require authentication
 * - Regular users can only access history for their own orders (enforced by resolveTargetUserId)
 * - Admin users can access any user's order history via the userId path parameter
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order History", description = "Order history query endpoints")
@PreAuthorize("isAuthenticated()")
public class OrderHistoryController {

    private final OrderHistoryQueryService orderHistoryQueryService;
    private final SecurityService securityService;

    /**
     * Gets order history for an order.
     *
     * @param userId    User ID from path (admin can query any user, regular users only themselves)
     * @param orderId   Order ID
     * @param principal Authenticated user principal
     * @return List of order history entries
     */
    @GetMapping("/{userId}/{orderId}/history")
    @Operation(summary = "Get order history", description = "Gets the history of status changes for an order. Regular users can only access their own order history; admins can access any user's order history.")
    @ApiResponseDoc(code = "200", description = "Order history retrieved successfully", implementation = OrderHistoryListResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<List<OrderHistoryDto>>> getOrderHistory(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Order ID (UUID)") @PathVariable UUID orderId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        List<OrderHistoryDto> history = orderHistoryQueryService.getOrderHistory(targetUserId, orderId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Gets a specific order history entry.
     *
     * @param userId    User ID from path (admin can query any user, regular users only themselves)
     * @param historyId History entry ID
     * @param principal Authenticated user principal
     * @return Order history entry
     */
    @GetMapping("/{userId}/history/{historyId}")
    @Operation(summary = "Get order history entry", description = "Gets a specific order history entry by ID. Regular users can only access their own order history; admins can access any user's order history.")
    @ApiResponseDoc(code = "200", description = "Order history entry retrieved successfully", implementation = OrderHistoryResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Order history entry not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<OrderHistoryDto>> getOrderHistoryEntry(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "History entry ID (UUID)") @PathVariable UUID historyId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        return orderHistoryQueryService.getOrderHistoryEntry(targetUserId, historyId)
                .map(history -> ResponseEntity.ok(ApiResponse.success(history)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order history entry not found")));
    }
}
