package com.pborsa.api.controller;

import com.pborsa.api.config.TestSecurityConfig;
import com.pborsa.api.order.controller.OrderHistoryController;
import com.pborsa.domain.dto.strategy.OrderHistoryDto;
import com.pborsa.domain.dto.trading.OrderStatus;
import com.pborsa.domain.dto.trading.OrderStatusReason;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import com.pborsa.api.security.WithMockFirebaseUser;
import com.pborsa.api.shared.security.SecurityService;
import com.pborsa.api.order.service.OrderHistoryQueryService;
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
 * MockMvc tests for {@link OrderHistoryController}.
 * <p>
 * Tests controller behavior with mocked service dependencies.
 */
@WebMvcTest(OrderHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("OrderHistoryController")
class OrderHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderHistoryQueryService orderHistoryQueryService;

    @MockitoBean
    private SecurityService securityService;

    private OrderHistoryDto createOrderHistoryDto(UUID id, UUID orderId, OrderStatus status, String message) {
        return new OrderHistoryDto(
                id,
                orderId,
                status,
                OrderStatusReason.UNKNOWN,
                message,
                Instant.now()
        );
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{userId}/{orderId}/history")
    class GetOrderHistory {

        @Test
        @DisplayName("should return order history")
        @WithMockFirebaseUser(uid = "history-test-uid-9220", email = "history9220@test.com")
        void returnsOrderHistory() throws Exception {
            // given
            Long userId = 9220L;
            UUID orderId = UUID.randomUUID();
            UUID historyId1 = UUID.randomUUID();
            UUID historyId2 = UUID.randomUUID();

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(orderHistoryQueryService.getOrderHistory(userId, orderId))
                    .thenReturn(List.of(
                            createOrderHistoryDto(historyId1, orderId, OrderStatus.NEW, "Order created"),
                            createOrderHistoryDto(historyId2, orderId, OrderStatus.FILLED, "Order filled")
                    ));

            // when/then
            mockMvc.perform(get("/api/v1/orders/{userId}/{orderId}/history", userId, orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].status", is("NEW")))
                    .andExpect(jsonPath("$.data[1].status", is("FILLED")));
        }

        @Test
        @DisplayName("should return empty list when no history")
        @WithMockFirebaseUser(uid = "history-test-uid-9221", email = "history9221@test.com")
        void returnsEmptyListWhenNoHistory() throws Exception {
            // given
            Long userId = 9221L;
            UUID orderId = UUID.randomUUID();

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(orderHistoryQueryService.getOrderHistory(userId, orderId))
                    .thenReturn(List.of());

            // when/then
            mockMvc.perform(get("/api/v1/orders/{userId}/{orderId}/history", userId, orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{userId}/history/{historyId}")
    class GetOrderHistoryEntry {

        @Test
        @DisplayName("should return order history entry when found")
        @WithMockFirebaseUser(uid = "history-test-uid-9222", email = "history9222@test.com")
        void returnsOrderHistoryEntryWhenFound() throws Exception {
            // given
            Long userId = 9222L;
            UUID historyId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(orderHistoryQueryService.getOrderHistoryEntry(userId, historyId))
                    .thenReturn(Optional.of(createOrderHistoryDto(historyId, orderId, OrderStatus.FILLED, "Order filled")));

            // when/then
            mockMvc.perform(get("/api/v1/orders/{userId}/history/{historyId}", userId, historyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.status", is("FILLED")))
                    .andExpect(jsonPath("$.data.message", is("Order filled")));
        }

        @Test
        @DisplayName("should return 404 when order history entry not found")
        @WithMockFirebaseUser(uid = "history-test-uid-9223", email = "history9223@test.com")
        void returns404WhenOrderHistoryEntryNotFound() throws Exception {
            // given
            Long userId = 9223L;
            UUID historyId = UUID.randomUUID();

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(orderHistoryQueryService.getOrderHistoryEntry(userId, historyId))
                    .thenReturn(Optional.empty());

            // when/then
            mockMvc.perform(get("/api/v1/orders/{userId}/history/{historyId}", userId, historyId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", is("Order history entry not found")));
        }
    }
}
