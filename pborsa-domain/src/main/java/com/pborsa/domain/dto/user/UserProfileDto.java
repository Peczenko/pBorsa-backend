package com.pborsa.domain.dto.user;

import java.time.Instant;

/**
 * User profile data returned by the API.
 */
public record UserProfileDto(
        Long id,
        String firebaseUid,
        String email,
        String displayName,
        String provider,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
