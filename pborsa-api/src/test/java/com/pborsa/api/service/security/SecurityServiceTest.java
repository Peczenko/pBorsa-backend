package com.pborsa.api.service.security;

import com.pborsa.api.user.UserRole;
import com.pborsa.domain.dto.user.UserStatus;
import com.pborsa.api.user.UserEntity;
import com.pborsa.domain.exception.AccessDeniedException;
import com.pborsa.api.user.UserRepository;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import com.pborsa.api.shared.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityService")
class SecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityService securityService;

    private UserEntity testUser;
    private FirebaseUserPrincipal regularPrincipal;
    private FirebaseUserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity()
                .setId(100L)
                .setFirebaseUid("test-firebase-uid")
                .setEmail("test@example.com")
                .setDisplayName("Test User")
                .setProvider("firebase")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

        regularPrincipal = new FirebaseUserPrincipal(
                "test-firebase-uid",
                "test@example.com",
                "Test User",
                "firebase",
                false
        );

        adminPrincipal = new FirebaseUserPrincipal(
                "admin-firebase-uid",
                "admin@example.com",
                "Admin User",
                "firebase",
                true
        );
    }

    @Nested
    @DisplayName("resolveTargetUserId")
    class ResolveTargetUserId {

        @Test
        @DisplayName("should return own user ID for regular user")
        void returnsOwnUserIdForRegularUser() {
            // given
            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.of(testUser));

            // when
            Long result = securityService.resolveTargetUserId(100L, regularPrincipal);

            // then
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when regular user accesses other user")
        void throwsWhenRegularUserAccessesOtherUser() {
            // given
            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.of(testUser));

            // when/then
            assertThatThrownBy(() -> securityService.resolveTargetUserId(200L, regularPrincipal))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Access denied");
        }

        @Test
        @DisplayName("should allow admin to access any user")
        void allowsAdminToAccessAnyUser() {
            // given
            UserEntity adminUser = new UserEntity()
                    .setId(1L)
                    .setFirebaseUid("admin-firebase-uid")
                    .setEmail("admin@example.com");

            when(userRepository.findByFirebaseUid("admin-firebase-uid"))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.existsById(200L)).thenReturn(true);

            // when
            Long result = securityService.resolveTargetUserId(200L, adminPrincipal);

            // then
            assertThat(result).isEqualTo(200L);
        }

        @Test
        @DisplayName("should throw when admin accesses non-existent user")
        void throwsWhenAdminAccessesNonExistentUser() {
            // given
            UserEntity adminUser = new UserEntity()
                    .setId(1L)
                    .setFirebaseUid("admin-firebase-uid");

            when(userRepository.findByFirebaseUid("admin-firebase-uid"))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.existsById(999L)).thenReturn(false);

            // when/then
            assertThatThrownBy(() -> securityService.resolveTargetUserId(999L, adminPrincipal))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Target user not found");
        }

        @Test
        @DisplayName("should throw when user ID is null")
        void throwsWhenUserIdIsNull() {
            // when/then
            assertThatThrownBy(() -> securityService.resolveTargetUserId(null, regularPrincipal))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("User ID is required");
        }

        @Test
        @DisplayName("should throw when principal is null")
        void throwsWhenPrincipalIsNull() {
            // when/then
            assertThatThrownBy(() -> securityService.resolveTargetUserId(100L, null))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("User not authenticated");
        }
    }

    @Nested
    @DisplayName("getCurrentUserId")
    class GetCurrentUserId {

        @Test
        @DisplayName("should return user ID when user exists")
        void returnsUserIdWhenUserExists() {
            // given
            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.of(testUser));

            // when
            Long result = securityService.getCurrentUserId(regularPrincipal);

            // then
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("should throw when user not found")
        void throwsWhenUserNotFound() {
            // given
            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> securityService.getCurrentUserId(regularPrincipal))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should throw when principal is null")
        void throwsWhenPrincipalIsNull() {
            // when/then
            assertThatThrownBy(() -> securityService.getCurrentUserId(null))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("User not authenticated");
        }
    }

    @Nested
    @DisplayName("getCurrentUserEntity")
    class GetCurrentUserEntity {

        @Test
        @DisplayName("should return user entity when user exists")
        void returnsUserEntityWhenUserExists() {
            // given
            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.of(testUser));

            // when
            UserEntity result = securityService.getCurrentUserEntity(regularPrincipal);

            // then
            assertThat(result).isEqualTo(testUser);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should throw when user not found")
        void throwsWhenUserNotFound() {
            // given
            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> securityService.getCurrentUserEntity(regularPrincipal))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("isAdmin")
    class IsAdmin {

        @Test
        @DisplayName("should return true for admin user")
        void returnsTrueForAdminUser() {
            // when
            boolean result = securityService.isAdmin(adminPrincipal);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false for regular user")
        void returnsFalseForRegularUser() {
            // when
            boolean result = securityService.isAdmin(regularPrincipal);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false for null principal")
        void returnsFalseForNullPrincipal() {
            // when
            boolean result = securityService.isAdmin(null);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("requireAdmin")
    class RequireAdmin {

        @Test
        @DisplayName("should not throw for admin user")
        void doesNotThrowForAdminUser() {
            // when/then - no exception
            securityService.requireAdmin(adminPrincipal);
        }

        @Test
        @DisplayName("should throw for regular user")
        void throwsForRegularUser() {
            // when/then
            assertThatThrownBy(() -> securityService.requireAdmin(regularPrincipal))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Admin privileges required");
        }

        @Test
        @DisplayName("should throw for null principal")
        void throwsForNullPrincipal() {
            // when/then
            assertThatThrownBy(() -> securityService.requireAdmin(null))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Admin privileges required");
        }
    }
}
