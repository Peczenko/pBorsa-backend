package com.pborsa.trading.account;

import com.pborsa.domain.dto.account.AccountInfoDto;
import com.pborsa.domain.dto.account.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for managing Alpaca account information.
 * Delegates cached access to AccountInfoCacheService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountInfoCacheService accountInfoCacheService;

    public AccountInfoDto getAccountInfo(Long userId) {
        return accountInfoCacheService.getAccountInfo(userId);
    }

    public BigDecimal getBuyingPower(Long userId) {
        AccountInfoDto accountInfo = accountInfoCacheService.getAccountInfo(userId);
        return accountInfo.buyingPower();
    }

    public BigDecimal getCashBalance(Long userId) {
        AccountInfoDto accountInfo = accountInfoCacheService.getAccountInfo(userId);
        return accountInfo.cash();
    }

    public BigDecimal getEquity(Long userId) {
        AccountInfoDto accountInfo = accountInfoCacheService.getAccountInfo(userId);
        return accountInfo.equity();
    }

    public AccountInfoDto refreshAccountInfo(Long userId) {
        return accountInfoCacheService.refreshAccountInfo(userId);
    }

    public boolean hasSufficientBuyingPower(Long userId, BigDecimal requiredAmount) {
        AccountInfoDto accountInfo = accountInfoCacheService.getAccountInfo(userId);
        return accountInfo.buyingPower().compareTo(requiredAmount) >= 0;
    }

    public double getPortfolioValue(Long userId) {
        AccountInfoDto accountInfo = accountInfoCacheService.getAccountInfo(userId);
        return accountInfo.portfolioValue().doubleValue();
    }

    public boolean canTrade(Long userId) {
        try {
            AccountInfoDto accountInfo = accountInfoCacheService.getAccountInfo(userId);
            return AccountStatus.ACTIVE == accountInfo.status() && !accountInfo.tradingBlocked();
        } catch (Exception e) {
            log.warn("Could not determine trade eligibility for user: {}", userId);
            return false;
        }
    }
}
