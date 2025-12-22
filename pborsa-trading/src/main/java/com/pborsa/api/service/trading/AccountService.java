package com.pborsa.api.service.trading;

import com.pborsa.api.config.cache.CacheNames;
import com.pborsa.api.domain.dto.account.AccountInfoDto;
import com.pborsa.api.domain.dto.account.AccountStatus;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.exception.AlpacaException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.service.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing Alpaca account information.
 * Provides account balance, portfolio value, and account status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AlpacaClientFactory clientFactory;
    private final UserCredentialsService credentialsService;
    private final AccountMapper accountMapper;

    /**
     * Gets account information for a user.
     * Results are cached briefly for performance.
     *
     * @param userId User ID
     * @return Account information DTO
     */
    @Cacheable(value = CacheNames.ACCOUNT_INFO, key = "#userId")
    public AccountInfoDto getAccountInfo(String userId) {
        log.debug("Fetching account info for user: {}", userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Get account using OpenAPI AccountsApi
            net.jacobpeterson.alpaca.openapi.trader.model.Account account = 
                    client.trader().accounts().getAccount();
            
            return accountMapper.toAccountInfoDto(account);
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch account info for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to fetch account information: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Async version of getAccountInfo.
     *
     * @param userId User ID
     * @return CompletableFuture with account information
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<AccountInfoDto> getAccountInfoAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> getAccountInfo(userId));
    }

    /**
     * Gets the buying power for a user.
     *
     * @param userId User ID
     * @return Buying power as BigDecimal
     */
    public BigDecimal getBuyingPower(String userId) {
        AccountInfoDto accountInfo = getAccountInfo(userId);
        return accountInfo.buyingPower();
    }

    /**
     * Gets the cash balance for a user.
     *
     * @param userId User ID
     * @return Cash balance as BigDecimal
     */
    public BigDecimal getCashBalance(String userId) {
        AccountInfoDto accountInfo = getAccountInfo(userId);
        return accountInfo.cash();
    }

    /**
     * Gets the equity for a user.
     *
     * @param userId User ID
     * @return Equity as BigDecimal
     */
    public BigDecimal getEquity(String userId) {
        AccountInfoDto accountInfo = getAccountInfo(userId);
        return accountInfo.equity();
    }

    /**
     * Refreshes account info by evicting cache and fetching fresh data.
     *
     * @param userId User ID
     * @return Fresh account information
     */
    @CacheEvict(value = CacheNames.ACCOUNT_INFO, key = "#userId")
    public AccountInfoDto refreshAccountInfo(String userId) {
        log.debug("Refreshing account info for user: {}", userId);
        return getAccountInfo(userId);
    }

    /**
     * Checks if the account has sufficient buying power.
     *
     * @param userId         User ID
     * @param requiredAmount Amount of buying power needed
     * @return true if account has sufficient buying power
     */
    public boolean hasSufficientBuyingPower(String userId, double requiredAmount) {
        AccountInfoDto accountInfo = getAccountInfo(userId);
        return accountInfo.buyingPower().doubleValue() >= requiredAmount;
    }

    /**
     * Gets the current portfolio value.
     *
     * @param userId User ID
     * @return Current portfolio value
     */
    public double getPortfolioValue(String userId) {
        AccountInfoDto accountInfo = getAccountInfo(userId);
        return accountInfo.portfolioValue().doubleValue();
    }

    /**
     * Checks if the account can trade.
     *
     * @param userId User ID
     * @return true if account is active and can trade
     */
    public boolean canTrade(String userId) {
        try {
            AccountInfoDto accountInfo = getAccountInfo(userId);
            return AccountStatus.ACTIVE == accountInfo.status() && !accountInfo.tradingBlocked();
        } catch (Exception e) {
            log.warn("Could not determine trade eligibility for user: {}", userId, e);
            return false;
        }
    }
}
