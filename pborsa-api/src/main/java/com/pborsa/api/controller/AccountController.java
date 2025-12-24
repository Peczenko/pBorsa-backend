package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.account.AccountInfoDto;
import com.pborsa.api.domain.dto.account.PositionDto;
import com.pborsa.api.service.trading.AccountService;
import com.pborsa.api.service.trading.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for account and position information.
 */
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final PositionService positionService;

    /**
     * Gets account information.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AccountInfoDto>> getAccountInfo(@PathVariable String userId) {
        log.debug("Getting account info for user: {}", userId);
        AccountInfoDto account = accountService.getAccountInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    /**
     * Gets buying power.
     */
    @GetMapping("/{userId}/buying-power")
    public ResponseEntity<ApiResponse<BigDecimal>> getBuyingPower(@PathVariable String userId) {
        BigDecimal buyingPower = accountService.getBuyingPower(userId);
        return ResponseEntity.ok(ApiResponse.success(buyingPower));
    }

    /**
     * Gets cash balance.
     */
    @GetMapping("/{userId}/cash")
    public ResponseEntity<ApiResponse<BigDecimal>> getCashBalance(@PathVariable String userId) {
        BigDecimal cash = accountService.getCashBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(cash));
    }

    /**
     * Gets equity.
     */
    @GetMapping("/{userId}/equity")
    public ResponseEntity<ApiResponse<BigDecimal>> getEquity(@PathVariable String userId) {
        BigDecimal equity = accountService.getEquity(userId);
        return ResponseEntity.ok(ApiResponse.success(equity));
    }

    /**
     * Checks if trading is allowed.
     */
    @GetMapping("/{userId}/can-trade")
    public ResponseEntity<ApiResponse<Boolean>> canTrade(@PathVariable String userId) {
        boolean canTrade = accountService.canTrade(userId);
        return ResponseEntity.ok(ApiResponse.success(canTrade));
    }

    /**
     * Refreshes account info cache.
     */
    @PostMapping("/{userId}/refresh")
    public ResponseEntity<ApiResponse<AccountInfoDto>> refreshAccountInfo(@PathVariable String userId) {
        AccountInfoDto account = accountService.refreshAccountInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(account, "Account info refreshed"));
    }

    // ==================== Positions ====================

    /**
     * Gets all positions.
     */
    @GetMapping("/{userId}/positions")
    public ResponseEntity<ApiResponse<List<PositionDto>>> getPositions(@PathVariable String userId) {
        log.debug("Getting positions for user: {}", userId);
        List<PositionDto> positions = positionService.getAllPositions(userId);
        return ResponseEntity.ok(ApiResponse.success(positions));
    }

    /**
     * Gets a specific position.
     */
    @GetMapping("/{userId}/positions/{symbol}")
    public ResponseEntity<ApiResponse<PositionDto>> getPosition(
            @PathVariable String userId,
            @PathVariable String symbol
    ) {
        PositionDto position = positionService.getPosition(userId, symbol);
        return ResponseEntity.ok(ApiResponse.success(position));
    }

    /**
     * Gets total portfolio value.
     */
    @GetMapping("/{userId}/portfolio-value")
    public ResponseEntity<ApiResponse<BigDecimal>> getPortfolioValue(@PathVariable String userId) {
        BigDecimal value = positionService.calculateTotalPortfolioValue(userId);
        return ResponseEntity.ok(ApiResponse.success(value));
    }

    /**
     * Gets total unrealized P&L.
     */
    @GetMapping("/{userId}/unrealized-pnl")
    public ResponseEntity<ApiResponse<BigDecimal>> getUnrealizedPnL(@PathVariable String userId) {
        BigDecimal pnl = positionService.calculateTotalUnrealizedPnL(userId);
        return ResponseEntity.ok(ApiResponse.success(pnl));
    }
}

