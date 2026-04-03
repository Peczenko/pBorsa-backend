package com.pborsa.api.service.trading;

import com.pborsa.api.order.entity.OrderEntity;
import com.pborsa.api.order.mapper.OrderDetailMapper;
import com.pborsa.api.order.repository.OrderRepository;
import com.pborsa.api.order.service.OrderQueryService;
import com.pborsa.domain.dto.strategy.OrderDetailDto;
import com.pborsa.domain.dto.trading.OrderSide;
import com.pborsa.domain.dto.trading.OrderStatus;
import com.pborsa.domain.dto.trading.OrderType;
import com.pborsa.domain.dto.trading.TimeInForce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OrderQueryService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderQueryService")
class OrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailMapper orderDetailMapper;

    @InjectMocks
    private OrderQueryService orderQueryService;

    private OrderEntity testOrder;
    private OrderDetailDto testOrderDto;
    private UUID testOrderId;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();
        Instant now = Instant.now();

        testOrder = new OrderEntity()
                .setId(testOrderId)
                .setUserId(100L)
                .setAlpacaOrderId("alpaca-123")
                .setClientOrderId("client-123")
                .setSymbol("TSLA")
                .setSide(OrderSide.BUY)
                .setType(OrderType.MARKET)
                .setTimeInForce(TimeInForce.DAY)
                .setQuantity(new BigDecimal("10"))
                .setFilledQuantity(new BigDecimal("10"))
                .setFilledAvgPrice(new BigDecimal("250.00"))
                .setStatus(OrderStatus.FILLED);

        testOrderDto = new OrderDetailDto(
                testOrderId,
                "alpaca-123",
                "client-123",
                "TSLA",
                new BigDecimal("10"),
                new BigDecimal("10"),
                OrderSide.BUY,
                OrderType.MARKET,
                TimeInForce.DAY,
                null,
                null,
                new BigDecimal("250.00"),
                OrderStatus.FILLED,
                null,
                false,
                now,
                now,
                now,
                now,
                null,
                null,
                "us_equity",
                100L
        );
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return order when found")
        void returnsOrderWhenFound() {
            // given
            when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

            // when
            Optional<OrderEntity> result = orderQueryService.findById(testOrderId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getAlpacaOrderId()).isEqualTo("alpaca-123");
        }

        @Test
        @DisplayName("should return empty when not found")
        void returnsEmptyWhenNotFound() {
            // given
            when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

            // when
            Optional<OrderEntity> result = orderQueryService.findById(testOrderId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByExternalIds")
    class FindByExternalIds {

        @Test
        @DisplayName("should find by Alpaca order ID first")
        void findsByAlpacaOrderIdFirst() {
            // given
            when(orderRepository.findByAlpacaOrderId("alpaca-123"))
                    .thenReturn(Optional.of(testOrder));

            // when
            Optional<OrderEntity> result = orderQueryService.findByExternalIds("alpaca-123", "client-123");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getAlpacaOrderId()).isEqualTo("alpaca-123");
        }

        @Test
        @DisplayName("should fall back to client order ID")
        void fallsBackToClientOrderId() {
            // given
            when(orderRepository.findByAlpacaOrderId("alpaca-123"))
                    .thenReturn(Optional.empty());
            when(orderRepository.findByClientOrderId("client-123"))
                    .thenReturn(Optional.of(testOrder));

            // when
            Optional<OrderEntity> result = orderQueryService.findByExternalIds("alpaca-123", "client-123");

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("should return empty when neither ID matches")
        void returnsEmptyWhenNeitherIdMatches() {
            // given
            when(orderRepository.findByAlpacaOrderId("alpaca-123"))
                    .thenReturn(Optional.empty());
            when(orderRepository.findByClientOrderId("client-123"))
                    .thenReturn(Optional.empty());

            // when
            Optional<OrderEntity> result = orderQueryService.findByExternalIds("alpaca-123", "client-123");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should handle null Alpaca order ID")
        void handlesNullAlpacaOrderId() {
            // given
            when(orderRepository.findByClientOrderId("client-123"))
                    .thenReturn(Optional.of(testOrder));

            // when
            Optional<OrderEntity> result = orderQueryService.findByExternalIds(null, "client-123");

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("should handle blank Alpaca order ID")
        void handlesBlankAlpacaOrderId() {
            // given
            when(orderRepository.findByClientOrderId("client-123"))
                    .thenReturn(Optional.of(testOrder));

            // when
            Optional<OrderEntity> result = orderQueryService.findByExternalIds("   ", "client-123");

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("should return empty when both IDs are null")
        void returnsEmptyWhenBothIdsAreNull() {
            // when
            Optional<OrderEntity> result = orderQueryService.findByExternalIds(null, null);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasOpenOrders")
    class HasOpenOrders {

        @Test
        @DisplayName("should return true when user has open orders")
        void returnsTrueWhenUserHasOpenOrders() {
            // given
            when(orderRepository.countByUserIdAndStatusNotIn(eq(100L), any()))
                    .thenReturn(3L);

            // when
            boolean result = orderQueryService.hasOpenOrders(100L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when user has no open orders")
        void returnsFalseWhenUserHasNoOpenOrders() {
            // given
            when(orderRepository.countByUserIdAndStatusNotIn(eq(100L), any()))
                    .thenReturn(0L);

            // when
            boolean result = orderQueryService.hasOpenOrders(100L);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getOrdersByUserStrategyId")
    class GetOrdersByUserStrategyId {

        @Test
        @DisplayName("should return orders for user strategy")
        void returnsOrdersForUserStrategy() {
            // given
            when(orderRepository.findByUserIdAndUserStrategyId(100L, 1L))
                    .thenReturn(List.of(testOrder));
            when(orderDetailMapper.toOrderDetailDtoList(List.of(testOrder)))
                    .thenReturn(List.of(testOrderDto));

            // when
            List<OrderDetailDto> result = orderQueryService.getOrdersByUserStrategyId(100L, 1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).symbol()).isEqualTo("TSLA");
        }

        @Test
        @DisplayName("should return empty list when no orders")
        void returnsEmptyListWhenNoOrders() {
            // given
            when(orderRepository.findByUserIdAndUserStrategyId(100L, 1L))
                    .thenReturn(List.of());
            when(orderDetailMapper.toOrderDetailDtoList(List.of()))
                    .thenReturn(List.of());

            // when
            List<OrderDetailDto> result = orderQueryService.getOrdersByUserStrategyId(100L, 1L);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when user ID is null")
        void throwsWhenUserIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderQueryService.getOrdersByUserStrategyId(null, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID is required");
        }

        @Test
        @DisplayName("should throw when user strategy ID is null")
        void throwsWhenUserStrategyIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderQueryService.getOrdersByUserStrategyId(100L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User strategy ID is required");
        }
    }

    @Nested
    @DisplayName("getOrderDetail")
    class GetOrderDetail {

        @Test
        @DisplayName("should return order detail when found and belongs to user")
        void returnsOrderDetailWhenFoundAndBelongsToUser() {
            // given
            when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
            when(orderDetailMapper.toOrderDetailDto(testOrder)).thenReturn(testOrderDto);

            // when
            Optional<OrderDetailDto> result = orderQueryService.getOrderDetail(100L, testOrderId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().symbol()).isEqualTo("TSLA");
        }

        @Test
        @DisplayName("should return empty when order not found")
        void returnsEmptyWhenOrderNotFound() {
            // given
            when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

            // when
            Optional<OrderDetailDto> result = orderQueryService.getOrderDetail(100L, testOrderId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when order belongs to different user")
        void returnsEmptyWhenOrderBelongsToDifferentUser() {
            // given
            when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

            // when - different user ID
            Optional<OrderDetailDto> result = orderQueryService.getOrderDetail(200L, testOrderId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when user ID is null")
        void throwsWhenUserIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderQueryService.getOrderDetail(null, testOrderId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID is required");
        }

        @Test
        @DisplayName("should throw when order ID is null")
        void throwsWhenOrderIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderQueryService.getOrderDetail(100L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Order ID is required");
        }
    }

    @Nested
    @DisplayName("getOrderDetailAdmin")
    class GetOrderDetailAdmin {

        @Test
        @DisplayName("should return order detail without user validation")
        void returnsOrderDetailWithoutUserValidation() {
            // given
            when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
            when(orderDetailMapper.toOrderDetailDto(testOrder)).thenReturn(testOrderDto);

            // when
            Optional<OrderDetailDto> result = orderQueryService.getOrderDetailAdmin(testOrderId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().symbol()).isEqualTo("TSLA");
        }

        @Test
        @DisplayName("should return empty when order not found")
        void returnsEmptyWhenOrderNotFound() {
            // given
            when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

            // when
            Optional<OrderDetailDto> result = orderQueryService.getOrderDetailAdmin(testOrderId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when order ID is null")
        void throwsWhenOrderIdIsNull() {
            // when/then
            assertThatThrownBy(() -> orderQueryService.getOrderDetailAdmin(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Order ID is required");
        }
    }
}
