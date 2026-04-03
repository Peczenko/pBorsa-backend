package com.pborsa.api.account;

import com.pborsa.api.shared.controller.ApiResponse;
import com.pborsa.domain.dto.account.AccountInfoDto;
import com.pborsa.domain.dto.account.PositionDto;
import com.pborsa.trading.account.AccountService;
import com.pborsa.trading.account.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Account", description = "Account balances, cash, buying power, and positions")
public class AccountController {

    private final AccountService accountService;
    private final PositionService positionService;

    /**
     * Gets account information.
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get account info", description = "Returns Alpaca account state for the given user")
    public ResponseEntity<ApiResponse<AccountInfoDto>> getAccountInfo(@PathVariable Long userId) {
        log.debug("Getting account info for user: {}", userId);
        AccountInfoDto account = accountService.getAccountInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    /**
     * Gets buying power.
     */
    @GetMapping("/{userId}/buying-power")
    @Operation(summary = "Get buying power", description = "Returns current buying power for the user")
    public ResponseEntity<ApiResponse<BigDecimal>> getBuyingPower(@PathVariable Long userId) {
        BigDecimal buyingPower = accountService.getBuyingPower(userId);
        return ResponseEntity.ok(ApiResponse.success(buyingPower));
    }

    /**
     * Gets cash balance.
     */
    @GetMapping("/{userId}/cash")
    @Operation(summary = "Get cash balance", description = "Returns cash on the account")
    public ResponseEntity<ApiResponse<BigDecimal>> getCashBalance(@PathVariable Long userId) {
        BigDecimal cash = accountService.getCashBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(cash));
    }

    /**
     * Gets equity.
     */
    @GetMapping("/{userId}/equity")
    @Operation(summary = "Get equity", description = "Returns account equity value")
    public ResponseEntity<ApiResponse<BigDecimal>> getEquity(@PathVariable Long userId) {
        BigDecimal equity = accountService.getEquity(userId);
        return ResponseEntity.ok(ApiResponse.success(equity));
    }

    /**
     * Checks if trading is allowed.
     */
    @GetMapping("/{userId}/can-trade")
    @Operation(summary = "Check trade permission", description = "Returns true if account can currently trade")
    public ResponseEntity<ApiResponse<Boolean>> canTrade(@PathVariable Long userId) {
        boolean canTrade = accountService.canTrade(userId);
        return ResponseEntity.ok(ApiResponse.success(canTrade));
    }

    /**
     * Refreshes account info cache.
     */
    @PostMapping("/{userId}/refresh")
    @Operation(summary = "Refresh account info", description = "Forces a fetch from Alpaca and updates cache")
    public ResponseEntity<ApiResponse<AccountInfoDto>> refreshAccountInfo(@PathVariable Long userId) {
        AccountInfoDto account = accountService.refreshAccountInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(account, "Account info refreshed"));
    }

    // ==================== Positions ====================

    /**
     * Gets all positions.
     */
    @GetMapping("/{userId}/positions")
    @Operation(summary = "Get positions", description = "Returns all open positions for the user")
    public ResponseEntity<ApiResponse<List<PositionDto>>> getPositions(@PathVariable Long userId) {
        log.debug("Getting positions for user: {}", userId);
        List<PositionDto> positions = positionService.getAllPositions(userId);
        return ResponseEntity.ok(ApiResponse.success(positions));
    }

    /**
     * Gets a specific position.
     */
    @GetMapping("/{userId}/positions/{symbol}")
    @Operation(summary = "Get single position", description = "Returns position for the symbol if it exists")
    public ResponseEntity<ApiResponse<PositionDto>> getPosition(
            @PathVariable Long userId,
            @PathVariable String symbol
    ) {
        PositionDto position = positionService.getPosition(userId, symbol);
        return ResponseEntity.ok(ApiResponse.success(position));
    }

    /**
     * Gets total portfolio value.
     */
    @GetMapping("/{userId}/portfolio-value")
    @Operation(summary = "Get portfolio value", description = "Returns total portfolio market value")
    public ResponseEntity<ApiResponse<BigDecimal>> getPortfolioValue(@PathVariable Long userId) {
        BigDecimal value = positionService.calculateTotalPortfolioValue(userId);
        return ResponseEntity.ok(ApiResponse.success(value));
    }

    /**
     * Gets total unrealized P&L.
     */
    @GetMapping("/{userId}/unrealized-pnl")
    @Operation(summary = "Get unrealized PnL", description = "Returns total unrealized profit/loss")
    public ResponseEntity<ApiResponse<BigDecimal>> getUnrealizedPnL(@PathVariable Long userId) {
        BigDecimal pnl = positionService.calculateTotalUnrealizedPnL(userId);
        return ResponseEntity.ok(ApiResponse.success(pnl));
    }
}

