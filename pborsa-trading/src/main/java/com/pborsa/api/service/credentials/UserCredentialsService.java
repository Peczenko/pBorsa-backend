package com.pborsa.api.service.credentials;

import com.pborsa.api.config.cache.CacheNames;
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
 * Service for managing user Alpaca API credentials.
 * Handles secure storage, retrieval, and caching of credentials.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCredentialsService {

    private final UserApiCredentialsRepository credentialsRepository;
    private final EncryptionService encryptionService;
    private final AlpacaClientFactory alpacaClientFactory;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Retrieves decrypted credentials for a user.
     * Results are cached for fast subsequent access.
     *
     * @param userId The user ID
     * @return Decrypted credentials DTO
     * @throws CredentialsNotFoundException if credentials don't exist
     */
    @Cacheable(
            value = CacheNames.API_CREDENTIALS,
            key = "#userId",
            cacheManager = "apiCredentialsCacheManager",
            sync = true
    )
    @Transactional(readOnly = true)
    public AlpacaCredentialsDto getCredentials(String userId) {
        // Delegate to internal method to avoid code duplication
        return getCredentialsInternal(userId);
    }

    /**
     * Async version of getCredentials for non-blocking operations.
     *
     * @param userId The user ID
     * @return CompletableFuture with credentials
     */
    @Async("asyncExecutor")
    public CompletableFuture<AlpacaCredentialsDto> getCredentialsAsync(String userId) {
        return CompletableFuture.completedFuture(getCredentials(userId));
    }

    /**
     * Registers or updates user credentials.
     *
     * @param userId  The user ID
     * @param request The registration request with API keys
     * @return true if credentials are valid and saved
     */
    @Caching(evict = {
            @CacheEvict(value = CacheNames.API_CREDENTIALS, key = "#userId"),
            @CacheEvict(value = CacheNames.ALPACA_CLIENTS, key = "#userId")
    })
    @Transactional
    public boolean registerCredentials(String userId, CredentialsRegistrationRequest request) {
        log.info("Registering credentials for user: {}", userId);

        // Create temporary credentials for validation
        AlpacaCredentialsDto tempCredentials = AlpacaCredentialsDto.builder()
                .userId(userId)
                .apiKey(request.apiKey())
                .secretKey(request.secretKey())
                .paperTrading(request.paperTrading())
                .build();

        // Validate credentials before saving
        if (!alpacaClientFactory.validateCredentials(tempCredentials)) {
            log.warn("Invalid credentials provided for user: {}", userId);
            return false;
        }

        // Encrypt and save
        UserApiCredentials entity = credentialsRepository.findByUserId(userId)
                .orElse(UserApiCredentials.builder().userId(userId).build());

        entity.setApiKeyEncrypted(encryptionService.encrypt(request.apiKey()));
        entity.setSecretKeyEncrypted(encryptionService.encrypt(request.secretKey()));
        entity.setPaperTrading(request.paperTrading());
        entity.setActive(true);

        credentialsRepository.save(entity);
        log.info("Successfully registered credentials for user: {}", userId);

        eventPublisher.publishEvent(new CredentialsChangedEvent(userId, true));
        
        return true;
    }

    /**
     * Deactivates user credentials.
     *
     * @param userId The user ID
     */
    @Caching(evict = {
            @CacheEvict(value = CacheNames.API_CREDENTIALS, key = "#userId"),
            @CacheEvict(value = CacheNames.ALPACA_CLIENTS, key = "#userId")
    })
    @Transactional
    public void deactivateCredentials(String userId) {
        log.info("Deactivating credentials for user: {}", userId);
        credentialsRepository.deactivateByUserId(userId);
        alpacaClientFactory.evictClient(userId);
        eventPublisher.publishEvent(new CredentialsChangedEvent(userId, false));
    }

    /**
     * Checks if credentials exist for a user.
     *
     * @param userId The user ID
     * @return true if active credentials exist
     */
    @Transactional(readOnly = true)
    public boolean hasCredentials(String userId) {
        return credentialsRepository.findByUserId(userId)
                .map(UserApiCredentials::isActive)
                .orElse(false);
    }

    /**
     * Refreshes the cached credentials.
     *
     * @param userId The user ID
     * @return Updated credentials
     */
    @CacheEvict(value = CacheNames.API_CREDENTIALS, key = "#userId")
    @Transactional(readOnly = true)
    public AlpacaCredentialsDto refreshCredentials(String userId) {
        log.debug("Refreshing credentials cache for user: {}", userId);
        // After evicting cache, fetch fresh data
        // Note: Direct call bypasses cache due to @CacheEvict, which is what we want
        return getCredentialsInternal(userId);
    }

    /**
     * Internal method to fetch credentials without cache.
     * Used by refreshCredentials to avoid self-invocation cache issues.
     */
    private AlpacaCredentialsDto getCredentialsInternal(String userId) {
        log.debug("Fetching credentials from database (uncached) for user: {}", userId);
        
        UserApiCredentials entity = credentialsRepository.findByUserId(userId)
                .orElseThrow(() -> new CredentialsNotFoundException(userId));

        if (!entity.isActive()) {
            throw new CredentialsNotFoundException(userId);
        }

        return AlpacaCredentialsDto.builder()
                .userId(entity.getUserId())
                .apiKey(encryptionService.decrypt(entity.getApiKeyEncrypted()))
                .secretKey(encryptionService.decrypt(entity.getSecretKeyEncrypted()))
                .paperTrading(entity.isPaperTrading())
                .build();
    }
}

