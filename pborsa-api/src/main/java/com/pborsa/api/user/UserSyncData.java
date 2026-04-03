package com.pborsa.api.user;

public record UserSyncData(
        String firebaseUid,
        String email,
        String displayName,
        String provider
) {
}
