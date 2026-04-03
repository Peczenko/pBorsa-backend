package com.pborsa.domain.dto.credentials;

import lombok.Builder;

/**
 * DTO for decrypted Alpaca credentials used internally.
 * Never expose this directly via API responses.
 */
@Builder
public record AlpacaCredentialsDto(
        Long userId,
        String apiKey,
        String secretKey,
        boolean paperTrading
) {
    /**
     * Creates a masked version safe for logging.
     */
    public String toMaskedString() {
        return "AlpacaCredentials{userId='%s', apiKey='%s...', paperTrading=%s}"
                .formatted(userId, apiKey.substring(0, Math.min(4, apiKey.length())), paperTrading);
    }
}

