package com.pborsa.api.repository;

import com.pborsa.api.BaseRepositoryTest;
import com.pborsa.api.config.TestCacheConfig;
import com.pborsa.api.domain.dto.user.UserRole;
import com.pborsa.api.domain.dto.user.UserStatus;
import com.pborsa.api.domain.entity.BaseStrategyEntity;
import com.pborsa.api.domain.entity.StrategyPositionEntity;
import com.pborsa.api.domain.entity.UserEntity;
import com.pborsa.api.domain.entity.UserStrategyEntity;
import com.pborsa.api.domain.entity.UserStrategyStatus;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link StrategyPositionRepository}.
 * <p>
 * Uses @Transactional for automatic rollback after each test.
 * Test data is created within each test method.
 */
@Import(TestCacheConfig.class)
@DisplayName("StrategyPositionRepository")
class StrategyPositionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private StrategyPositionRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity testUser;
    private BaseStrategyEntity testBaseStrategy;
    private UserStrategyEntity testUserStrategy;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new UserEntity()
                .setFirebaseUid("position-repo-test-uid-9140")
                .setEmail("position-9140@test.com")
                .setDisplayName("Position Repo Test User")
                .setProvider("test")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);
        testUser = entityManager.persist(testUser);

        // Create test base strategy
        testBaseStrategy = new BaseStrategyEntity();
        testBaseStrategy.setCode("POSITION_REPO_TEST_9140");
        testBaseStrategy.setName("Position Repo Test Strategy");
        testBaseStrategy.setDescription("For StrategyPositionRepositoryTest");
        testBaseStrategy.setActive(true);
        testBaseStrategy = entityManager.persist(testBaseStrategy);

        // Create test user strategy
        testUserStrategy = new UserStrategyEntity()
                .setUserId(testUser.getId())
                .setBaseStrategy(testBaseStrategy)
                .setName("Position Test User Strategy")
                .setSymbol("TSLA")
                .setStatus(UserStrategyStatus.ACTIVE)
                .setBudget(new BigDecimal("5000.00"));
        testUserStrategy = entityManager.persist(testUserStrategy);

        entityManager.flush();
    }

    private StrategyPositionEntity createPosition(UserStrategyEntity userStrategy, BigDecimal shares,
                                                   BigDecimal costBasis, BigDecimal realizedPnL) {
        StrategyPositionEntity position = new StrategyPositionEntity()
                .setUserStrategy(userStrategy)
                .setTotalShares(shares)
                .setTotalCostBasis(costBasis)
                .setRealizedPnL(realizedPnL);
        return entityManager.persist(position);
    }

    @Nested
    @DisplayName("findByUserStrategy_UserId")
    class FindByUserStrategyUserId {

        @Test
        @DisplayName("should return all positions for user")
        void returnsAllPositionsForUser() {
            // given
            createPosition(testUserStrategy, new BigDecimal("100"), new BigDecimal("25000.00"), BigDecimal.ZERO);

            // Create another user strategy for same user
            UserStrategyEntity anotherStrategy = new UserStrategyEntity()
                    .setUserId(testUser.getId())
                    .setBaseStrategy(testBaseStrategy)
                    .setName("Another Strategy")
                    .setSymbol("AAPL")
                    .setStatus(UserStrategyStatus.ACTIVE)
                    .setBudget(new BigDecimal("3000.00"));
            anotherStrategy = entityManager.persist(anotherStrategy);

            createPosition(anotherStrategy, new BigDecimal("50"), new BigDecimal("8500.00"), new BigDecimal("500.00"));
            entityManager.flush();

            // when
            List<StrategyPositionEntity> result = repository.findByUserStrategy_UserId(testUser.getId());

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(p -> p.getUserStrategy().getSymbol())
                    .containsExactlyInAnyOrder("TSLA", "AAPL");
        }

        @Test
        @DisplayName("should return empty list when user has no positions")
        void returnsEmptyListWhenUserHasNoPositions() {
            // when
            List<StrategyPositionEntity> result = repository.findByUserStrategy_UserId(999999L);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should not return positions for other users")
        void doesNotReturnPositionsForOtherUsers() {
            // given
            createPosition(testUserStrategy, new BigDecimal("100"), new BigDecimal("25000.00"), BigDecimal.ZERO);

            // Create another user with their own strategy and position
            UserEntity anotherUser = new UserEntity()
                    .setFirebaseUid("another-user-9141")
                    .setEmail("another-9141@test.com")
                    .setDisplayName("Another User")
                    .setProvider("test")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);
            anotherUser = entityManager.persist(anotherUser);

            UserStrategyEntity otherStrategy = new UserStrategyEntity()
                    .setUserId(anotherUser.getId())
                    .setBaseStrategy(testBaseStrategy)
                    .setName("Other User Strategy")
                    .setSymbol("GOOGL")
                    .setStatus(UserStrategyStatus.ACTIVE)
                    .setBudget(new BigDecimal("2000.00"));
            otherStrategy = entityManager.persist(otherStrategy);

            createPosition(otherStrategy, new BigDecimal("20"), new BigDecimal("2800.00"), BigDecimal.ZERO);
            entityManager.flush();

            // when
            List<StrategyPositionEntity> result = repository.findByUserStrategy_UserId(testUser.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserStrategy().getSymbol()).isEqualTo("TSLA");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return position by strategy ID")
        void returnsPositionByStrategyId() {
            // given
            StrategyPositionEntity position = createPosition(testUserStrategy, new BigDecimal("100"), new BigDecimal("25000.00"), BigDecimal.ZERO);
            entityManager.flush();

            // when - position ID is same as userStrategyId due to @MapsId
            Optional<StrategyPositionEntity> result = repository.findById(testUserStrategy.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getTotalShares()).isEqualByComparingTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("should return empty when position doesn't exist")
        void returnsEmptyWhenPositionDoesntExist() {
            // when
            Optional<StrategyPositionEntity> result = repository.findById(999999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist new position")
        void persistsNewPosition() {
            // given
            StrategyPositionEntity position = new StrategyPositionEntity()
                    .setUserStrategy(testUserStrategy)
                    .setTotalShares(new BigDecimal("50"))
                    .setTotalCostBasis(new BigDecimal("12500.00"))
                    .setRealizedPnL(BigDecimal.ZERO);

            // when
            StrategyPositionEntity saved = repository.save(position);
            entityManager.flush();

            // then
            assertThat(saved.getUserStrategyId()).isEqualTo(testUserStrategy.getId());
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should update existing position")
        void updatesExistingPosition() {
            // given
            StrategyPositionEntity position = createPosition(testUserStrategy, new BigDecimal("100"), new BigDecimal("25000.00"), BigDecimal.ZERO);
            entityManager.flush();

            // when - apply a buy
            position.applyBuy(new BigDecimal("50"), new BigDecimal("260.00"));
            repository.save(position);
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<StrategyPositionEntity> updated = repository.findById(testUserStrategy.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getTotalShares()).isEqualByComparingTo(new BigDecimal("150"));
            assertThat(updated.get().getTotalCostBasis()).isEqualByComparingTo(new BigDecimal("38000.00")); // 25000 + 13000
        }
    }

    @Nested
    @DisplayName("Position P/L Calculations")
    class PositionPnLCalculations {

        @Test
        @DisplayName("should correctly calculate average cost per share")
        void calculatesAverageCostPerShare() {
            // given
            StrategyPositionEntity position = createPosition(testUserStrategy, new BigDecimal("100"), new BigDecimal("25000.00"), BigDecimal.ZERO);
            entityManager.flush();

            // when
            BigDecimal avgCost = position.getAverageCostPerShare();

            // then
            assertThat(avgCost).isEqualByComparingTo(new BigDecimal("250.0000"));
        }

        @Test
        @DisplayName("should correctly apply buy order")
        void appliesBuyOrder() {
            // given
            StrategyPositionEntity position = createPosition(testUserStrategy, new BigDecimal("100"), new BigDecimal("25000.00"), BigDecimal.ZERO);
            entityManager.flush();

            // when
            position.applyBuy(new BigDecimal("50"), new BigDecimal("260.00"));

            // then
            assertThat(position.getTotalShares()).isEqualByComparingTo(new BigDecimal("150"));
            assertThat(position.getTotalCostBasis()).isEqualByComparingTo(new BigDecimal("38000.00"));
        }

        @Test
        @DisplayName("should correctly apply sell order and record realized P/L")
        void appliesSellOrderWithRealizedPnL() {
            // given - buy at $250/share
            StrategyPositionEntity position = createPosition(testUserStrategy, new BigDecimal("100"), new BigDecimal("25000.00"), BigDecimal.ZERO);
            entityManager.flush();

            // when - sell 50 shares at $300/share (profit of $50/share = $2500 total)
            BigDecimal realizedPnL = position.applySell(new BigDecimal("50"), new BigDecimal("300.00"));

            // then
            assertThat(realizedPnL).isEqualByComparingTo(new BigDecimal("2500.00"));
            assertThat(position.getTotalShares()).isEqualByComparingTo(new BigDecimal("50"));
            assertThat(position.getRealizedPnL()).isEqualByComparingTo(new BigDecimal("2500.00"));
        }

        @Test
        @DisplayName("should calculate unrealized P/L correctly")
        void calculatesUnrealizedPnL() {
            // given - buy at $250/share
            StrategyPositionEntity position = createPosition(testUserStrategy, new BigDecimal("100"), new BigDecimal("25000.00"), BigDecimal.ZERO);
            entityManager.flush();

            // when - current price is $280
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(new BigDecimal("280.00"));

            // then - $30 gain per share * 100 shares = $3000
            assertThat(unrealizedPnL).isEqualByComparingTo(new BigDecimal("3000.00"));
        }

        @Test
        @DisplayName("should return zero for unrealized P/L when no shares held")
        void returnsZeroUnrealizedPnLWhenNoShares() {
            // given
            StrategyPositionEntity position = createPosition(testUserStrategy, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            entityManager.flush();

            // when
            BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(new BigDecimal("100.00"));

            // then
            assertThat(unrealizedPnL).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
