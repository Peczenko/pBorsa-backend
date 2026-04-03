package com.pborsa.api.repository;

import com.pborsa.api.BaseRepositoryTest;
import com.pborsa.api.config.TestCacheConfig;
import com.pborsa.api.user.UserEntity;
import com.pborsa.api.user.UserRepository;
import com.pborsa.api.user.UserRole;
import com.pborsa.domain.dto.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link UserRepository}.
 * <p>
 * Uses @Transactional for automatic rollback after each test.
 * Test data is created within each test method.
 */
@Import(TestCacheConfig.class)
@DisplayName("UserRepository")
class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity createAndPersistUser(String firebaseUid, String email, String displayName) {
        UserEntity entity = new UserEntity()
                .setFirebaseUid(firebaseUid)
                .setEmail(email)
                .setDisplayName(displayName)
                .setProvider("google.com")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);
        return entityManager.persist(entity);
    }

    @Nested
    @DisplayName("findByFirebaseUid")
    class FindByFirebaseUid {

        @Test
        @DisplayName("should return user when Firebase UID exists")
        void returnsUserWhenFirebaseUidExists() {
            // given
            String firebaseUid = "test-firebase-uid-9100";
            createAndPersistUser(firebaseUid, "user9100@test.com", "Test User 9100");
            entityManager.flush();

            // when
            Optional<UserEntity> result = repository.findByFirebaseUid(firebaseUid);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getFirebaseUid()).isEqualTo(firebaseUid);
            assertThat(result.get().getEmail()).isEqualTo("user9100@test.com");
            assertThat(result.get().getDisplayName()).isEqualTo("Test User 9100");
        }

        @Test
        @DisplayName("should return empty when Firebase UID does not exist")
        void returnsEmptyWhenFirebaseUidNotFound() {
            // when
            Optional<UserEntity> result = repository.findByFirebaseUid("nonexistent-uid");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should be case sensitive for Firebase UID")
        void isCaseSensitiveForFirebaseUid() {
            // given
            String firebaseUid = "CaseSensitiveUid9101";
            createAndPersistUser(firebaseUid, "case9101@test.com", "Case Test");
            entityManager.flush();

            // when
            Optional<UserEntity> upperResult = repository.findByFirebaseUid("CaseSensitiveUid9101");
            Optional<UserEntity> lowerResult = repository.findByFirebaseUid("casesensitiveuid9101");

            // then
            assertThat(upperResult).isPresent();
            assertThat(lowerResult).isEmpty();
        }

        @Test
        @DisplayName("should find user with special characters in Firebase UID")
        void findsUserWithSpecialCharactersInFirebaseUid() {
            // given - Firebase UIDs can contain various characters
            String firebaseUid = "user_9102-abc.xyz:123";
            createAndPersistUser(firebaseUid, "special9102@test.com", "Special User");
            entityManager.flush();

            // when
            Optional<UserEntity> result = repository.findByFirebaseUid(firebaseUid);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getFirebaseUid()).isEqualTo(firebaseUid);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return user when ID exists")
        void returnsUserWhenIdExists() {
            // given
            UserEntity saved = createAndPersistUser("uid-findbyid-9103", "findbyid9103@test.com", "FindById User");
            entityManager.flush();
            Long id = saved.getId();

            // when
            Optional<UserEntity> result = repository.findById(id);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getFirebaseUid()).isEqualTo("uid-findbyid-9103");
        }

        @Test
        @DisplayName("should return empty when ID does not exist")
        void returnsEmptyWhenIdNotFound() {
            // when
            Optional<UserEntity> result = repository.findById(999999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist new user with all required fields")
        void persistsNewUserWithAllRequiredFields() {
            // given
            UserEntity entity = new UserEntity()
                    .setFirebaseUid("new-user-uid-9104")
                    .setEmail("newuser9104@test.com")
                    .setDisplayName("New User 9104")
                    .setProvider("password")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);

            // when
            UserEntity saved = repository.save(entity);
            entityManager.flush();

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
            assertThat(saved.getFirebaseUid()).isEqualTo("new-user-uid-9104");
        }

        @Test
        @DisplayName("should persist admin user")
        void persistsAdminUser() {
            // given
            UserEntity entity = new UserEntity()
                    .setFirebaseUid("admin-user-uid-9105")
                    .setEmail("admin9105@test.com")
                    .setDisplayName("Admin User")
                    .setProvider("google.com")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.ADMIN);

            // when
            UserEntity saved = repository.save(entity);
            entityManager.flush();

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("should update existing user")
        void updatesExistingUser() {
            // given
            UserEntity entity = createAndPersistUser("update-user-uid-9106", "update9106@test.com", "Original Name");
            entityManager.flush();
            Long id = entity.getId();

            // when
            entity.setDisplayName("Updated Name");
            entity.setStatus(UserStatus.DISABLED);
            repository.save(entity);
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<UserEntity> updated = repository.findById(id);
            assertThat(updated).isPresent();
            assertThat(updated.get().getDisplayName()).isEqualTo("Updated Name");
            assertThat(updated.get().getStatus()).isEqualTo(UserStatus.DISABLED);
        }

        @Test
        @DisplayName("should handle user without email")
        void handlesUserWithoutEmail() {
            // given - email can be null
            UserEntity entity = new UserEntity()
                    .setFirebaseUid("no-email-uid-9107")
                    .setEmail(null)
                    .setDisplayName("No Email User")
                    .setProvider("anonymous")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);

            // when
            UserEntity saved = repository.save(entity);
            entityManager.flush();

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEmail()).isNull();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete existing user")
        void deletesExistingUser() {
            // given
            UserEntity entity = createAndPersistUser("delete-uid-9108", "delete9108@test.com", "Delete User");
            entityManager.flush();
            Long id = entity.getId();

            // when
            repository.delete(entity);
            entityManager.flush();

            // then
            Optional<UserEntity> result = repository.findById(id);
            assertThat(result).isEmpty();
        }
    }
}
