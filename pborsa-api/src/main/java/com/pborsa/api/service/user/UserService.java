package com.pborsa.api.service.user;

import com.pborsa.api.domain.dto.user.UserStatus;
import com.pborsa.api.domain.entity.UserEntity;
import com.pborsa.api.repository.UserRepository;
import com.pborsa.api.security.FirebaseUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity syncUser(FirebaseUserPrincipal principal) {
        UserEntity entity = userRepository.findByFirebaseUid(principal.uid())
                .orElseGet(() -> new UserEntity()
                        .setFirebaseUid(principal.uid())
                        .setStatus(UserStatus.ACTIVE));

        boolean changed = false;

        if (!equals(entity.getFirebaseUid(), principal.uid())) {
            entity.setFirebaseUid(principal.uid());
            changed = true;
        }

        String email = normalize(principal.email());
        if (!equals(entity.getEmail(), email)) {
            entity.setEmail(email);
            changed = true;
        }

        String displayName = normalize(principal.displayName());
        if (!equals(entity.getDisplayName(), displayName)) {
            entity.setDisplayName(displayName);
            changed = true;
        }

        String provider = normalize(principal.provider());
        if (provider == null) {
            provider = "unknown";
        }
        if (!equals(entity.getProvider(), provider)) {
            entity.setProvider(provider);
            changed = true;
        }

        if (entity.getStatus() == null) {
            entity.setStatus(UserStatus.ACTIVE);
            changed = true;
        }

        if (changed || entity.getCreatedAt() == null) {
            entity = userRepository.save(entity);
            log.info("Synced user {}", entity.getId());
        }

        return entity;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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
