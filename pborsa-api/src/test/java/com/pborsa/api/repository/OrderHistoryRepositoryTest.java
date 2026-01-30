package com.pborsa.api.repository;

import com.pborsa.api.BaseRepositoryTest;
import com.pborsa.api.config.TestCacheConfig;
import com.pborsa.api.domain.dto.trading.OrderSide;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderStatusReason;
import com.pborsa.api.domain.dto.trading.OrderType;
import com.pborsa.api.domain.dto.trading.TimeInForce;
import com.pborsa.api.domain.dto.user.UserRole;
import com.pborsa.api.domain.dto.user.UserStatus;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.domain.entity.OrderHistoryEntity;
import com.pborsa.api.domain.entity.UserEntity;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link OrderHistoryRepository}.
 * <p>
 * Uses @Transactional for automatic rollback after each test.
 * Test data is created within each test method.
 */
@Import(TestCacheConfig.class)
@DisplayName("OrderHistoryRepository")
class OrderHistoryRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private OrderHistoryRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity testUser;
    private OrderEntity testOrder;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new UserEntity()
                .setFirebaseUid("order-history-repo-test-uid-9120")
                .setEmail("order-history-9120@test.com")
                .setDisplayName("Order History Test User")
                .setProvider("test")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);
        testUser = entityManager.persist(testUser);

        // Create test order
        testOrder = new OrderEntity()
                .setUserId(testUser.getId())
                .setAlpacaOrderId("alpaca-history-9120")
                .setClientOrderId("client-history-9120")
                .setSymbol("TSLA")
                .setSide(OrderSide.BUY)
                .setType(OrderType.MARKET)
                .setTimeInForce(TimeInForce.DAY)
                .setQuantity(new BigDecimal("10"))
                .setStatus(OrderStatus.FILLED);
        testOrder = entityManager.persist(testOrder);

        entityManager.flush();
    }

    private OrderHistoryEntity createOrderHistory(OrderEntity order, Long userId, OrderStatus status, String message) {
        OrderHistoryEntity history = new OrderHistoryEntity()
                .setOrder(order)
                .setUserId(userId)
                .setStatus(status)
                .setMessage(message);
        return entityManager.persist(history);
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return history entry when ID exists")
        void returnsHistoryWhenIdExists() {
            // given
            OrderHistoryEntity history = createOrderHistory(testOrder, testUser.getId(), OrderStatus.NEW, "Order created");
            entityManager.flush();
            UUID id = history.getId();

            // when
            Optional<OrderHistoryEntity> result = repository.findById(id);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(OrderStatus.NEW);
            assertThat(result.get().getMessage()).isEqualTo("Order created");
        }

        @Test
        @DisplayName("should return empty when ID does not exist")
        void returnsEmptyWhenIdNotFound() {
            // when
            Optional<OrderHistoryEntity> result = repository.findById(UUID.randomUUID());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrderIdOrderByCreatedAtAsc")
    class FindByOrderIdOrderByCreatedAtAsc {

        @Test
        @DisplayName("should return history entries in chronological order")
        void returnsHistoryInChronologicalOrder() throws InterruptedException {
            // given - create history entries with slight delay to ensure ordering
            OrderHistoryEntity first = createOrderHistory(testOrder, testUser.getId(), OrderStatus.NEW, "Order created");
            entityManager.flush();

            // Small delay to ensure different createdAt timestamps
            Thread.sleep(10);

            OrderHistoryEntity second = createOrderHistory(testOrder, testUser.getId(), OrderStatus.ACCEPTED, "Order accepted");
            entityManager.flush();

            Thread.sleep(10);

            OrderHistoryEntity third = createOrderHistory(testOrder, testUser.getId(), OrderStatus.FILLED, "Order filled");
            entityManager.flush();

            // when
            List<OrderHistoryEntity> result = repository.findByOrderIdOrderByCreatedAtAsc(testOrder.getId());

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.NEW);
            assertThat(result.get(1).getStatus()).isEqualTo(OrderStatus.ACCEPTED);
            assertThat(result.get(2).getStatus()).isEqualTo(OrderStatus.FILLED);
        }

        @Test
        @DisplayName("should return empty list when order has no history")
        void returnsEmptyListWhenOrderHasNoHistory() {
            // given - create new order without history
            OrderEntity newOrder = new OrderEntity()
                    .setUserId(testUser.getId())
                    .setAlpacaOrderId("alpaca-no-history-9121")
                    .setSymbol("AAPL")
                    .setSide(OrderSide.BUY)
                    .setType(OrderType.MARKET)
                    .setTimeInForce(TimeInForce.DAY)
                    .setQuantity(new BigDecimal("5"))
                    .setStatus(OrderStatus.NEW);
            newOrder = entityManager.persist(newOrder);
            entityManager.flush();

            // when
            List<OrderHistoryEntity> result = repository.findByOrderIdOrderByCreatedAtAsc(newOrder.getId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndOrderId")
    class FindByUserIdAndOrderId {

        @Test
        @DisplayName("should return history for specific user and order")
        void returnsHistoryForUserAndOrder() {
            // given
            createOrderHistory(testOrder, testUser.getId(), OrderStatus.NEW, "Created");
            createOrderHistory(testOrder, testUser.getId(), OrderStatus.FILLED, "Filled");
            entityManager.flush();

            // when
            List<OrderHistoryEntity> result = repository.findByUserIdAndOrderId(testUser.getId(), testOrder.getId());

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(OrderHistoryEntity::getUserId)
                    .containsOnly(testUser.getId());
        }

        @Test
        @DisplayName("should return empty when user doesn't match order")
        void returnsEmptyWhenUserDoesntMatchOrder() {
            // given
            createOrderHistory(testOrder, testUser.getId(), OrderStatus.NEW, "Created");
            entityManager.flush();

            // when - different user ID
            List<OrderHistoryEntity> result = repository.findByUserIdAndOrderId(999999L, testOrder.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when order doesn't exist")
        void returnsEmptyWhenOrderDoesntExist() {
            // when
            List<OrderHistoryEntity> result = repository.findByUserIdAndOrderId(testUser.getId(), UUID.randomUUID());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndCreatedAtAfter")
    class FindByUserIdAndCreatedAtAfter {

        @Test
        @DisplayName("should return history entries created after timestamp")
        void returnsHistoryAfterTimestamp() {
            // given
            Instant baseTime = Instant.now().minus(1, ChronoUnit.HOURS);
            createOrderHistory(testOrder, testUser.getId(), OrderStatus.NEW, "Recent entry");
            entityManager.flush();

            // when
            List<OrderHistoryEntity> result = repository.findByUserIdAndCreatedAtAfter(testUser.getId(), baseTime);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(h -> h.getCreatedAt().isAfter(baseTime));
        }

        @Test
        @DisplayName("should return empty when no entries after timestamp")
        void returnsEmptyWhenNoEntriesAfterTimestamp() {
            // given
            createOrderHistory(testOrder, testUser.getId(), OrderStatus.NEW, "Old entry");
            entityManager.flush();

            // when - future timestamp
            Instant futureTime = Instant.now().plus(1, ChronoUnit.HOURS);
            List<OrderHistoryEntity> result = repository.findByUserIdAndCreatedAtAfter(testUser.getId(), futureTime);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist new history entry with UUID")
        void persistsNewHistoryEntryWithUuid() {
            // given
            OrderHistoryEntity history = new OrderHistoryEntity()
                    .setOrder(testOrder)
                    .setUserId(testUser.getId())
                    .setStatus(OrderStatus.REJECTED)
                    .setReason(OrderStatusReason.INSUFFICIENT_FUNDS)
                    .setMessage("Insufficient buying power");

            // when
            OrderHistoryEntity saved = repository.save(history);
            entityManager.flush();

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getId()).isInstanceOf(UUID.class);
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getReason()).isEqualTo(OrderStatusReason.INSUFFICIENT_FUNDS);
        }

        @Test
        @DisplayName("should persist history with all status reasons")
        void persistsHistoryWithAllStatusReasons() {
            // given & when & then - verify each reason can be persisted
            for (OrderStatusReason reason : OrderStatusReason.values()) {
                OrderHistoryEntity history = new OrderHistoryEntity()
                        .setOrder(testOrder)
                        .setUserId(testUser.getId())
                        .setStatus(OrderStatus.REJECTED)
                        .setReason(reason)
                        .setMessage("Reason: " + reason);

                OrderHistoryEntity saved = repository.save(history);
                entityManager.flush();

                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getReason()).isEqualTo(reason);
            }
        }

        @Test
        @DisplayName("should allow null reason")
        void allowsNullReason() {
            // given
            OrderHistoryEntity history = new OrderHistoryEntity()
                    .setOrder(testOrder)
                    .setUserId(testUser.getId())
                    .setStatus(OrderStatus.FILLED)
                    .setReason(null)
                    .setMessage("Order filled successfully");

            // when
            OrderHistoryEntity saved = repository.save(history);
            entityManager.flush();

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getReason()).isNull();
        }
    }
}
