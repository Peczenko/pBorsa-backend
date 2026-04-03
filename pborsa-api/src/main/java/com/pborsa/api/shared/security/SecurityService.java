package com.pborsa.api.shared.security;

import com.pborsa.api.user.UserEntity;
import com.pborsa.api.user.UserRepository;
import com.pborsa.domain.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for security-related operations including user ID validation and access control.
 * Admin status is determined by the 'admin' custom claim in the Firebase ID token.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final UserRepository userRepository;

    /**
     * Resolves the target user ID for a query, considering admin privileges.
     * - If the authenticated user has admin=true claim: returns the requested userId (can access any user's data)
     * - If the authenticated user has admin=false: returns the authenticated user's ID (ignores path parameter)
     * 
     * This method MUST be used in controllers instead of trusting path parameters.
     *
     * @param requestedUserId The user ID from the request path
     * @param principal       Firebase user principal
     * @return The user ID to use for queries
     * @throws AccessDeniedException if user is not authenticated or not found
     */
    public Long resolveTargetUserId(Long requestedUserId, FirebaseUserPrincipal principal) {
        if (requestedUserId == null) {
            throw new AccessDeniedException("User ID is required");
        }

        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        Long currentUserId = getCurrentUserId(principal);

        if (isAdmin(principal)) {
            log.debug("Admin user {} accessing data for user {}", currentUserId, requestedUserId);
            // Validate that the target user exists
            if (!userRepository.existsById(requestedUserId)) {
                throw new AccessDeniedException("Target user not found: " + requestedUserId);
            }
            return requestedUserId;
        }

        // Regular user can only access their own data
        if (!currentUserId.equals(requestedUserId)) {
            log.warn("User {} attempted to access user {} data", currentUserId, requestedUserId);
            throw new AccessDeniedException("Access denied: You can only access your own data");
        }

        return currentUserId;
    }

    /**
     * Gets the current authenticated user's ID.
     *
     * @param principal Firebase user principal
     * @return Internal user ID
     * @throws AccessDeniedException if user cannot be found
     */
    public Long getCurrentUserId(FirebaseUserPrincipal principal) {
        return getCurrentUserEntity(principal).getId();
    }

    /**
     * Gets the current authenticated user entity.
     *
     * @param principal Firebase user principal
     * @return User entity
     * @throws AccessDeniedException if user cannot be found
     */
    public UserEntity getCurrentUserEntity(FirebaseUserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        Optional<UserEntity> user = userRepository.findByFirebaseUid(principal.uid());
        if (user.isEmpty()) {
            log.warn("User not found for Firebase UID: {}", principal.uid());
            throw new AccessDeniedException("User not found");
        }

        return user.get();
    }

    /**
     * Checks if the authenticated user has admin claim.
     * Admin status is determined by the 'admin' custom claim in the Firebase ID token.
     *
     * @param principal Firebase user principal
     * @return true if user has admin=true claim
     */
    public boolean isAdmin(FirebaseUserPrincipal principal) {
        if (principal == null) {
            return false;
        }
        return principal.admin();
    }

    /**
     * Requires the authenticated user to be an admin.
     * Throws AccessDeniedException if the user is not an admin.
     *
     * @param principal Firebase user principal
     * @throws AccessDeniedException if user is not an admin
     */
    public void requireAdmin(FirebaseUserPrincipal principal) {
        if (!isAdmin(principal)) {
            log.warn("Non-admin user attempted to access admin-only resource");
            throw new AccessDeniedException("Access denied: Admin privileges required");
        }
    }

    /**
     * Gets the current user ID with transactional support.
     *
     * @param principal Firebase user principal
     * @return Internal user ID
     * @throws AccessDeniedException if user cannot be found
     */
    @Transactional(readOnly = true)
    public Long getCurrentUserIdTransactional(FirebaseUserPrincipal principal) {
        return getCurrentUserId(principal);
    }

    /**
     * @deprecated Use {@link #resolveTargetUserId(Long, FirebaseUserPrincipal)} instead.
     * This method only validates access but requires a separate call to get the user ID.
     */
    @Deprecated
    public void validateUserAccess(Long requestedUserId, FirebaseUserPrincipal principal) {
        resolveTargetUserId(requestedUserId, principal);
    }

    /**
     * Gets the current user ID or throws an exception if not authenticated.
     *
     * @param principal Firebase user principal
     * @return Current user ID
     * @throws AccessDeniedException if user is not authenticated
     */
    public Long requireCurrentUserId(FirebaseUserPrincipal principal) {
        return getCurrentUserId(principal);
    }
}
