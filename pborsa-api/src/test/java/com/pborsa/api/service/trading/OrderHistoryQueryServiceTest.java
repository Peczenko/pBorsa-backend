package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.strategy.OrderHistoryDto;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderStatusReason;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.domain.entity.OrderHistoryEntity;
import com.pborsa.api.service.mapper.OrderHistoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OrderHistoryQueryService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderHistoryQueryService")
class OrderHistoryQueryServiceTest {

    @Mock
    private OrderHistoryPersistenceService orderHistoryPersistenceService;

    @Mock
    private OrderQueryService orderQueryService;

    @Mock
    private OrderHistoryMapper orderHistoryMapper;

    @InjectMocks
    private OrderHistoryQueryService orderHistoryQueryService;

    private UUID testOrderId;
    private UUID testHistoryId;
    private OrderEntity testOrder;
    private OrderHistoryEntity testHistoryEntity;
    private OrderHistoryDto testHistoryDto;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();
        testHistoryId = UUID.randomUUID();
        Instant now = Instant.now();

        testOrder = new OrderEntity()
                .setId(testOrderId)
                .setUserId(100L);

        testHistoryEntity = new OrderHistoryEntity()
                .setId(testHistoryId)
                .setOrder(testOrder)
                .setUserId(100L)
                .setStatus(OrderStatus.FILLED)
                .setMessage("Order filled");

        testHistoryDto = new OrderHistoryDto(
                testHistoryId,
                testOrderId,
                OrderStatus.FILLED,
                null,
                "Order filled",
                now
        );
    }

    @Nested
    @DisplayName("getOrderHistory")
    class GetOrderHistory {

        @Test
        @DisplayName("should return history when order belongs to user")
        void returnsHistoryWhenOrderBelongsToUser() {
            // given
            when(orderQueryService.findById(testOrderId)).thenReturn(Optional.of(testOrder));
            when(orderHistoryPersistenceService.findHistoryByUserIdAndOrderId(100L, testOrderId))
                    .thenReturn(List.of(testHistoryEntity));
            when(orderHistoryMapper.toOrderHistoryDtoList(List.of(testHistoryEntity)))
                    .thenReturn(List.of(testHistoryDto));

            // when
            List<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistory(100L, testOrderId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(OrderStatus.FILLED);
        }

        @Test
        @DisplayName("should return empty list when order not found")
        void returnsEmptyListWhenOrderNotFound() {
            // given
            when(orderQueryService.findById(testOrderId)).thenReturn(Optional.empty());

            // when
            List<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistory(100L, testOrderId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when order belongs to different user")
        void returnsEmptyListWhenOrderBelongsToDifferentUser() {
            // given
            when(orderQueryService.findById(testOrderId)).thenReturn(Optional.of(testOrder));

            // when - different user ID
            List<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistory(200L, testOrderId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when user ID is null")
        void throwsWhenUserIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderHistoryQueryService.getOrderHistory(null, testOrderId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID is required");
        }

        @Test
        @DisplayName("should throw when order ID is null")
        void throwsWhenOrderIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderHistoryQueryService.getOrderHistory(100L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Order ID is required");
        }
    }

    @Nested
    @DisplayName("getOrderHistoryEntry")
    class GetOrderHistoryEntry {

        @Test
        @DisplayName("should return history entry when belongs to user")
        void returnsHistoryEntryWhenBelongsToUser() {
            // given
            when(orderHistoryPersistenceService.findHistoryById(testHistoryId))
                    .thenReturn(Optional.of(testHistoryEntity));
            when(orderHistoryMapper.toOrderHistoryDto(testHistoryEntity))
                    .thenReturn(testHistoryDto);

            // when
            Optional<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistoryEntry(100L, testHistoryId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().status()).isEqualTo(OrderStatus.FILLED);
        }

        @Test
        @DisplayName("should return empty when history not found")
        void returnsEmptyWhenHistoryNotFound() {
            // given
            when(orderHistoryPersistenceService.findHistoryById(testHistoryId))
                    .thenReturn(Optional.empty());

            // when
            Optional<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistoryEntry(100L, testHistoryId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when history belongs to different user")
        void returnsEmptyWhenHistoryBelongsToDifferentUser() {
            // given
            when(orderHistoryPersistenceService.findHistoryById(testHistoryId))
                    .thenReturn(Optional.of(testHistoryEntity));

            // when - different user ID
            Optional<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistoryEntry(200L, testHistoryId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when user ID is null")
        void throwsWhenUserIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderHistoryQueryService.getOrderHistoryEntry(null, testHistoryId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID is required");
        }

        @Test
        @DisplayName("should throw when history ID is null")
        void throwsWhenHistoryIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderHistoryQueryService.getOrderHistoryEntry(100L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("History ID is required");
        }
    }

    @Nested
    @DisplayName("getOrderHistoryAdmin")
    class GetOrderHistoryAdmin {

        @Test
        @DisplayName("should return history without user validation")
        void returnsHistoryWithoutUserValidation() {
            // given
            when(orderHistoryPersistenceService.findHistoryByOrderId(testOrderId))
                    .thenReturn(List.of(testHistoryEntity));
            when(orderHistoryMapper.toOrderHistoryDtoList(List.of(testHistoryEntity)))
                    .thenReturn(List.of(testHistoryDto));

            // when
            List<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistoryAdmin(testOrderId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(OrderStatus.FILLED);
        }

        @Test
        @DisplayName("should return empty list when no history")
        void returnsEmptyListWhenNoHistory() {
            // given
            when(orderHistoryPersistenceService.findHistoryByOrderId(testOrderId))
                    .thenReturn(List.of());
            when(orderHistoryMapper.toOrderHistoryDtoList(List.of()))
                    .thenReturn(List.of());

            // when
            List<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistoryAdmin(testOrderId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when order ID is null")
        void throwsWhenOrderIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderHistoryQueryService.getOrderHistoryAdmin(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Order ID is required");
        }
    }

    @Nested
    @DisplayName("getOrderHistoryEntryAdmin")
    class GetOrderHistoryEntryAdmin {

        @Test
        @DisplayName("should return history entry without user validation")
        void returnsHistoryEntryWithoutUserValidation() {
            // given
            when(orderHistoryPersistenceService.findHistoryById(testHistoryId))
                    .thenReturn(Optional.of(testHistoryEntity));
            when(orderHistoryMapper.toOrderHistoryDto(testHistoryEntity))
                    .thenReturn(testHistoryDto);

            // when
            Optional<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistoryEntryAdmin(testHistoryId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().status()).isEqualTo(OrderStatus.FILLED);
        }

        @Test
        @DisplayName("should return empty when history not found")
        void returnsEmptyWhenHistoryNotFound() {
            // given
            when(orderHistoryPersistenceService.findHistoryById(testHistoryId))
                    .thenReturn(Optional.empty());

            // when
            Optional<OrderHistoryDto> result = orderHistoryQueryService.getOrderHistoryEntryAdmin(testHistoryId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when history ID is null")
        void throwsWhenHistoryIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderHistoryQueryService.getOrderHistoryEntryAdmin(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("History ID is required");
        }
    }
}
