package com.pborsa.api.strategy.controller;

import com.pborsa.api.shared.config.openapi.ApiResponseDoc;
import com.pborsa.api.shared.config.openapi.model.ApiErrorResponseDoc;
import com.pborsa.api.shared.config.openapi.model.UserStrategyListResponseDoc;
import com.pborsa.api.shared.config.openapi.model.UserStrategyResponseDoc;
import com.pborsa.api.shared.controller.ApiResponse;
import com.pborsa.domain.dto.strategy.CreateUserStrategyRequest;
import com.pborsa.domain.dto.strategy.StrategyPnLDto;
import com.pborsa.domain.dto.strategy.UpdateUserStrategyRequest;
import com.pborsa.domain.dto.strategy.UserStrategyDto;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import com.pborsa.api.shared.security.SecurityService;
import com.pborsa.api.strategy.service.StrategyService;
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

/**
 * Controller for user strategy subscriptions (CRUD).
 * Users can subscribe to base strategies with specific stocks.
 *
 * Security:
 * - All endpoints require authentication
 * - Regular users can only access their own strategies
 * - Admin users can access any user's strategies via the userId path parameter
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/strategies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Strategies", description = "User strategy subscription management")
@PreAuthorize("isAuthenticated()")
public class UserStrategyController {

    private final StrategyService strategyService;
    private final SecurityService securityService;

    /**
     * Gets all strategies for a user.
     *
     * @param userId    User ID from path
     * @param principal Authenticated user principal
     * @return List of user strategies
     */
    @GetMapping
    @Operation(summary = "List user strategies", description = "Gets all strategy subscriptions for a user.")
    @ApiResponseDoc(code = "200", description = "User strategies retrieved successfully", implementation = UserStrategyListResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<List<UserStrategyDto>>> getUserStrategies(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        List<UserStrategyDto> strategies = strategyService.getStrategiesByUserId(targetUserId);
        return ResponseEntity.ok(ApiResponse.success(strategies));
    }

    /**
     * Gets a specific user strategy by ID.
     *
     * @param userId     User ID from path
     * @param strategyId Strategy ID
     * @param principal  Authenticated user principal
     * @return User strategy details
     */
    @GetMapping("/{strategyId}")
    @Operation(summary = "Get user strategy", description = "Gets a specific strategy subscription by ID.")
    @ApiResponseDoc(code = "200", description = "User strategy retrieved successfully", implementation = UserStrategyResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Strategy not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<UserStrategyDto>> getUserStrategy(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Strategy ID") @PathVariable Long strategyId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        return strategyService.getStrategyById(targetUserId, strategyId)
                .map(strategy -> ResponseEntity.ok(ApiResponse.success(strategy)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Strategy not found")));
    }

    /**
     * Creates a new user strategy subscription.
     *
     * @param userId    User ID from path
     * @param request   Create request with base strategy code, name, and symbol
     * @param principal Authenticated user principal
     * @return Created user strategy
     */
    @PostMapping
    @Operation(summary = "Create user strategy", description = "Subscribes to a base strategy with a specific stock.")
    @ApiResponseDoc(code = "201", description = "User strategy created successfully", implementation = UserStrategyResponseDoc.class)
    @ApiResponseDoc(code = "400", description = "Invalid request", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<UserStrategyDto>> createUserStrategy(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody CreateUserStrategyRequest request,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        UserStrategyDto created = strategyService.createUserStrategy(targetUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    /**
     * Updates an existing user strategy.
     *
     * @param userId     User ID from path
     * @param strategyId Strategy ID
     * @param request    Update request with name and/or status
     * @param principal  Authenticated user principal
     * @return Updated user strategy
     */
    @PatchMapping("/{strategyId}")
    @Operation(summary = "Update user strategy", description = "Updates a strategy subscription (name or status).")
    @ApiResponseDoc(code = "200", description = "User strategy updated successfully", implementation = UserStrategyResponseDoc.class)
    @ApiResponseDoc(code = "400", description = "Invalid request", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Strategy not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<UserStrategyDto>> updateUserStrategy(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Strategy ID") @PathVariable Long strategyId,
            @Valid @RequestBody UpdateUserStrategyRequest request,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        return strategyService.updateUserStrategy(targetUserId, strategyId, request)
                .map(strategy -> ResponseEntity.ok(ApiResponse.success(strategy)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Strategy not found")));
    }

    /**
     * Deletes a user strategy subscription.
     *
     * @param userId     User ID from path
     * @param strategyId Strategy ID
     * @param principal  Authenticated user principal
     * @return Success or not found
     */
    @DeleteMapping("/{strategyId}")
    @Operation(summary = "Delete user strategy", description = "Removes a strategy subscription.")
    @ApiResponseDoc(code = "200", description = "User strategy deleted successfully", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Strategy not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<Void>> deleteUserStrategy(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Strategy ID") @PathVariable Long strategyId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal)
    {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Strategy removal is temporally disabled by API"));
//        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
//        boolean deleted = strategyService.deleteUserStrategy(targetUserId, strategyId);
//        if (deleted) {
//            return ResponseEntity.ok(ApiResponse.success(null));
//        }
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(ApiResponse.error("Strategy not found"));
    }

    /**
     * Activates a user strategy, starting data transfer to the trading engine.
     * The strategy must be in CREATED status. After activation, status transitions to PREPARING,
     * and once data transfer completes, it becomes ACTIVE.
     *
     * @param userId     User ID from path
     * @param strategyId Strategy ID
     * @param principal  Authenticated user principal
     * @return Activated user strategy (status: PREPARING)
     */
    @PostMapping("/{strategyId}/activate")
    @Operation(summary = "Activate user strategy", 
            description = "Activates a strategy, starting data transfer to the trading engine. " +
                    "Strategy must be in CREATED status. Status will be PREPARING until data transfer completes.")
    @ApiResponseDoc(code = "200", description = "Strategy activated successfully", implementation = UserStrategyResponseDoc.class)
    @ApiResponseDoc(code = "400", description = "Invalid state (strategy not in CREATED status)", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Strategy not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<UserStrategyDto>> activateStrategy(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Strategy ID") @PathVariable Long strategyId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        return strategyService.activateStrategy(targetUserId, strategyId)
                .map(strategy -> ResponseEntity.ok(ApiResponse.success(strategy, "Strategy activation started")))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Strategy not found")));
    }

    /**
     * Gets the profit/loss data for a user strategy.
     * Includes realized P/L from closed positions and unrealized P/L from open positions.
     *
     * @param userId     User ID from path
     * @param strategyId Strategy ID
     * @param principal  Authenticated user principal
     * @return Strategy P/L data
     */
    @GetMapping("/{strategyId}/pnl")
    @Operation(summary = "Get strategy P/L", 
            description = "Gets profit/loss data for a strategy including realized P/L (closed positions), " +
                    "unrealized P/L (current market value vs cost basis), and position details.")
    @ApiResponseDoc(code = "200", description = "P/L data retrieved successfully", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Strategy not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<StrategyPnLDto>> getStrategyPnL(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Strategy ID") @PathVariable Long strategyId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        return strategyService.getStrategyPnL(targetUserId, strategyId)
                .map(pnl -> ResponseEntity.ok(ApiResponse.success(pnl)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Strategy not found")));
    }
}

