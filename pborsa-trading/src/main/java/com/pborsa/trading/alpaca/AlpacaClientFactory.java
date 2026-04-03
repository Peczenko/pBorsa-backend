package com.pborsa.trading.alpaca;

import com.pborsa.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.domain.exception.AlpacaException;
import com.pborsa.trading.config.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Factory for creating and caching Alpaca API client instances per user or system.
 * Each user gets their own client configured with their API credentials.
 * System credentials (userId = SystemConstants.SYSTEM_USER_ID) are cached separately.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlpacaClientFactory {

    private final AlpacaConfiguration alpacaConfiguration;

    /**
     * Gets or creates an Alpaca API client for the given credentials.
     * Clients are cached to avoid recreating them on every request.
     * System credentials (userId = SystemConstants.SYSTEM_USER_ID) are cached separately.
     *
     * @param credentials The user's or system's Alpaca credentials
     * @return Configured AlpacaAPI instance
     */
    @Cacheable(
            value = CacheNames.ALPACA_CLIENTS,
            key = "#credentials.userId()",
            cacheManager = "apiCredentialsCacheManager",
            sync = true
    )
    public AlpacaAPI getOrCreateClient(AlpacaCredentialsDto credentials) {
        log.debug("Creating new Alpaca API client for user: {}", credentials.userId());
        
        try {
            AlpacaAPI alpacaAPI = createAlpacaAPI(
                    credentials.apiKey(),
                    credentials.secretKey(),
                    credentials.paperTrading()
            );
            
            log.info("Successfully created Alpaca API client for user: {}", credentials.userId());
            return alpacaAPI;
        } catch (Exception e) {
            log.error("Failed to create Alpaca API client for user: {}", credentials.userId(), e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.AUTHENTICATION_FAILED,
                    "Failed to create Alpaca client: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Creates AlpacaAPI instance using proper SDK constructors with enums.
     */
    private AlpacaAPI createAlpacaAPI(String apiKey, String secretKey, boolean paperTrading) {
        try {
            TraderAPIEndpointType endpointType = paperTrading 
                    ? TraderAPIEndpointType.PAPER 
                    : TraderAPIEndpointType.LIVE;
            
            // Use IEX as default data source (free tier)
            MarketDataWebsocketSourceType sourceType = MarketDataWebsocketSourceType.IEX;
            
            // Create AlpacaAPI with proper constructor: (keyID, secretKey, endpointType, sourceType)
            return new AlpacaAPI(apiKey, secretKey, endpointType, sourceType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create AlpacaAPI instance", e);
        }
    }

    /**
     * Evicts the cached client for a user or system.
     * Should be called when credentials are updated or invalidated.
     *
     * @param userId The user ID (or SystemConstants.SYSTEM_USER_ID for system) whose client should be evicted
     */
    @CacheEvict(value = CacheNames.ALPACA_CLIENTS, key = "#userId", cacheManager = "apiCredentialsCacheManager")
    public void evictClient(Long userId) {
        log.info("Evicted Alpaca API client from cache for user: {}", userId);
    }

    /**
     * Evicts all cached clients.
     * Should be used sparingly, mainly for system maintenance.
     */
    @CacheEvict(value = CacheNames.ALPACA_CLIENTS, allEntries = true, cacheManager = "apiCredentialsCacheManager")
    public void evictAllClients() {
        log.info("Evicted all Alpaca API clients from cache");
    }

    /**
     * Creates a new client without caching.
     * Useful for one-time operations or validation.
     *
     * @param credentials The credentials to use
     * @return New AlpacaAPI instance
     */
    public AlpacaAPI createTransientClient(AlpacaCredentialsDto credentials) {
        log.debug("Creating transient Alpaca API client for user: {}", credentials.userId());
        
        try {
            return createAlpacaAPI(
                    credentials.apiKey(),
                    credentials.secretKey(),
                    credentials.paperTrading()
            );
        } catch (Exception e) {
            log.error("Failed to create transient Alpaca API client", e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.AUTHENTICATION_FAILED,
                    "Failed to create Alpaca client: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Validates credentials by attempting to create a client and fetch account info.
     *
     * @param credentials The credentials to validate
     * @return true if credentials are valid
     */
    public boolean validateCredentials(AlpacaCredentialsDto credentials) {
        try {
            AlpacaAPI client = createTransientClient(credentials);
            // Try to fetch account to validate credentials
            client.trader().accounts().getAccount();
            return true;
        } catch (Exception e) {
            log.warn("Credential validation failed for user: {}", credentials.userId(), e);
            return false;
        }
    }
}
