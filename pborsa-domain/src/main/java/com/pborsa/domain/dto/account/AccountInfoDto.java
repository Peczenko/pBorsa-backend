package com.pborsa.domain.dto.account;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing Alpaca account information.
 */
@Builder
public record AccountInfoDto(
        String accountId,
        String accountNumber,
        AccountStatus status,
        String currency,
        BigDecimal cash,
        BigDecimal portfolioValue,
        BigDecimal buyingPower,
        BigDecimal equity,
        BigDecimal lastEquity,
        BigDecimal longMarketValue,
        BigDecimal shortMarketValue,
        BigDecimal initialMargin,
        BigDecimal maintenanceMargin,
        BigDecimal lastMaintenanceMargin,
        BigDecimal daytradeCount,
        boolean patternDayTrader,
        boolean tradingBlocked,
        boolean transfersBlocked,
        boolean accountBlocked,
        boolean tradeSuspendedByUser,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Calculates the daily profit/loss.
     */
    public BigDecimal dailyPnL() {
        if (equity == null || lastEquity == null) {
            return BigDecimal.ZERO;
        }
        return equity.subtract(lastEquity);
    }

    /**
     * Calculates the daily profit/loss percentage.
     */
    public BigDecimal dailyPnLPercent() {
        if (lastEquity == null || lastEquity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return dailyPnL()
                .divide(lastEquity, 6, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Checks if the account can trade.
     */
    public boolean canTrade() {
        return !tradingBlocked
                && !accountBlocked
                && !tradeSuspendedByUser
                && status == AccountStatus.ACTIVE;
    }

    /**
     * Gets the margin utilization percentage.
     */
    public BigDecimal marginUtilization() {
        if (buyingPower == null || buyingPower.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal usedMargin = portfolioValue != null ? portfolioValue : BigDecimal.ZERO;
        return usedMargin
                .divide(buyingPower.add(usedMargin), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}

