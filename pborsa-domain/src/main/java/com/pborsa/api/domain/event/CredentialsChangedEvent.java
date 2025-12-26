package com.pborsa.api.domain.event;

/**
 * Published when user API credentials are created, updated, or deactivated.
 */
public record CredentialsChangedEvent(String userId, boolean active) {
}
