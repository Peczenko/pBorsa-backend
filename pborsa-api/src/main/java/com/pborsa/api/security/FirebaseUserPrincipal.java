package com.pborsa.api.security;

import com.google.firebase.auth.FirebaseToken;

import java.util.Map;

/**
 * Authenticated Firebase user details extracted from the ID token.
 * Includes custom claims like admin status.
 */
public record FirebaseUserPrincipal(
        String uid,
        String email,
        String displayName,
        String provider,
        boolean admin
) {
    /**
     * Custom claim key for admin status.
     */
    public static final String ADMIN_CLAIM = "admin";

    public static FirebaseUserPrincipal fromToken(FirebaseToken token) {
        String provider = null;
        Object firebaseClaim = token.getClaims().get("firebase");
        if (firebaseClaim instanceof Map<?, ?> map) {
            Object signInProvider = map.get("sign_in_provider");
            if (signInProvider != null) {
                provider = signInProvider.toString();
            }
        }

        // Extract admin claim, default to false
        boolean admin = false;
        Object adminClaim = token.getClaims().get(ADMIN_CLAIM);
        if (adminClaim instanceof Boolean b) {
            admin = b;
        }

        return new FirebaseUserPrincipal(
                token.getUid(),
                token.getEmail(),
                token.getName(),
                provider,
                admin
        );
    }
}
