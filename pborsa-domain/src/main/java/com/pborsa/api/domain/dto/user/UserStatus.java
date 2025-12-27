package com.pborsa.api.domain.dto.user;

/**
 * Status of a local user record linked to Firebase authentication.
 */
public enum UserStatus {
    ACTIVE,
    DISABLED,
    DELETED
}
