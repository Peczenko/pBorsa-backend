package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.backtest.BacktestDto;
import com.pborsa.api.domain.dto.backtest.CreateBacktestRequest;
import com.pborsa.api.security.FirebaseUserPrincipal;
import com.pborsa.api.service.backtest.BacktestService;
import com.pborsa.api.service.security.SecurityService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for strategy backtesting.
 * Allows users to run strategies against historical data.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/backtests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Backtests", description = "Strategy backtesting operations")
@PreAuthorize("isAuthenticated()")
public class BacktestController {

    private final BacktestService backtestService;
    private final SecurityService securityService;

    /**
     * Lists all backtests for a user.
     *
     * @param userId    User ID from path
     * @param principal Authenticated user principal
     * @return List of backtests
     */
    @GetMapping
    @Operation(summary = "List backtests", description = "Gets all backtests for a user.")
    public ResponseEntity<ApiResponse<List<BacktestDto>>> getUserBacktests(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        List<BacktestDto> backtests = backtestService.getUserBacktests(targetUserId);
        return ResponseEntity.ok(ApiResponse.success(backtests));
    }

    /**
     * Gets a specific backtest with its orders.
     *
     * @param userId     User ID from path
     * @param backtestId Backtest ID
     * @param principal  Authenticated user principal
     * @return Backtest details with orders
     */
    @GetMapping("/{backtestId}")
    @Operation(summary = "Get backtest", description = "Gets a specific backtest by ID with its orders.")
    public ResponseEntity<ApiResponse<BacktestDto>> getBacktest(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Backtest ID") @PathVariable Long backtestId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        return backtestService.getBacktest(targetUserId, backtestId)
                .map(backtest -> ResponseEntity.ok(ApiResponse.success(backtest)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Backtest not found")));
    }

    /**
     * Creates a new backtest.
     *
     * @param userId    User ID from path
     * @param request   Create backtest request
     * @param principal Authenticated user principal
     * @return Created backtest
     */
    @PostMapping
    @Operation(summary = "Create backtest", description = "Creates a new backtest configuration.")
    public ResponseEntity<ApiResponse<BacktestDto>> createBacktest(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody CreateBacktestRequest request,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        BacktestDto created = backtestService.createBacktest(targetUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    /**
     * Starts a backtest execution.
     * The backtest must be in CREATED status. After starting, status transitions to PREPARING,
     * then RUNNING, and finally COMPLETED or FAILED.
     *
     * @param userId     User ID from path
     * @param backtestId Backtest ID
     * @param principal  Authenticated user principal
     * @return Started backtest (status: PREPARING)
     */
    @PostMapping("/{backtestId}/start")
    @Operation(summary = "Start backtest",
            description = "Starts backtest execution. Backtest must be in CREATED status.")
    public ResponseEntity<ApiResponse<BacktestDto>> startBacktest(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Backtest ID") @PathVariable Long backtestId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        return backtestService.startBacktest(targetUserId, backtestId)
                .map(backtest -> ResponseEntity.ok(ApiResponse.success(backtest, "Backtest started")))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Backtest not found")));
    }

    /**
     * Deletes a backtest.
     * Only backtests in CREATED, COMPLETED, or FAILED status can be deleted.
     *
     * @param userId     User ID from path
     * @param backtestId Backtest ID
     * @param principal  Authenticated user principal
     * @return Success or error
     */
    @DeleteMapping("/{backtestId}")
    @Operation(summary = "Delete backtest", description = "Deletes a backtest and its orders.")
    public ResponseEntity<ApiResponse<Void>> deleteBacktest(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Backtest ID") @PathVariable Long backtestId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        Long targetUserId = securityService.resolveTargetUserId(userId, principal);
        boolean deleted = backtestService.deleteBacktest(targetUserId, backtestId);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Backtest not found"));
    }
}
