package com.pborsa.api.admin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.pborsa.api.user.UserRole;
import com.pborsa.api.user.UserEntity;
import com.pborsa.domain.exception.AccessDeniedException;
import com.pborsa.api.user.UserRepository;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing Firebase custom claims and admin status.
 * Provides methods to grant and revoke admin privileges.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseAdminService {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    /**
     * Sets the admin status for a user by updating their Firebase custom claims.
     * Also updates the local database role for consistency.
     *
     * @param targetUserId The internal user ID to update
     * @param admin        true to grant admin, false to revoke
     * @throws AccessDeniedException if target user is not found
     */
    @Transactional
    public void setAdminStatus(Long targetUserId, boolean admin) {
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + targetUserId));

        String firebaseUid = targetUser.getFirebaseUid();

        try {
            // Get current claims and update admin claim
            UserRecord userRecord = firebaseAuth.getUser(firebaseUid);
            Map<String, Object> currentClaims = userRecord.getCustomClaims();
            Map<String, Object> newClaims = new HashMap<>(currentClaims != null ? currentClaims : Map.of());
            newClaims.put(FirebaseUserPrincipal.ADMIN_CLAIM, admin);

            // Update Firebase custom claims
            firebaseAuth.setCustomUserClaims(firebaseUid, newClaims);

            // Update local DB role for consistency/audit
            targetUser.setRole(admin ? UserRole.ADMIN : UserRole.USER);
            userRepository.save(targetUser);

            log.info("Set admin={} for user {} (firebaseUid={})", admin, targetUserId, firebaseUid);
        } catch (FirebaseAuthException e) {
            log.error("Failed to update Firebase custom claims for user {}", targetUserId, e);
            throw new RuntimeException("Failed to update admin status: " + e.getMessage(), e);
        }
    }

    /**
     * Grants admin privileges to a user.
     *
     * @param targetUserId The internal user ID to grant admin to
     */
    @Transactional
    public void grantAdmin(Long targetUserId) {
        setAdminStatus(targetUserId, true);
    }

    /**
     * Revokes admin privileges from a user.
     *
     * @param targetUserId The internal user ID to revoke admin from
     */
    @Transactional
    public void revokeAdmin(Long targetUserId) {
        setAdminStatus(targetUserId, false);
    }

    /**
     * Gets the current admin status for a user from Firebase.
     *
     * @param targetUserId The internal user ID to check
     * @return true if user has admin claim set to true
     * @throws AccessDeniedException if target user is not found
     */
    public boolean getAdminStatus(Long targetUserId) {
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + targetUserId));

        try {
            UserRecord userRecord = firebaseAuth.getUser(targetUser.getFirebaseUid());
            Map<String, Object> claims = userRecord.getCustomClaims();
            if (claims == null) {
                return false;
            }
            Object adminClaim = claims.get(FirebaseUserPrincipal.ADMIN_CLAIM);
            return adminClaim instanceof Boolean b && b;
        } catch (FirebaseAuthException e) {
            log.error("Failed to get Firebase custom claims for user {}", targetUserId, e);
            throw new RuntimeException("Failed to get admin status: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes default admin claim (false) for a new user.
     * Should be called when a new user is created.
     *
     * @param firebaseUid The Firebase UID of the new user
     */
    public void initializeDefaultClaims(String firebaseUid) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(firebaseUid);
            Map<String, Object> currentClaims = userRecord.getCustomClaims();
            
            // Only set if admin claim doesn't exist
            if (currentClaims == null || !currentClaims.containsKey(FirebaseUserPrincipal.ADMIN_CLAIM)) {
                Map<String, Object> newClaims = new HashMap<>(currentClaims != null ? currentClaims : Map.of());
                newClaims.put(FirebaseUserPrincipal.ADMIN_CLAIM, false);
                firebaseAuth.setCustomUserClaims(firebaseUid, newClaims);
                log.debug("Initialized default admin=false claim for firebaseUid={}", firebaseUid);
            }
        } catch (FirebaseAuthException e) {
            log.warn("Failed to initialize default claims for firebaseUid={}: {}", firebaseUid, e.getMessage());
            // Don't throw - this is not critical for user creation
        }
    }
}


