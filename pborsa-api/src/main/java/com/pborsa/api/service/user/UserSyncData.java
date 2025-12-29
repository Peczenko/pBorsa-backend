package com.pborsa.api.service.user;

public record UserSyncData(
        String firebaseUid,
        String email,
        String displayName,
        String provider
) {
}
