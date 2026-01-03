package com.pborsa.api.controller;

import com.pborsa.api.config.openapi.ApiResponseDoc;
import com.pborsa.api.config.openapi.model.AdminStatusResponseDoc;
import com.pborsa.api.config.openapi.model.ApiErrorResponseDoc;
import com.pborsa.api.config.openapi.model.OrderDetailResponseDoc;
import com.pborsa.api.config.openapi.model.OrderHistoryListResponseDoc;
import com.pborsa.api.config.openapi.model.OrderHistoryResponseDoc;
import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.admin.AdminStatusResponse;
import com.pborsa.api.domain.dto.admin.SetAdminRequest;
import com.pborsa.api.domain.dto.strategy.OrderDetailDto;
import com.pborsa.api.domain.dto.strategy.OrderHistoryDto;
import com.pborsa.api.security.FirebaseUserPrincipal;
import com.pborsa.api.service.admin.FirebaseAdminService;
import com.pborsa.api.service.security.SecurityService;
import com.pborsa.api.service.trading.OrderHistoryQueryService;
import com.pborsa.api.service.trading.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * Controller for admin-only operations.
 * All endpoints require the authenticated user to have ROLE_ADMIN authority.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin-only endpoints for user management and data access")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final FirebaseAdminService firebaseAdminService;
    private final SecurityService securityService;
    private final OrderQueryService orderQueryService;
    private final OrderHistoryQueryService orderHistoryQueryService;

    /**
     * Sets the admin status for a user.
     *
     * @param targetUserId The user ID to update
     * @param request      Request containing the new admin status
     * @param principal    Authenticated admin user principal
     * @return Updated admin status
     */
    @PutMapping("/users/{targetUserId}/admin")
    @Operation(summary = "Set admin status", description = "Sets the admin status for a target user. Requires admin privileges.")
    @ApiResponseDoc(code = "200", description = "Admin status updated successfully", implementation = AdminStatusResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Forbidden - Admin privileges required", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<AdminStatusResponse>> setAdminStatus(
            @Parameter(description = "Target user ID") @PathVariable Long targetUserId,
            @Valid @RequestBody SetAdminRequest request,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        Long adminUserId = securityService.getCurrentUserId(principal);
        log.info("Admin {} setting admin={} for user {}", adminUserId, request.admin(), targetUserId);

        firebaseAdminService.setAdminStatus(targetUserId, request.admin());

        AdminStatusResponse response = new AdminStatusResponse(targetUserId, request.admin());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Gets the admin status for a user.
     *
     * @param targetUserId The user ID to check
     * @param principal    Authenticated admin user principal
     * @return Current admin status
     */
    @GetMapping("/users/{targetUserId}/admin")
    @Operation(summary = "Get admin status", description = "Gets the admin status for a target user. Requires admin privileges.")
    @ApiResponseDoc(code = "200", description = "Admin status retrieved successfully", implementation = AdminStatusResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Forbidden - Admin privileges required", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<AdminStatusResponse>> getAdminStatus(
            @Parameter(description = "Target user ID") @PathVariable Long targetUserId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        boolean isAdmin = firebaseAdminService.getAdminStatus(targetUserId);

        AdminStatusResponse response = new AdminStatusResponse(targetUserId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Grants admin privileges to a user.
     * Convenience endpoint, equivalent to PUT with admin=true.
     *
     * @param targetUserId The user ID to grant admin to
     * @param principal    Authenticated admin user principal
     * @return Updated admin status
     */
    @PostMapping("/users/{targetUserId}/grant")
    @Operation(summary = "Grant admin privileges", description = "Grants admin privileges to a target user. Requires admin privileges.")
    @ApiResponseDoc(code = "200", description = "Admin privileges granted successfully", implementation = AdminStatusResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Forbidden - Admin privileges required", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<AdminStatusResponse>> grantAdmin(
            @Parameter(description = "Target user ID") @PathVariable Long targetUserId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        Long adminUserId = securityService.getCurrentUserId(principal);
        log.info("Admin {} granting admin to user {}", adminUserId, targetUserId);

        firebaseAdminService.grantAdmin(targetUserId);

        AdminStatusResponse response = new AdminStatusResponse(targetUserId, true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Revokes admin privileges from a user.
     * Convenience endpoint, equivalent to PUT with admin=false.
     *
     * @param targetUserId The user ID to revoke admin from
     * @param principal    Authenticated admin user principal
     * @return Updated admin status
     */
    @PostMapping("/users/{targetUserId}/revoke")
    @Operation(summary = "Revoke admin privileges", description = "Revokes admin privileges from a target user. Requires admin privileges.")
    @ApiResponseDoc(code = "200", description = "Admin privileges revoked successfully", implementation = AdminStatusResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Forbidden - Admin privileges required", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<AdminStatusResponse>> revokeAdmin(
            @Parameter(description = "Target user ID") @PathVariable Long targetUserId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        Long adminUserId = securityService.getCurrentUserId(principal);
        log.info("Admin {} revoking admin from user {}", adminUserId, targetUserId);

        firebaseAdminService.revokeAdmin(targetUserId);

        AdminStatusResponse response = new AdminStatusResponse(targetUserId, false);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =============================================
    // Order Admin Endpoints
    // =============================================

    /**
     * Gets order details by order ID.
     * Admin only - no user ownership validation.
     *
     * @param orderId   Order ID
     * @param principal Authenticated admin user principal
     * @return Order detail DTO
     */
    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get order by ID (Admin)", description = "Gets order details by order ID without user ownership validation. Requires admin privileges.")
    @ApiResponseDoc(code = "200", description = "Order details retrieved successfully", implementation = OrderDetailResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Forbidden - Admin privileges required", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Order not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<OrderDetailDto>> getOrder(
            @Parameter(description = "Order ID (UUID)") @PathVariable UUID orderId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        log.debug("Admin {} fetching order {}", principal.uid(), orderId);

        return orderQueryService.getOrderDetailAdmin(orderId)
                .map(order -> ResponseEntity.ok(ApiResponse.success(order)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order not found")));
    }

    /**
     * Gets order history for an order.
     * Admin only - no user ownership validation.
     *
     * @param orderId   Order ID
     * @param principal Authenticated admin user principal
     * @return List of order history DTOs
     */
    @GetMapping("/orders/{orderId}/history")
    @Operation(summary = "Get order history (Admin)", description = "Gets order history for an order without user ownership validation. Requires admin privileges.")
    @ApiResponseDoc(code = "200", description = "Order history retrieved successfully", implementation = OrderHistoryListResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Forbidden - Admin privileges required", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<List<OrderHistoryDto>>> getOrderHistory(
            @Parameter(description = "Order ID (UUID)") @PathVariable UUID orderId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        log.debug("Admin {} fetching order history for order {}", principal.uid(), orderId);

        List<OrderHistoryDto> history = orderHistoryQueryService.getOrderHistoryAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Gets a specific order history entry by ID.
     * Admin only - no user ownership validation.
     *
     * @param historyId History entry ID
     * @param principal Authenticated admin user principal
     * @return Order history DTO
     */
    @GetMapping("/history/{historyId}")
    @Operation(summary = "Get order history entry (Admin)", description = "Gets a specific order history entry by ID without user ownership validation. Requires admin privileges.")
    @ApiResponseDoc(code = "200", description = "Order history entry retrieved successfully", implementation = OrderHistoryResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Forbidden - Admin privileges required", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Order history entry not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<OrderHistoryDto>> getOrderHistoryEntry(
            @Parameter(description = "History entry ID (UUID)") @PathVariable UUID historyId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {

        log.debug("Admin {} fetching history entry {}", principal.uid(), historyId);

        return orderHistoryQueryService.getOrderHistoryEntryAdmin(historyId)
                .map(history -> ResponseEntity.ok(ApiResponse.success(history)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order history entry not found")));
    }
}
