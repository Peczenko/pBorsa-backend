package com.pborsa.trading.credentials;

import com.pborsa.domain.constants.SystemConstants;
import com.pborsa.domain.dto.credentials.AlpacaCredentialsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Unified service that provides credentials for both users and system operations.
 * Automatically routes to UserCredentialsService for regular users or
 * SystemCredentialsService for system operations.
 * 
 * This service implements the CredentialsProvider interface and serves as
 * a single entry point for credential retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedCredentialsService implements CredentialsProvider {

    private final UserCredentialsService userCredentialsService;
    private final SystemCredentialsService systemCredentialsService;

    /**
     * Retrieves credentials for the given user ID.
     * 
     * If userId is null or equals SYSTEM_USER_ID, returns system credentials.
     * Otherwise, returns user-specific credentials.
     *
     * @param userId User ID. Use null or SystemConstants.SYSTEM_USER_ID for system credentials.
     * @return Decrypted credentials DTO
     */
    @Override
    public AlpacaCredentialsDto getCredentials(Long userId) {
        if (userId == null || userId.equals(SystemConstants.SYSTEM_USER_ID)) {
            log.debug("Retrieving system credentials");
            return systemCredentialsService.getCredentials();
        } else {
            log.debug("Retrieving credentials for user: {}", userId);
            return userCredentialsService.getCredentials(userId);
        }
    }

    /**
     * Checks if credentials exist for the given user ID.
     * 
     * @param userId User ID. Use null or SystemConstants.SYSTEM_USER_ID for system credentials.
     * @return true if active credentials exist
     */
    public boolean hasCredentials(Long userId) {
        if (userId == null || userId.equals(SystemConstants.SYSTEM_USER_ID)) {
            return systemCredentialsService.hasCredentials();
        } else {
            return userCredentialsService.hasCredentials(userId);
        }
    }
}


