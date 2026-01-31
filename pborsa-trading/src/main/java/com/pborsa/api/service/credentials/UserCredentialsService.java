package com.pborsa.api.service.credentials;

import com.pborsa.api.config.cache.CacheNames;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.credentials.CredentialsRegistrationRequest;
import com.pborsa.api.domain.event.CredentialsChangedEvent;
import com.pborsa.api.domain.entity.UserApiCredentials;
import com.pborsa.api.exception.ActiveStrategiesExistException;
import com.pborsa.api.exception.CredentialsNotFoundException;
import com.pborsa.api.repository.UserApiCredentialsRepository;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.encryption.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class UserCredentialsService {

    private final UserApiCredentialsRepository credentialsRepository;
    private final EncryptionService encryptionService;
    private final AlpacaClientFactory alpacaClientFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final ActiveStrategyChecker activeStrategyChecker;

    @Autowired
    public UserCredentialsService(
            UserApiCredentialsRepository credentialsRepository,
            EncryptionService encryptionService,
            AlpacaClientFactory alpacaClientFactory,
            ApplicationEventPublisher eventPublisher,
            @Autowired(required = false) ActiveStrategyChecker activeStrategyChecker) {
        this.credentialsRepository = credentialsRepository;
        this.encryptionService = encryptionService;
        this.alpacaClientFactory = alpacaClientFactory;
        this.eventPublisher = eventPublisher;
        // Use no-op implementation if not provided (e.g., in tests or standalone usage)
        this.activeStrategyChecker = activeStrategyChecker != null
                ? activeStrategyChecker
                : userId -> false;
    }

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
    public AlpacaCredentialsDto getCredentials(Long userId) {
        return getCredentialsInternal(userId);
    }

    /**
     * Async version of getCredentials for non-blocking operations.
     *
     * @param userId The user ID
     * @return CompletableFuture with credentials
     */
    @Async("asyncExecutor")
    public CompletableFuture<AlpacaCredentialsDto> getCredentialsAsync(Long userId) {
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
            @CacheEvict(value = CacheNames.API_CREDENTIALS, key = "#userId", cacheManager = "apiCredentialsCacheManager"),
            @CacheEvict(value = CacheNames.ALPACA_CLIENTS, key = "#userId", cacheManager = "apiCredentialsCacheManager")
    })
    @Transactional
    public boolean registerCredentials(Long userId, CredentialsRegistrationRequest request) {
        log.info("Registering credentials for user: {}", userId);

        // Check if user has active strategies before allowing credential changes
        validateNoActiveStrategies(userId);

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
            @CacheEvict(value = CacheNames.API_CREDENTIALS, key = "#userId", cacheManager = "apiCredentialsCacheManager"),
            @CacheEvict(value = CacheNames.ALPACA_CLIENTS, key = "#userId", cacheManager = "apiCredentialsCacheManager")
    })
    @Transactional
    public void deactivateCredentials(Long userId) {
        log.info("Deactivating credentials for user: {}", userId);

        // Check if user has active strategies before allowing credential deactivation
        validateNoActiveStrategies(userId);

        credentialsRepository.deactivateByUserId(userId);
        alpacaClientFactory.evictClient(userId);
        eventPublisher.publishEvent(new CredentialsChangedEvent(userId, false));
    }

    /**
     * Validates that the user has no active or preparing strategies.
     * Throws exception if active strategies exist.
     *
     * @param userId User ID
     * @throws ActiveStrategiesExistException if user has active strategies
     */
    private void validateNoActiveStrategies(Long userId) {
        if (activeStrategyChecker.hasActiveStrategies(userId)) {
            log.warn("Cannot modify credentials for user {}: active strategies exist", userId);
            throw new ActiveStrategiesExistException(userId);
        }
    }

    /**
     * Checks if credentials exist for a user.
     *
     * @param userId The user ID
     * @return true if active credentials exist
     */
    @Transactional(readOnly = true)
    public boolean hasCredentials(Long userId) {
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
    @CacheEvict(value = CacheNames.API_CREDENTIALS, key = "#userId", cacheManager = "apiCredentialsCacheManager")
    @Transactional(readOnly = true)
    public AlpacaCredentialsDto refreshCredentials(Long userId) {
        log.debug("Refreshing credentials cache for user: {}", userId);
        // After evicting cache, fetch fresh data
        // Note: Direct call bypasses cache due to @CacheEvict, which is what we want
        return getCredentialsInternal(userId);
    }

    /**
     * Internal method to fetch credentials without cache.
     * Used by refreshCredentials to avoid self-invocation cache issues.
     */
    private AlpacaCredentialsDto getCredentialsInternal(Long userId) {
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

