package com.pborsa.api.security;

import com.google.firebase.auth.FirebaseToken;

import java.util.Map;

/**
 * Authenticated Firebase user details extracted from the ID token.
 */
public record FirebaseUserPrincipal(
        String uid,
        String email,
        String displayName,
        String provider
) {
    public static FirebaseUserPrincipal fromToken(FirebaseToken token) {
        String provider = null;
        Object firebaseClaim = token.getClaims().get("firebase");
        if (firebaseClaim instanceof Map<?, ?> map) {
            Object signInProvider = map.get("sign_in_provider");
            if (signInProvider != null) {
                provider = signInProvider.toString();
            }
        }
        return new FirebaseUserPrincipal(
                token.getUid(),
                token.getEmail(),
                token.getName(),
                provider
        );
    }
}
