package com.pborsa.api.repository;

import com.pborsa.api.BaseRepositoryTest;
import com.pborsa.api.config.TestCacheConfig;
import com.pborsa.api.domain.dto.trading.OrderSide;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderType;
import com.pborsa.api.domain.dto.trading.TimeInForce;
import com.pborsa.api.domain.dto.user.UserRole;
import com.pborsa.api.domain.dto.user.UserStatus;
import com.pborsa.api.domain.entity.BaseStrategyEntity;
import com.pborsa.api.domain.entity.OrderEntity;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link OrderRepository}.
 * <p>
 * Uses @Transactional for automatic rollback after each test.
 * Test data is created within each test method.
 */
@Import(TestCacheConfig.class)
@DisplayName("OrderRepository")
class OrderRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity testUser;
    private BaseStrategyEntity testStrategy;
    private UserStrategyEntity testUserStrategy;

    private static final Collection<OrderStatus> TERMINAL_STATUSES = Arrays.asList(
            OrderStatus.FILLED, OrderStatus.CANCELED, OrderStatus.EXPIRED, OrderStatus.REJECTED
    );

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new UserEntity()
                .setFirebaseUid("order-repo-test-uid-9110")
                .setEmail("order-repo-9110@test.com")
                .setDisplayName("Order Repo Test User")
                .setProvider("test")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);
        testUser = entityManager.persist(testUser);

        // Create test base strategy
        testStrategy = new BaseStrategyEntity();
        testStrategy.setCode("ORDER_REPO_TEST_9110");
        testStrategy.setName("Order Repo Test Strategy");
        testStrategy.setDescription("For OrderRepositoryTest");
        testStrategy.setActive(true);
        testStrategy = entityManager.persist(testStrategy);

        // Create test user strategy
        testUserStrategy = new UserStrategyEntity()
                .setUserId(testUser.getId())
                .setBaseStrategy(testStrategy)
                .setName("Order Repo User Strategy")
                .setSymbol("TSLA")
                .setStatus(UserStrategyStatus.ACTIVE)
                .setBudget(new BigDecimal("5000.00"));
        testUserStrategy = entityManager.persist(testUserStrategy);

        entityManager.flush();
    }

    private OrderEntity createOrder(Long userId, UserStrategyEntity userStrategy, String alpacaOrderId,
                                    String symbol, OrderSide side, OrderStatus status) {
        OrderEntity order = new OrderEntity()
                .setUserId(userId)
                .setUserStrategy(userStrategy)
                .setAlpacaOrderId(alpacaOrderId)
                .setClientOrderId("client-" + alpacaOrderId)
                .setSymbol(symbol)
                .setSide(side)
                .setType(OrderType.MARKET)
                .setTimeInForce(TimeInForce.DAY)
                .setQuantity(new BigDecimal("10"))
                .setStatus(status);
        return entityManager.persist(order);
    }

    @Nested
    @DisplayName("findByAlpacaOrderId")
    class FindByAlpacaOrderId {

        @Test
        @DisplayName("should return order when Alpaca order ID exists")
        void returnsOrderWhenAlpacaOrderIdExists() {
            // given
            String alpacaOrderId = "alpaca-9110-001";
            createOrder(testUser.getId(), testUserStrategy, alpacaOrderId, "TSLA", OrderSide.BUY, OrderStatus.FILLED);
            entityManager.flush();

            // when
            Optional<OrderEntity> result = repository.findByAlpacaOrderId(alpacaOrderId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getAlpacaOrderId()).isEqualTo(alpacaOrderId);
            assertThat(result.get().getSymbol()).isEqualTo("TSLA");
        }

        @Test
        @DisplayName("should return empty when Alpaca order ID does not exist")
        void returnsEmptyWhenAlpacaOrderIdNotFound() {
            // when
            Optional<OrderEntity> result = repository.findByAlpacaOrderId("nonexistent-alpaca-id");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByClientOrderId")
    class FindByClientOrderId {

        @Test
        @DisplayName("should return order when client order ID exists")
        void returnsOrderWhenClientOrderIdExists() {
            // given
            OrderEntity order = createOrder(testUser.getId(), testUserStrategy, "alpaca-9111-001", "AAPL", OrderSide.SELL, OrderStatus.NEW);
            entityManager.flush();

            // when
            Optional<OrderEntity> result = repository.findByClientOrderId("client-alpaca-9111-001");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getClientOrderId()).isEqualTo("client-alpaca-9111-001");
        }

        @Test
        @DisplayName("should return empty when client order ID does not exist")
        void returnsEmptyWhenClientOrderIdNotFound() {
            // when
            Optional<OrderEntity> result = repository.findByClientOrderId("nonexistent-client-id");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("should return all orders for user")
        void returnsAllOrdersForUser() {
            // given
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9112-001", "TSLA", OrderSide.BUY, OrderStatus.FILLED);
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9112-002", "AAPL", OrderSide.SELL, OrderStatus.NEW);
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9112-003", "GOOGL", OrderSide.BUY, OrderStatus.CANCELED);
            entityManager.flush();

            // when
            List<OrderEntity> result = repository.findByUserId(testUser.getId());

            // then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(OrderEntity::getAlpacaOrderId)
                    .containsExactlyInAnyOrder("alpaca-9112-001", "alpaca-9112-002", "alpaca-9112-003");
        }

        @Test
        @DisplayName("should return empty list when user has no orders")
        void returnsEmptyListWhenUserHasNoOrders() {
            // when
            List<OrderEntity> result = repository.findByUserId(999999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndUserStrategyId")
    class FindByUserIdAndUserStrategyId {

        @Test
        @DisplayName("should return orders for user and strategy")
        void returnsOrdersForUserAndStrategy() {
            // given
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9113-001", "TSLA", OrderSide.BUY, OrderStatus.FILLED);
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9113-002", "TSLA", OrderSide.SELL, OrderStatus.NEW);
            entityManager.flush();

            // when
            List<OrderEntity> result = repository.findByUserIdAndUserStrategyId(testUser.getId(), testUserStrategy.getId());

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(OrderEntity::getAlpacaOrderId)
                    .containsExactlyInAnyOrder("alpaca-9113-001", "alpaca-9113-002");
        }

        @Test
        @DisplayName("should return empty when no matching orders")
        void returnsEmptyWhenNoMatchingOrders() {
            // when
            List<OrderEntity> result = repository.findByUserIdAndUserStrategyId(testUser.getId(), 999999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByUserIdAndStatusNotIn")
    class CountByUserIdAndStatusNotIn {

        @Test
        @DisplayName("should count non-terminal orders for user")
        void countsNonTerminalOrdersForUser() {
            // given
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9114-001", "TSLA", OrderSide.BUY, OrderStatus.NEW);
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9114-002", "TSLA", OrderSide.BUY, OrderStatus.PARTIALLY_FILLED);
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9114-003", "TSLA", OrderSide.SELL, OrderStatus.FILLED); // terminal
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9114-004", "TSLA", OrderSide.SELL, OrderStatus.CANCELED); // terminal
            entityManager.flush();

            // when
            long count = repository.countByUserIdAndStatusNotIn(testUser.getId(), TERMINAL_STATUSES);

            // then
            assertThat(count).isEqualTo(2); // NEW and PARTIALLY_FILLED
        }

        @Test
        @DisplayName("should return zero when all orders are terminal")
        void returnsZeroWhenAllOrdersAreTerminal() {
            // given
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9115-001", "TSLA", OrderSide.BUY, OrderStatus.FILLED);
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9115-002", "TSLA", OrderSide.SELL, OrderStatus.CANCELED);
            entityManager.flush();

            // when
            long count = repository.countByUserIdAndStatusNotIn(testUser.getId(), TERMINAL_STATUSES);

            // then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("findDistinctUserIdByStatusNotIn")
    class FindDistinctUserIdByStatusNotIn {

        @Test
        @DisplayName("should return distinct user IDs with non-terminal orders")
        void returnsDistinctUserIdsWithNonTerminalOrders() {
            // given - create another user
            UserEntity anotherUser = new UserEntity()
                    .setFirebaseUid("order-repo-test-uid-9116-2")
                    .setEmail("order-repo-9116-2@test.com")
                    .setDisplayName("Another User")
                    .setProvider("test")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);
            anotherUser = entityManager.persist(anotherUser);

            createOrder(testUser.getId(), testUserStrategy, "alpaca-9116-001", "TSLA", OrderSide.BUY, OrderStatus.NEW);
            createOrder(anotherUser.getId(), null, "alpaca-9116-002", "AAPL", OrderSide.BUY, OrderStatus.PARTIALLY_FILLED);
            entityManager.flush();

            // when
            List<Long> result = repository.findDistinctUserIdByStatusNotIn(TERMINAL_STATUSES);

            // then
            assertThat(result).contains(testUser.getId(), anotherUser.getId());
        }

        @Test
        @DisplayName("should not include users with only terminal orders")
        void doesNotIncludeUsersWithOnlyTerminalOrders() {
            // given
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9117-001", "TSLA", OrderSide.BUY, OrderStatus.FILLED);
            entityManager.flush();

            // when
            List<Long> result = repository.findDistinctUserIdByStatusNotIn(TERMINAL_STATUSES);

            // then
            assertThat(result).doesNotContain(testUser.getId());
        }
    }

    @Nested
    @DisplayName("findByUpdatedAtBeforeAndStatusNotIn")
    class FindByUpdatedAtBeforeAndStatusNotIn {

        @Test
        @DisplayName("should find stale non-terminal orders")
        void findsStaleNonTerminalOrders() {
            // given
            OrderEntity staleOrder = createOrder(testUser.getId(), testUserStrategy, "alpaca-9118-001", "TSLA", OrderSide.BUY, OrderStatus.NEW);
            entityManager.flush();
            entityManager.clear();

            // when - find orders updated before now + 1 hour (simulating stale check)
            Instant cutoff = Instant.now().plus(1, ChronoUnit.HOURS);
            List<OrderEntity> result = repository.findByUpdatedAtBeforeAndStatusNotIn(cutoff, TERMINAL_STATUSES);

            // then
            assertThat(result).extracting(OrderEntity::getAlpacaOrderId)
                    .contains("alpaca-9118-001");
        }

        @Test
        @DisplayName("should not include terminal orders")
        void doesNotIncludeTerminalOrders() {
            // given
            createOrder(testUser.getId(), testUserStrategy, "alpaca-9119-001", "TSLA", OrderSide.BUY, OrderStatus.FILLED);
            entityManager.flush();

            // when
            Instant cutoff = Instant.now().plus(1, ChronoUnit.HOURS);
            List<OrderEntity> result = repository.findByUpdatedAtBeforeAndStatusNotIn(cutoff, TERMINAL_STATUSES);

            // then
            assertThat(result).extracting(OrderEntity::getAlpacaOrderId)
                    .doesNotContain("alpaca-9119-001");
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist new order with UUID")
        void persistsNewOrderWithUuid() {
            // given
            OrderEntity order = new OrderEntity()
                    .setUserId(testUser.getId())
                    .setUserStrategy(testUserStrategy)
                    .setAlpacaOrderId("alpaca-save-9110")
                    .setClientOrderId("client-save-9110")
                    .setSymbol("NVDA")
                    .setSide(OrderSide.BUY)
                    .setType(OrderType.LIMIT)
                    .setTimeInForce(TimeInForce.GTC)
                    .setQuantity(new BigDecimal("50"))
                    .setLimitPrice(new BigDecimal("450.00"))
                    .setStatus(OrderStatus.NEW);

            // when
            OrderEntity saved = repository.save(order);
            entityManager.flush();

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getId()).isInstanceOf(UUID.class);
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should update existing order status")
        void updatesExistingOrderStatus() {
            // given
            OrderEntity order = createOrder(testUser.getId(), testUserStrategy, "alpaca-update-9110", "TSLA", OrderSide.BUY, OrderStatus.NEW);
            entityManager.flush();
            UUID id = order.getId();

            // when
            order.setStatus(OrderStatus.FILLED);
            order.setFilledQuantity(new BigDecimal("10"));
            order.setFilledAvgPrice(new BigDecimal("250.50"));
            order.setFilledAt(Instant.now());
            repository.save(order);
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<OrderEntity> updated = repository.findById(id);
            assertThat(updated).isPresent();
            assertThat(updated.get().getStatus()).isEqualTo(OrderStatus.FILLED);
            assertThat(updated.get().getFilledQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        }
    }
}
