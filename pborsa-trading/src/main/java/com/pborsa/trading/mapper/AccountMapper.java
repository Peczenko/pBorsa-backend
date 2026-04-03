package com.pborsa.trading.mapper;

import com.pborsa.domain.dto.account.AccountInfoDto;
import com.pborsa.domain.dto.account.AccountStatus;
import com.pborsa.domain.dto.account.PositionDto;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.openapi.trader.model.Account;
import net.jacobpeterson.alpaca.openapi.trader.model.Position;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper for converting Alpaca account models to DTOs.
 */
@Component
@Slf4j
public class AccountMapper {

    /**
     * Converts an Alpaca Account object to AccountInfoDto.
     */
    public AccountInfoDto toAccountInfoDto(Account account) {
        if (account == null) {
            return null;
        }

        return AccountInfoDto.builder()
                .accountId(account.getId() != null ? account.getId().toString() : null)
                .accountNumber(account.getAccountNumber())
                .status(parseAccountStatus(account.getStatus()))
                .currency(account.getCurrency())
                .cash(account.getCash() != null ? new BigDecimal(account.getCash()) : null)
                .portfolioValue(account.getPortfolioValue() != null ? new BigDecimal(account.getPortfolioValue()) : null)
                .buyingPower(account.getBuyingPower() != null ? new BigDecimal(account.getBuyingPower()) : null)
                .equity(account.getEquity() != null ? new BigDecimal(account.getEquity()) : null)
                .lastEquity(account.getLastEquity() != null ? new BigDecimal(account.getLastEquity()) : null)
                .longMarketValue(account.getLongMarketValue() != null ? new BigDecimal(account.getLongMarketValue()) : null)
                .shortMarketValue(account.getShortMarketValue() != null ? new BigDecimal(account.getShortMarketValue()) : null)
                .initialMargin(account.getInitialMargin() != null ? new BigDecimal(account.getInitialMargin()) : null)
                .maintenanceMargin(account.getMaintenanceMargin() != null ? new BigDecimal(account.getMaintenanceMargin()) : null)
                .lastMaintenanceMargin(account.getLastMaintenanceMargin() != null ? new BigDecimal(account.getLastMaintenanceMargin()) : null)
                .daytradeCount(account.getDaytradeCount() != null ? new BigDecimal(account.getDaytradeCount()) : null)
                .patternDayTrader(account.getPatternDayTrader() != null ? account.getPatternDayTrader() : false)
                .tradingBlocked(account.getTradingBlocked() != null ? account.getTradingBlocked() : false)
                .transfersBlocked(account.getTransfersBlocked() != null ? account.getTransfersBlocked() : false)
                .accountBlocked(account.getAccountBlocked() != null ? account.getAccountBlocked() : false)
                .tradeSuspendedByUser(account.getTradeSuspendedByUser() != null ? account.getTradeSuspendedByUser() : false)
                .createdAt(account.getCreatedAt() != null ? account.getCreatedAt().toInstant() : null)
                .updatedAt(null) // UpdatedAt may not be available in SDK model
                .build();
    }

    /**
     * Converts an Alpaca Position object to PositionDto.
     */
    public PositionDto toPositionDto(Position position) {
        if (position == null) {
            return null;
        }

        return PositionDto.builder()
                .assetId(position.getAssetId() != null ? position.getAssetId().toString() : null)
                .symbol(position.getSymbol())
                .exchange(position.getExchange() != null ? position.getExchange().getValue() : null)
                .assetClass(position.getAssetClass() != null ? position.getAssetClass().name() : null)
                .averageEntryPrice(position.getAvgEntryPrice() != null ? new BigDecimal(position.getAvgEntryPrice()) : null)
                .quantity(position.getQty() != null ? new BigDecimal(position.getQty()) : null)
                .side(position.getSide() != null ? position.getSide() : null)
                .marketValue(position.getMarketValue() != null ? new BigDecimal(position.getMarketValue()) : null)
                .costBasis(position.getCostBasis() != null ? new BigDecimal(position.getCostBasis()) : null)
                .unrealizedPnL(position.getUnrealizedPl() != null ? new BigDecimal(position.getUnrealizedPl()) : null)
                .unrealizedPnLPercent(position.getUnrealizedPlpc() != null ? new BigDecimal(position.getUnrealizedPlpc()) : null)
                .unrealizedIntradayPnL(position.getUnrealizedIntradayPl() != null ? new BigDecimal(position.getUnrealizedIntradayPl()) : null)
                .unrealizedIntradayPnLPercent(position.getUnrealizedIntradayPlpc() != null ? new BigDecimal(position.getUnrealizedIntradayPlpc()) : null)
                .currentPrice(position.getCurrentPrice() != null ? new BigDecimal(position.getCurrentPrice()) : null)
                .lastDayPrice(position.getLastdayPrice() != null ? new BigDecimal(position.getLastdayPrice()) : null)
                .changeToday(position.getChangeToday() != null ? new BigDecimal(position.getChangeToday()) : null)
                .build();
    }

    // Enum conversion helper

    private AccountStatus parseAccountStatus(net.jacobpeterson.alpaca.openapi.trader.model.AccountStatus status) {
        if (status == null) {
            return null;
        }
        try {
            return AccountStatus.valueOf(status.name());
        } catch (Exception e) {
            log.warn("Unknown account status: {}", status);
            return null;
        }
    }
}

