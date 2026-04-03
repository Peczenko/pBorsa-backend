package com.pborsa.domain.event;

/**
 * Published when user API credentials are created, updated, or deactivated.
 */
public record CredentialsChangedEvent(Long userId, boolean active) {
}
