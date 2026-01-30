package com.pborsa.api.service.user;

import com.pborsa.api.domain.dto.user.UserProfileDto;
import com.pborsa.api.domain.dto.user.UserRole;
import com.pborsa.api.domain.dto.user.UserStatus;
import com.pborsa.api.domain.entity.UserEntity;
import com.pborsa.api.repository.UserRepository;
import com.pborsa.api.security.FirebaseUserPrincipal;
import com.pborsa.api.service.admin.FirebaseAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FirebaseAdminService firebaseAdminService;

    @InjectMocks
    private UserService userService;

    private FirebaseUserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new FirebaseUserPrincipal(
                "test-firebase-uid",
                "test@example.com",
                "Test User",
                "firebase",
                false
        );
    }

    @Nested
    @DisplayName("syncUser")
    class SyncUser {

        @Test
        @DisplayName("should create new user when not exists")
        void createsNewUserWhenNotExists() {
            // given
            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.empty());

            UserEntity savedUser = new UserEntity()
                    .setId(1L)
                    .setFirebaseUid("test-firebase-uid")
                    .setEmail("test@example.com")
                    .setDisplayName("Test User")
                    .setProvider("unknown")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);

            when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

            // when
            UserEntity result = userService.syncUser(principal);

            // then
            assertThat(result.getFirebaseUid()).isEqualTo("test-firebase-uid");
            assertThat(result.getEmail()).isEqualTo("test@example.com");

            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(captor.capture());

            UserEntity capturedEntity = captor.getValue();
            assertThat(capturedEntity.getFirebaseUid()).isEqualTo("test-firebase-uid");
            assertThat(capturedEntity.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(capturedEntity.getRole()).isEqualTo(UserRole.USER);

            // Should initialize default claims for new user
            verify(firebaseAdminService).initializeDefaultClaims("test-firebase-uid");
        }

        @Test
        @DisplayName("should update existing user when data changed")
        void updatesExistingUserWhenDataChanged() {
            // given
            UserEntity existingUser = new UserEntity()
                    .setId(1L)
                    .setFirebaseUid("test-firebase-uid")
                    .setEmail("old@example.com")
                    .setDisplayName("Old Name")
                    .setProvider("firebase")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER)
                    .setCreatedAt(Instant.now());

            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

            // when
            UserEntity result = userService.syncUser(principal);

            // then
            verify(userRepository).save(any(UserEntity.class));
            // Should not initialize claims for existing user
            verify(firebaseAdminService, never()).initializeDefaultClaims(any());
        }

        @Test
        @DisplayName("should not save when no changes")
        void doesNotSaveWhenNoChanges() {
            // given - existing user matches the principal data exactly
            UserEntity existingUser = new UserEntity()
                    .setId(1L)
                    .setFirebaseUid("test-firebase-uid")
                    .setEmail("test@example.com")
                    .setDisplayName("Test User")
                    .setProvider("firebase")  // Must match principal's provider
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER)
                    .setCreatedAt(Instant.now());

            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.of(existingUser));

            // when
            UserEntity result = userService.syncUser(principal);

            // then
            verify(userRepository, never()).save(any(UserEntity.class));
            verify(firebaseAdminService, never()).initializeDefaultClaims(any());
        }

        @Test
        @DisplayName("should handle null display name")
        void handlesNullDisplayName() {
            // given
            FirebaseUserPrincipal principalWithNoName = new FirebaseUserPrincipal(
                    "test-firebase-uid",
                    "test@example.com",
                    null,
                    "firebase",
                    false
            );

            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.empty());

            UserEntity savedUser = new UserEntity()
                    .setId(1L)
                    .setFirebaseUid("test-firebase-uid")
                    .setEmail("test@example.com")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);

            when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

            // when
            UserEntity result = userService.syncUser(principalWithNoName);

            // then
            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getDisplayName()).isNull();
        }

        @Test
        @DisplayName("should handle blank email")
        void handlesBlankEmail() {
            // given
            FirebaseUserPrincipal principalWithBlankEmail = new FirebaseUserPrincipal(
                    "test-firebase-uid",
                    "   ",
                    "Test User",
                    "firebase",
                    false
            );

            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.empty());

            UserEntity savedUser = new UserEntity()
                    .setId(1L)
                    .setFirebaseUid("test-firebase-uid")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);

            when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

            // when
            UserEntity result = userService.syncUser(principalWithBlankEmail);

            // then
            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getEmail()).isNull();
        }
    }

    @Nested
    @DisplayName("syncUserProfile")
    class SyncUserProfile {

        @Test
        @DisplayName("should return mapped user profile")
        void returnsMappedUserProfile() {
            // given
            UserEntity savedUser = new UserEntity()
                    .setId(1L)
                    .setFirebaseUid("test-firebase-uid")
                    .setEmail("test@example.com")
                    .setDisplayName("Test User")
                    .setProvider("unknown")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);

            UserProfileDto expectedProfile = new UserProfileDto(
                    1L,
                    "test-firebase-uid",
                    "test@example.com",
                    "Test User",
                    "unknown",
                    UserStatus.ACTIVE,
                    Instant.now(),
                    Instant.now()
            );

            when(userRepository.findByFirebaseUid("test-firebase-uid"))
                    .thenReturn(Optional.empty());
            when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
            when(userMapper.toProfile(savedUser)).thenReturn(expectedProfile);

            // when
            UserProfileDto result = userService.syncUserProfile(principal);

            // then
            assertThat(result).isEqualTo(expectedProfile);
            verify(userMapper).toProfile(savedUser);
        }
    }
}
