package com.pborsa.api.controller;

import com.pborsa.api.config.TestSecurityConfig;
import com.pborsa.api.order.controller.OrderController;
import com.pborsa.domain.dto.strategy.OrderDetailDto;
import com.pborsa.domain.dto.trading.OrderSide;
import com.pborsa.domain.dto.trading.OrderStatus;
import com.pborsa.domain.dto.trading.OrderType;
import com.pborsa.domain.dto.trading.TimeInForce;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import com.pborsa.api.security.WithMockFirebaseUser;
import com.pborsa.api.shared.security.SecurityService;
import com.pborsa.api.order.service.OrderQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link OrderController}.
 * <p>
 * Tests controller behavior with mocked service dependencies.
 */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("OrderController")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private SecurityService securityService;

    private OrderDetailDto createOrderDetail(UUID id, String symbol, OrderSide side, OrderStatus status) {
        Instant now = Instant.now();
        return new OrderDetailDto(
                id,
                "alpaca-" + id,         // orderId (Alpaca order ID)
                "client-" + id,          // clientOrderId
                symbol,
                new BigDecimal("10"),    // quantity
                new BigDecimal("10"),    // filledQuantity
                side,
                OrderType.MARKET,
                TimeInForce.DAY,
                null,                    // limitPrice
                null,                    // stopPrice
                new BigDecimal("250.50"), // filledAveragePrice
                status,
                null,                    // message
                false,                   // extendedHours
                now,                     // createdAt
                now,                     // updatedAt
                now,                     // submittedAt
                status == OrderStatus.FILLED ? now : null, // filledAt
                null,                    // expiredAt
                null,                    // cancelledAt
                "us_equity",             // assetClass
                9210L                    // userStrategyId
        );
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{userId}/strategy/{userStrategyId}")
    class GetOrdersByUserStrategy {

        @Test
        @DisplayName("should return orders for user strategy")
        @WithMockFirebaseUser(uid = "order-test-uid-9210", email = "order9210@test.com")
        void returnsOrdersForUserStrategy() throws Exception {
            // given
            Long userId = 9210L;
            Long userStrategyId = 9210L;
            UUID orderId = UUID.randomUUID();

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(orderQueryService.getOrdersByUserStrategyId(userId, userStrategyId))
                    .thenReturn(List.of(
                            createOrderDetail(orderId, "TSLA", OrderSide.BUY, OrderStatus.FILLED)
                    ));

            // when/then
            mockMvc.perform(get("/api/v1/orders/{userId}/strategy/{userStrategyId}", userId, userStrategyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].symbol", is("TSLA")))
                    .andExpect(jsonPath("$.data[0].side", is("BUY")))
                    .andExpect(jsonPath("$.data[0].status", is("FILLED")));
        }

        @Test
        @DisplayName("should return empty list when no orders")
        @WithMockFirebaseUser(uid = "order-test-uid-9211", email = "order9211@test.com")
        void returnsEmptyListWhenNoOrders() throws Exception {
            // given
            Long userId = 9211L;
            Long userStrategyId = 9211L;

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(orderQueryService.getOrdersByUserStrategyId(userId, userStrategyId))
                    .thenReturn(List.of());

            // when/then
            mockMvc.perform(get("/api/v1/orders/{userId}/strategy/{userStrategyId}", userId, userStrategyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("should return multiple orders")
        @WithMockFirebaseUser(uid = "order-test-uid-9212", email = "order9212@test.com")
        void returnsMultipleOrders() throws Exception {
            // given
            Long userId = 9212L;
            Long userStrategyId = 9212L;

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(orderQueryService.getOrdersByUserStrategyId(userId, userStrategyId))
                    .thenReturn(List.of(
                            createOrderDetail(UUID.randomUUID(), "TSLA", OrderSide.BUY, OrderStatus.FILLED),
                            createOrderDetail(UUID.randomUUID(), "TSLA", OrderSide.SELL, OrderStatus.NEW)
                    ));

            // when/then
            mockMvc.perform(get("/api/v1/orders/{userId}/strategy/{userStrategyId}", userId, userStrategyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{userId}/{orderId}")
    class GetOrderDetail {

        @Test
        @DisplayName("should return order detail when found")
        @WithMockFirebaseUser(uid = "order-test-uid-9213", email = "order9213@test.com")
        void returnsOrderDetailWhenFound() throws Exception {
            // given
            Long userId = 9213L;
            UUID orderId = UUID.randomUUID();

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(orderQueryService.getOrderDetail(userId, orderId))
                    .thenReturn(Optional.of(createOrderDetail(orderId, "AAPL", OrderSide.BUY, OrderStatus.FILLED)));

            // when/then
            mockMvc.perform(get("/api/v1/orders/{userId}/{orderId}", userId, orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.symbol", is("AAPL")));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        @WithMockFirebaseUser(uid = "order-test-uid-9214", email = "order9214@test.com")
        void returns404WhenOrderNotFound() throws Exception {
            // given
            Long userId = 9214L;
            UUID orderId = UUID.randomUUID();

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(orderQueryService.getOrderDetail(userId, orderId))
                    .thenReturn(Optional.empty());

            // when/then
            mockMvc.perform(get("/api/v1/orders/{userId}/{orderId}", userId, orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", is("Order not found")));
        }
    }
}
