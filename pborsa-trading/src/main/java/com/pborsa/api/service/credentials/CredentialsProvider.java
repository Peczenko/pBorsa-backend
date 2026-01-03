package com.pborsa.api.service.credentials;

import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;

/**
 * Interface for providing Alpaca API credentials.
 * Implementations can provide either user-specific or system credentials.
 */
public interface CredentialsProvider {

    /**
     * Retrieves credentials for the given user ID.
     * 
     * @param userId User ID. Use null or SystemConstants.SYSTEM_USER_ID for system credentials.
     * @return Decrypted credentials DTO
     */
    AlpacaCredentialsDto getCredentials(Long userId);
}


