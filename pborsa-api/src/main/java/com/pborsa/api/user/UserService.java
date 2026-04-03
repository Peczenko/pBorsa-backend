package com.pborsa.api.user;

import com.pborsa.api.admin.FirebaseAdminService;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import com.pborsa.domain.dto.user.UserProfileDto;
import com.pborsa.domain.dto.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String DEFAULT_PROVIDER = "unknown";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FirebaseAdminService firebaseAdminService;

    @Transactional
    public UserEntity syncUser(FirebaseUserPrincipal principal) {
        UserSyncData data = buildSyncData(principal);
        boolean isNewUser = false;

        UserEntity entity = userRepository.findByFirebaseUid(data.firebaseUid())
                .orElseGet(() -> {
                    return new UserEntity()
                            .setFirebaseUid(data.firebaseUid())
                            .setStatus(UserStatus.ACTIVE)
                            .setRole(UserRole.USER); // Default role
                });

        // Check if this is a new user (no ID yet)
        if (entity.getId() == null) {
            isNewUser = true;
        }

        boolean changed = applySync(entity, data);

        if (changed || entity.getCreatedAt() == null) {
            entity = userRepository.save(entity);
            log.info("Synced user {}", entity.getId());
        }

        // Initialize default admin=false claim for new users
        if (isNewUser) {
            firebaseAdminService.initializeDefaultClaims(data.firebaseUid());
        }

        return entity;
    }

    @Transactional
    public UserProfileDto syncUserProfile(FirebaseUserPrincipal principal) {
        return userMapper.toProfile(syncUser(principal));
    }

    private UserSyncData buildSyncData(FirebaseUserPrincipal principal) {
        return new UserSyncData(
                principal.uid(),
                normalize(principal.email()),
                normalize(principal.displayName()),
                normalizeOrDefault(principal.provider(), DEFAULT_PROVIDER)
        );
    }

    private boolean applySync(UserEntity entity, UserSyncData data) {
        boolean changed = false;
        changed |= updateIfChanged(entity.getFirebaseUid(), data.firebaseUid(), entity::setFirebaseUid);
        changed |= updateIfChanged(entity.getEmail(), data.email(), entity::setEmail);
        changed |= updateIfChanged(entity.getDisplayName(), data.displayName(), entity::setDisplayName);
        changed |= updateIfChanged(entity.getProvider(), data.provider(), entity::setProvider);

        if (entity.getStatus() == null) {
            entity.setStatus(UserStatus.ACTIVE);
            changed = true;
        }

        return changed;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        String normalized = normalize(value);
        return normalized != null ? normalized : defaultValue;
    }

    private boolean updateIfChanged(String current, String next, java.util.function.Consumer<String> setter) {
        if (equals(current, next)) {
            return false;
        }
        setter.accept(next);
        return true;
    }

    private boolean equals(String left, String right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }

}
