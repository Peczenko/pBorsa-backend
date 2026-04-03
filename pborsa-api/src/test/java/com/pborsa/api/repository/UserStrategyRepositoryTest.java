package com.pborsa.api.repository;

import com.pborsa.api.BaseRepositoryTest;
import com.pborsa.api.config.TestCacheConfig;
import com.pborsa.api.strategy.repository.UserStrategyRepository;
import com.pborsa.api.user.UserRole;
import com.pborsa.domain.dto.user.UserStatus;
import com.pborsa.api.strategy.entity.BaseStrategyEntity;
import com.pborsa.api.user.UserEntity;
import com.pborsa.api.strategy.entity.UserStrategyEntity;
import com.pborsa.domain.entity.UserStrategyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link UserStrategyRepository}.
 * <p>
 * Uses @Transactional for automatic rollback after each test.
 * Test data is created within each test method.
 */
@Import(TestCacheConfig.class)
@DisplayName("UserStrategyRepository")
class UserStrategyRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserStrategyRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity testUser;
    private BaseStrategyEntity testBaseStrategy;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new UserEntity()
                .setFirebaseUid("user-strategy-repo-test-uid-9130")
                .setEmail("user-strategy-9130@test.com")
                .setDisplayName("User Strategy Test User")
                .setProvider("test")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);
        testUser = entityManager.persist(testUser);

        // Create test base strategy
        testBaseStrategy = new BaseStrategyEntity();
        testBaseStrategy.setCode("USER_STRATEGY_REPO_TEST_9130");
        testBaseStrategy.setName("User Strategy Repo Test");
        testBaseStrategy.setDescription("For UserStrategyRepositoryTest");
        testBaseStrategy.setActive(true);
        testBaseStrategy = entityManager.persist(testBaseStrategy);

        entityManager.flush();
    }

    private UserStrategyEntity createUserStrategy(Long userId, BaseStrategyEntity baseStrategy,
                                                   String name, String symbol, UserStrategyStatus status) {
        UserStrategyEntity strategy = new UserStrategyEntity()
                .setUserId(userId)
                .setBaseStrategy(baseStrategy)
                .setName(name)
                .setSymbol(symbol)
                .setStatus(status)
                .setBudget(new BigDecimal("5000.00"));
        return entityManager.persist(strategy);
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("should return all strategies for user")
        void returnsAllStrategiesForUser() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Strategy 1", "TSLA", UserStrategyStatus.ACTIVE);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Strategy 2", "AAPL", UserStrategyStatus.PAUSED);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Strategy 3", "GOOGL", UserStrategyStatus.STOPPED);
            entityManager.flush();

            // when
            List<UserStrategyEntity> result = repository.findByUserId(testUser.getId());

            // then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(UserStrategyEntity::getSymbol)
                    .containsExactlyInAnyOrder("TSLA", "AAPL", "GOOGL");
        }

        @Test
        @DisplayName("should return empty list when user has no strategies")
        void returnsEmptyListWhenUserHasNoStrategies() {
            // when
            List<UserStrategyEntity> result = repository.findByUserId(999999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndStatus")
    class FindByUserIdAndStatus {

        @Test
        @DisplayName("should return only strategies with matching status")
        void returnsOnlyStrategiesWithMatchingStatus() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Active 1", "TSLA", UserStrategyStatus.ACTIVE);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Active 2", "AAPL", UserStrategyStatus.ACTIVE);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Paused 1", "GOOGL", UserStrategyStatus.PAUSED);
            entityManager.flush();

            // when
            List<UserStrategyEntity> result = repository.findByUserIdAndStatus(testUser.getId(), UserStrategyStatus.ACTIVE);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserStrategyEntity::getStatus)
                    .containsOnly(UserStrategyStatus.ACTIVE);
        }

        @Test
        @DisplayName("should return empty when no strategies match status")
        void returnsEmptyWhenNoStrategiesMatchStatus() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Active 1", "TSLA", UserStrategyStatus.ACTIVE);
            entityManager.flush();

            // when
            List<UserStrategyEntity> result = repository.findByUserIdAndStatus(testUser.getId(), UserStrategyStatus.STOPPED);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {

        @Test
        @DisplayName("should return strategy when ID and user ID match")
        void returnsStrategyWhenIdAndUserIdMatch() {
            // given
            UserStrategyEntity strategy = createUserStrategy(testUser.getId(), testBaseStrategy, "Test Strategy", "TSLA", UserStrategyStatus.ACTIVE);
            entityManager.flush();
            Long id = strategy.getId();

            // when
            Optional<UserStrategyEntity> result = repository.findByIdAndUserId(id, testUser.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Test Strategy");
        }

        @Test
        @DisplayName("should return empty when ID exists but user ID doesn't match")
        void returnsEmptyWhenUserIdDoesntMatch() {
            // given
            UserStrategyEntity strategy = createUserStrategy(testUser.getId(), testBaseStrategy, "Test Strategy", "TSLA", UserStrategyStatus.ACTIVE);
            entityManager.flush();
            Long id = strategy.getId();

            // when
            Optional<UserStrategyEntity> result = repository.findByIdAndUserId(id, 999999L);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when ID doesn't exist")
        void returnsEmptyWhenIdDoesntExist() {
            // when
            Optional<UserStrategyEntity> result = repository.findByIdAndUserId(999999L, testUser.getId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsRunningStrategyByUserIdAndBaseStrategyIdAndSymbol")
    class ExistsRunningStrategyByUserIdAndBaseStrategyIdAndSymbol {

        @Test
        @DisplayName("should return true when running combination exists")
        void returnsTrueWhenCombinationExists() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Existing Strategy", "TSLA", UserStrategyStatus.ACTIVE);
            entityManager.flush();

            // when
            boolean exists = repository.existsRunningStrategyByUserIdAndBaseStrategyIdAndSymbol(
                    testUser.getId(), testBaseStrategy.getId(), "TSLA");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when symbol differs")
        void returnsFalseWhenSymbolDiffers() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Existing Strategy", "TSLA", UserStrategyStatus.ACTIVE);
            entityManager.flush();

            // when
            boolean exists = repository.existsRunningStrategyByUserIdAndBaseStrategyIdAndSymbol(
                    testUser.getId(), testBaseStrategy.getId(), "AAPL");

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("should return false when user ID differs")
        void returnsFalseWhenUserIdDiffers() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Existing Strategy", "TSLA", UserStrategyStatus.ACTIVE);
            entityManager.flush();

            // when
            boolean exists = repository.existsRunningStrategyByUserIdAndBaseStrategyIdAndSymbol(
                    999999L, testBaseStrategy.getId(), "TSLA");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findByStatus")
    class FindByStatus {

        @Test
        @DisplayName("should return all strategies with given status")
        void returnsAllStrategiesWithGivenStatus() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Active 1", "TSLA", UserStrategyStatus.ACTIVE);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Paused 1", "AAPL", UserStrategyStatus.PAUSED);
            entityManager.flush();

            // when
            List<UserStrategyEntity> activeResult = repository.findByStatus(UserStrategyStatus.ACTIVE);
            List<UserStrategyEntity> pausedResult = repository.findByStatus(UserStrategyStatus.PAUSED);

            // then
            assertThat(activeResult).extracting(UserStrategyEntity::getSymbol).contains("TSLA");
            assertThat(pausedResult).extracting(UserStrategyEntity::getSymbol).contains("AAPL");
        }
    }

    @Nested
    @DisplayName("findDistinctSymbolsByActiveStatus")
    class FindDistinctSymbolsByActiveStatus {

        @Test
        @DisplayName("should return distinct symbols from active strategies")
        void returnsDistinctSymbolsFromActiveStrategies() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Active TSLA 1", "TSLA", UserStrategyStatus.ACTIVE);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Active AAPL", "AAPL", UserStrategyStatus.ACTIVE);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Paused GOOGL", "GOOGL", UserStrategyStatus.PAUSED);
            entityManager.flush();

            // when
            Set<String> result = repository.findDistinctSymbolsByActiveStatus();

            // then
            assertThat(result).contains("TSLA", "AAPL");
            assertThat(result).doesNotContain("GOOGL");
        }

        @Test
        @DisplayName("should return empty when no active strategies")
        void returnsEmptyWhenNoActiveStrategies() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Paused", "TSLA", UserStrategyStatus.PAUSED);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Stopped", "AAPL", UserStrategyStatus.STOPPED);
            entityManager.flush();

            // when
            Set<String> result = repository.findDistinctSymbolsByActiveStatus();

            // then - may contain symbols from migration data, but not our test symbols
            assertThat(result).doesNotContain("TSLA", "AAPL");
        }
    }

    @Nested
    @DisplayName("findAnyActiveUserId")
    class FindAnyActiveUserId {

        @Test
        @DisplayName("should return user ID when active strategies exist")
        void returnsUserIdWhenActiveStrategiesExist() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Active", "TSLA", UserStrategyStatus.ACTIVE);
            entityManager.flush();

            // when
            Optional<Long> result = repository.findAnyActiveUserId();

            // then
            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findActiveSymbolsWithStrategyIds")
    class FindActiveSymbolsWithStrategyIds {

        @Test
        @DisplayName("should return symbol and strategy ID pairs for active strategies")
        void returnsSymbolAndStrategyIdPairs() {
            // given
            UserStrategyEntity active = createUserStrategy(testUser.getId(), testBaseStrategy, "Active", "NVDA", UserStrategyStatus.ACTIVE);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Paused", "AMD", UserStrategyStatus.PAUSED);
            entityManager.flush();

            // when
            List<Object[]> result = repository.findActiveSymbolsWithStrategyIds();

            // then
            boolean foundNvda = result.stream()
                    .anyMatch(arr -> "NVDA".equals(arr[0]) && active.getId().equals(arr[1]));
            assertThat(foundNvda).isTrue();

            boolean foundAmd = result.stream()
                    .anyMatch(arr -> "AMD".equals(arr[0]));
            assertThat(foundAmd).isFalse();
        }
    }

    @Nested
    @DisplayName("hasActiveOrPreparingStrategies")
    class HasActiveOrPreparingStrategies {

        @Test
        @DisplayName("should return true when user has active strategy")
        void returnsTrueWhenUserHasActiveStrategy() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Active", "TSLA", UserStrategyStatus.ACTIVE);
            entityManager.flush();

            // when
            boolean result = repository.hasActiveOrPreparingStrategies(testUser.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return true when user has preparing strategy")
        void returnsTrueWhenUserHasPreparingStrategy() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Preparing", "TSLA", UserStrategyStatus.PREPARING);
            entityManager.flush();

            // when
            boolean result = repository.hasActiveOrPreparingStrategies(testUser.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when user has only stopped strategies")
        void returnsFalseWhenUserHasOnlyStoppedStrategies() {
            // given
            createUserStrategy(testUser.getId(), testBaseStrategy, "Stopped", "TSLA", UserStrategyStatus.STOPPED);
            createUserStrategy(testUser.getId(), testBaseStrategy, "Paused", "AAPL", UserStrategyStatus.PAUSED);
            entityManager.flush();

            // when
            boolean result = repository.hasActiveOrPreparingStrategies(testUser.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when user has no strategies")
        void returnsFalseWhenUserHasNoStrategies() {
            // when
            boolean result = repository.hasActiveOrPreparingStrategies(testUser.getId());

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist new user strategy")
        void persistsNewUserStrategy() {
            // given
            UserStrategyEntity strategy = new UserStrategyEntity()
                    .setUserId(testUser.getId())
                    .setBaseStrategy(testBaseStrategy)
                    .setName("New Strategy")
                    .setSymbol("META")
                    .setStatus(UserStrategyStatus.CREATED)
                    .setBudget(new BigDecimal("10000.00"));

            // when
            UserStrategyEntity saved = repository.save(strategy);
            entityManager.flush();

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should update existing user strategy")
        void updatesExistingUserStrategy() {
            // given
            UserStrategyEntity strategy = createUserStrategy(testUser.getId(), testBaseStrategy, "Original", "TSLA", UserStrategyStatus.CREATED);
            entityManager.flush();
            Long id = strategy.getId();

            // when
            strategy.setName("Updated Name");
            strategy.setStatus(UserStrategyStatus.ACTIVE);
            strategy.setBudget(new BigDecimal("15000.00"));
            repository.save(strategy);
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<UserStrategyEntity> updated = repository.findById(id);
            assertThat(updated).isPresent();
            assertThat(updated.get().getName()).isEqualTo("Updated Name");
            assertThat(updated.get().getStatus()).isEqualTo(UserStrategyStatus.ACTIVE);
            assertThat(updated.get().getBudget()).isEqualByComparingTo(new BigDecimal("15000.00"));
        }
    }
}
