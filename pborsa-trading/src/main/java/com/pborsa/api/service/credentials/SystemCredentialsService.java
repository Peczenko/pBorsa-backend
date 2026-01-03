package com.pborsa.api.service.credentials;

import com.pborsa.api.config.cache.CacheNames;
import com.pborsa.api.domain.constants.SystemConstants;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.credentials.CredentialsRegistrationRequest;
import com.pborsa.api.domain.event.CredentialsChangedEvent;
import com.pborsa.api.domain.entity.UserApiCredentials;
import com.pborsa.api.exception.CredentialsNotFoundException;
import com.pborsa.api.repository.UserApiCredentialsRepository;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.encryption.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Service for managing system-level Alpaca API credentials.
 * System credentials are stored in the same user_api_credentials table
 * with userId = SYSTEM_USER_ID (0L).
 * Used for operations that aren't tied to a specific user, such as
 * polling market data for the trading engine.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemCredentialsService {

    private final UserApiCredentialsRepository credentialsRepository;
    private final EncryptionService encryptionService;
    private final AlpacaClientFactory alpacaClientFactory;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Retrieves decrypted system credentials.
     * Results are cached for fast subsequent access.
     *
     * @return Decrypted credentials DTO
     * @throws CredentialsNotFoundException if system credentials don't exist
     */
    @Cacheable(
            value = CacheNames.API_CREDENTIALS,
            key = "T(com.pborsa.api.domain.constants.SystemConstants).SYSTEM_USER_ID",
            cacheManager = "apiCredentialsCacheManager",
            sync = true
    )
    @Transactional(readOnly = true)
    public AlpacaCredentialsDto getCredentials() {
        return getCredentialsInternal();
    }

    /**
     * Async version of getCredentials for non-blocking operations.
     *
     * @return CompletableFuture with credentials
     */
    @Async("asyncExecutor")
    public CompletableFuture<AlpacaCredentialsDto> getCredentialsAsync() {
        return CompletableFuture.completedFuture(getCredentials());
    }

    /**
     * Registers or updates system credentials.
     *
     * @param request The registration request with API keys
     * @return true if credentials are valid and saved
     */
    @Caching(evict = {
            @CacheEvict(value = CacheNames.API_CREDENTIALS, key = "T(com.pborsa.api.domain.constants.SystemConstants).SYSTEM_USER_ID"),
            @CacheEvict(value = CacheNames.ALPACA_CLIENTS, key = "T(com.pborsa.api.domain.constants.SystemConstants).SYSTEM_USER_ID")
    })
    @Transactional
    public boolean registerCredentials(CredentialsRegistrationRequest request) {
        log.info("Registering system credentials");

        Long systemUserId = getSystemUserId();

        // Create temporary credentials for validation
        AlpacaCredentialsDto tempCredentials = AlpacaCredentialsDto.builder()
                .userId(systemUserId)
                .apiKey(request.apiKey())
                .secretKey(request.secretKey())
                .paperTrading(request.paperTrading())
                .build();

        // Validate credentials before saving
        if (!alpacaClientFactory.validateCredentials(tempCredentials)) {
            log.warn("Invalid system credentials provided");
            return false;
        }

        // Encrypt and save
        UserApiCredentials entity = credentialsRepository.findByUserId(systemUserId)
                .orElse(UserApiCredentials.builder().userId(systemUserId).build());

        entity.setApiKeyEncrypted(encryptionService.encrypt(request.apiKey()));
        entity.setSecretKeyEncrypted(encryptionService.encrypt(request.secretKey()));
        entity.setPaperTrading(request.paperTrading());
        entity.setActive(true);

        credentialsRepository.save(entity);
        log.info("Successfully registered system credentials");

        eventPublisher.publishEvent(new CredentialsChangedEvent(systemUserId, true));

        return true;
    }

    /**
     * Deactivates system credentials.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheNames.API_CREDENTIALS, key = "T(com.pborsa.api.domain.constants.SystemConstants).SYSTEM_USER_ID"),
            @CacheEvict(value = CacheNames.ALPACA_CLIENTS, key = "T(com.pborsa.api.domain.constants.SystemConstants).SYSTEM_USER_ID")
    })
    @Transactional
    public void deactivateCredentials() {
        log.info("Deactivating system credentials");
        Long systemUserId = getSystemUserId();
        credentialsRepository.deactivateByUserId(systemUserId);
        alpacaClientFactory.evictClient(systemUserId);
        eventPublisher.publishEvent(new CredentialsChangedEvent(systemUserId, false));
    }

    /**
     * Checks if system credentials exist.
     *
     * @return true if active system credentials exist
     */
    @Transactional(readOnly = true)
    public boolean hasCredentials() {
        return credentialsRepository.findByUserId(getSystemUserId())
                .map(UserApiCredentials::isActive)
                .orElse(false);
    }

    /**
     * Refreshes the cached system credentials.
     *
     * @return Updated credentials
     */
    @CacheEvict(value = CacheNames.API_CREDENTIALS, key = "T(com.pborsa.api.domain.constants.SystemConstants).SYSTEM_USER_ID")
    @Transactional(readOnly = true)
    public AlpacaCredentialsDto refreshCredentials() {
        log.debug("Refreshing system credentials cache");
        // After evicting cache, fetch fresh data
        // Note: Direct call bypasses cache due to @CacheEvict, which is what we want
        return getCredentialsInternal();
    }

    /**
     * Gets the system user ID constant.
     * Used for cache key generation in SpEL expressions.
     */
    public Long getSystemUserId() {
        return SystemConstants.SYSTEM_USER_ID;
    }

    /**
     * Internal method to fetch system credentials without cache.
     * Used by refreshCredentials to avoid self-invocation cache issues.
     */
    private AlpacaCredentialsDto getCredentialsInternal() {
        log.debug("Fetching system credentials from database (uncached)");

        Long systemUserId = getSystemUserId();
        UserApiCredentials entity = credentialsRepository.findByUserId(systemUserId)
                .orElseThrow(() -> new CredentialsNotFoundException(systemUserId));

        if (!entity.isActive()) {
            throw new CredentialsNotFoundException(systemUserId);
        }

        return AlpacaCredentialsDto.builder()
                .userId(entity.getUserId())
                .apiKey(encryptionService.decrypt(entity.getApiKeyEncrypted()))
                .secretKey(encryptionService.decrypt(entity.getSecretKeyEncrypted()))
                .paperTrading(entity.isPaperTrading())
                .build();
    }
}

