package com.pborsa.api.controller;

import com.pborsa.api.config.openapi.ApiResponseDoc;
import com.pborsa.api.config.openapi.model.ApiErrorResponseDoc;
import com.pborsa.api.config.openapi.model.StrategyListResponseDoc;
import com.pborsa.api.config.openapi.model.StrategyResponseDoc;
import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.strategy.StrategyDto;
import com.pborsa.api.security.FirebaseUserPrincipal;
import com.pborsa.api.service.security.SecurityService;
import com.pborsa.api.service.strategy.StrategyService;
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

/**
 * Controller for strategy-related endpoints.
 * Handles HTTP requests for strategy data.
 *
 * Security:
 * - All endpoints require authentication
 * - Regular users can only access their own strategies (enforced by resolveTargetUserId)
 * - Admin users can access any user's strategies via the userId path parameter
 */
@RestController
@RequestMapping("/api/v1/strategies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Strategies", description = "Strategy management endpoints")
@PreAuthorize("isAuthenticated()")
public class StrategyController {

    private final StrategyService strategyService;
    private final SecurityService securityService;

    /**
     * Gets all strategies for a user.
     *
     * @param userId    User ID from path (admin can query any user, regular users only themselves)
     * @param principal Authenticated user principal
     * @return List of strategies
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user strategies", description = "Gets all strategies for a user. Regular users can only access their own strategies; admins can access any user's strategies.")
    @ApiResponseDoc(code = "200", description = "Strategies retrieved successfully", implementation = StrategyListResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<List<StrategyDto>>> getStrategies(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        List<StrategyDto> strategies = strategyService.getStrategiesByUserId(targetUserId);
        return ResponseEntity.ok(ApiResponse.success(strategies));
    }

    /**
     * Gets a specific strategy by ID.
     *
     * @param userId     User ID from path (admin can query any user, regular users only themselves)
     * @param strategyId Strategy ID
     * @param principal  Authenticated user principal
     * @return Strategy details
     */
    @GetMapping("/{userId}/{strategyId}")
    @Operation(summary = "Get strategy by ID", description = "Gets a specific strategy by ID. Regular users can only access their own strategies; admins can access any user's strategies.")
    @ApiResponseDoc(code = "200", description = "Strategy retrieved successfully", implementation = StrategyResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "403", description = "Access denied", implementation = ApiErrorResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Strategy not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<StrategyDto>> getStrategy(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Strategy ID") @PathVariable Long strategyId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        return strategyService.getStrategyById(targetUserId, strategyId)
                .map(strategy -> ResponseEntity.ok(ApiResponse.success(strategy)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Strategy not found")));
    }
}
