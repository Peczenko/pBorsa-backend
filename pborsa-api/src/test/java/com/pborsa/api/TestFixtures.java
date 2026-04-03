package com.pborsa.api;

import com.pborsa.domain.dto.strategy.BaseStrategyDto;
import com.pborsa.api.user.UserRole;
import com.pborsa.domain.dto.user.UserStatus;
import com.pborsa.api.strategy.entity.BaseStrategyEntity;
import com.pborsa.api.user.UserEntity;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;

import java.time.Instant;

/**
 * Factory methods for creating test entities and DTOs.
 * <p>
 * Provides convenient methods to create properly initialized test objects
 * with sensible defaults that can be customized as needed.
 */
public final class TestFixtures {

    private TestFixtures() {
        // Utility class - prevent instantiation
    }

    // ========== FirebaseUserPrincipal ==========

    /**
     * Creates a test FirebaseUserPrincipal with default values.
     */
    public static FirebaseUserPrincipal createFirebaseUserPrincipal() {
        return createFirebaseUserPrincipal(
                TestDataConstants.DEFAULT_TEST_UID,
                TestDataConstants.DEFAULT_TEST_EMAIL
        );
    }

    /**
     * Creates a test FirebaseUserPrincipal with specified uid and email.
     */
    public static FirebaseUserPrincipal createFirebaseUserPrincipal(String uid, String email) {
        return new FirebaseUserPrincipal(
                uid,
                email,
                TestDataConstants.DEFAULT_TEST_DISPLAY_NAME,
                TestDataConstants.DEFAULT_TEST_PROVIDER,
                false
        );
    }

    /**
     * Creates a test FirebaseUserPrincipal with admin role.
     */
    public static FirebaseUserPrincipal createAdminPrincipal() {
        return new FirebaseUserPrincipal(
                TestDataConstants.TEST_ADMIN_UID,
                TestDataConstants.TEST_ADMIN_EMAIL,
                "Test Admin",
                TestDataConstants.DEFAULT_TEST_PROVIDER,
                true
        );
    }

    // ========== UserEntity ==========

    /**
     * Creates a test UserEntity with default values.
     */
    public static UserEntity createUserEntity() {
        return createUserEntity(
                TestDataConstants.DEFAULT_TEST_UID,
                TestDataConstants.DEFAULT_TEST_EMAIL
        );
    }

    /**
     * Creates a test UserEntity with specified uid and email.
     */
    public static UserEntity createUserEntity(String firebaseUid, String email) {
        return new UserEntity()
                .setFirebaseUid(firebaseUid)
                .setEmail(email)
                .setDisplayName(TestDataConstants.DEFAULT_TEST_DISPLAY_NAME)
                .setProvider(TestDataConstants.DEFAULT_TEST_PROVIDER)
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);
    }

    /**
     * Creates a test admin UserEntity.
     */
    public static UserEntity createAdminUserEntity() {
        return new UserEntity()
                .setFirebaseUid(TestDataConstants.TEST_ADMIN_UID)
                .setEmail(TestDataConstants.TEST_ADMIN_EMAIL)
                .setDisplayName("Test Admin")
                .setProvider(TestDataConstants.DEFAULT_TEST_PROVIDER)
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.ADMIN);
    }

    // ========== BaseStrategyEntity ==========

    /**
     * Creates a test BaseStrategyEntity with default values.
     */
    public static BaseStrategyEntity createBaseStrategyEntity() {
        return createBaseStrategyEntity(
                TestDataConstants.TEST_BASE_STRATEGY_CODE,
                TestDataConstants.TEST_BASE_STRATEGY_NAME,
                true
        );
    }

    /**
     * Creates a test BaseStrategyEntity with specified values.
     */
    public static BaseStrategyEntity createBaseStrategyEntity(String code, String name, boolean active) {
        BaseStrategyEntity entity = new BaseStrategyEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setDescription("Test strategy description for " + name);
        entity.setActive(active);
        return entity;
    }

    /**
     * Creates an inactive test BaseStrategyEntity.
     */
    public static BaseStrategyEntity createInactiveBaseStrategyEntity() {
        return createBaseStrategyEntity(
                TestDataConstants.TEST_INACTIVE_STRATEGY_CODE,
                "Test Inactive Strategy",
                false
        );
    }

    // ========== BaseStrategyDto ==========

    /**
     * Creates a test BaseStrategyDto with default values.
     */
    public static BaseStrategyDto createBaseStrategyDto() {
        return createBaseStrategyDto(
                TestDataConstants.TEST_BASE_STRATEGY_ID,
                TestDataConstants.TEST_BASE_STRATEGY_CODE,
                TestDataConstants.TEST_BASE_STRATEGY_NAME,
                true
        );
    }

    /**
     * Creates a test BaseStrategyDto with specified values.
     */
    public static BaseStrategyDto createBaseStrategyDto(Long id, String code, String name, boolean active) {
        Instant now = Instant.now();
        return new BaseStrategyDto(
                id,
                code,
                name,
                "Test strategy description for " + name,
                active,
                now,
                now
        );
    }

    /**
     * Creates an inactive test BaseStrategyDto.
     */
    public static BaseStrategyDto createInactiveBaseStrategyDto() {
        return createBaseStrategyDto(
                TestDataConstants.TEST_INACTIVE_STRATEGY_ID,
                TestDataConstants.TEST_INACTIVE_STRATEGY_CODE,
                "Test Inactive Strategy",
                false
        );
    }
}
