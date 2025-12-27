package com.pborsa.api.service.trading;

import com.pborsa.api.config.cache.CacheNames;
import com.pborsa.api.domain.dto.account.AccountInfoDto;
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
import org.springframework.stereotype.Service;

/**
 * Cached access to account info to avoid self-invocation cache bypass.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountInfoCacheService {

    private final AlpacaClientFactory clientFactory;
    private final UserCredentialsService credentialsService;
    private final AccountMapper accountMapper;

    @Cacheable(
            value = CacheNames.ACCOUNT_INFO,
            key = "#userId",
            cacheManager = "accountInfoCacheManager",
            sync = true
    )
    public AccountInfoDto getAccountInfo(Long userId) {
        return fetchAccountInfo(userId);
    }

    @CacheEvict(value = CacheNames.ACCOUNT_INFO, key = "#userId")
    public AccountInfoDto refreshAccountInfo(Long userId) {
        log.debug("Refreshing account info for user: {}", userId);
        return fetchAccountInfo(userId);
    }

    private AccountInfoDto fetchAccountInfo(Long userId) {
        log.debug("Fetching account info (uncached) for user: {}", userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);

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
}
