package com.pborsa.domain.dto.credentials;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for registering user's Alpaca API credentials.
 */
public record CredentialsRegistrationRequest(
        @NotBlank(message = "API key is required")
        @Size(min = 10, max = 100, message = "API key must be between 10 and 100 characters")
        String apiKey,

        @NotBlank(message = "Secret key is required")
        @Size(min = 10, max = 100, message = "Secret key must be between 10 and 100 characters")
        String secretKey,

        boolean paperTrading
) {
    /**
     * Creates a request with paper trading enabled by default.
     */
    public CredentialsRegistrationRequest(String apiKey, String secretKey) {
        this(apiKey, secretKey, true);
    }
}

