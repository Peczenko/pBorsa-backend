package com.pborsa.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pborsa.api.config.TestSecurityConfig;
import com.pborsa.api.domain.dto.admin.SetAdminRequest;
import com.pborsa.api.domain.dto.strategy.OrderDetailDto;
import com.pborsa.api.domain.dto.strategy.OrderHistoryDto;
import com.pborsa.api.domain.dto.trading.OrderSide;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderStatusReason;
import com.pborsa.api.domain.dto.trading.OrderType;
import com.pborsa.api.domain.dto.trading.TimeInForce;
import com.pborsa.api.security.FirebaseUserPrincipal;
import com.pborsa.api.security.WithMockFirebaseUser;
import com.pborsa.api.service.admin.FirebaseAdminService;
import com.pborsa.api.service.security.SecurityService;
import com.pborsa.api.service.trading.OrderHistoryQueryService;
import com.pborsa.api.service.trading.OrderQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link AdminController}.
 * <p>
 * Tests controller behavior with mocked service dependencies.
 * All tests use admin=true in @WithMockFirebaseUser annotation.
 */
@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("AdminController")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FirebaseAdminService firebaseAdminService;

    @MockitoBean
    private SecurityService securityService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private OrderHistoryQueryService orderHistoryQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OrderDetailDto createOrderDetailDto(UUID id, String symbol, OrderSide side, OrderStatus status) {
        Instant now = Instant.now();
        return new OrderDetailDto(
                id,
                "alpaca-" + id,
                "client-" + id,
                symbol,
                new BigDecimal("10"),
                new BigDecimal("10"),
                side,
                OrderType.MARKET,
                TimeInForce.DAY,
                null,
                null,
                new BigDecimal("250.50"),
                status,
                null,
                false,
                now,
                now,
                now,
                status == OrderStatus.FILLED ? now : null,
                null,
                null,
                "us_equity",
                9240L
        );
    }

    private OrderHistoryDto createOrderHistoryDto(UUID id, UUID orderId, OrderStatus status, String message) {
        return new OrderHistoryDto(
                id,
                orderId,
                status,
                null,
                message,
                Instant.now()
        );
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/users/{targetUserId}/admin")
    class SetAdminStatus {

        @Test
        @DisplayName("should set admin status successfully")
        @WithMockFirebaseUser(uid = "admin-test-uid-9240", email = "admin9240@test.com", admin = true)
        void setsAdminStatusSuccessfully() throws Exception {
            // given
            Long targetUserId = 100L;
            SetAdminRequest request = new SetAdminRequest(true);

            when(securityService.getCurrentUserId(any(FirebaseUserPrincipal.class))).thenReturn(9240L);
            doNothing().when(firebaseAdminService).setAdminStatus(targetUserId, true);

            // when/then
            mockMvc.perform(put("/api/v1/admin/users/{targetUserId}/admin", targetUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.userId", is(100)))
                    .andExpect(jsonPath("$.data.admin", is(true)));

            verify(firebaseAdminService).setAdminStatus(targetUserId, true);
        }

        @Test
        @DisplayName("should revoke admin status successfully")
        @WithMockFirebaseUser(uid = "admin-test-uid-9241", email = "admin9241@test.com", admin = true)
        void revokesAdminStatusSuccessfully() throws Exception {
            // given
            Long targetUserId = 101L;
            SetAdminRequest request = new SetAdminRequest(false);

            when(securityService.getCurrentUserId(any(FirebaseUserPrincipal.class))).thenReturn(9241L);
            doNothing().when(firebaseAdminService).setAdminStatus(targetUserId, false);

            // when/then
            mockMvc.perform(put("/api/v1/admin/users/{targetUserId}/admin", targetUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.userId", is(101)))
                    .andExpect(jsonPath("$.data.admin", is(false)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/{targetUserId}/admin")
    class GetAdminStatus {

        @Test
        @DisplayName("should return true for admin user")
        @WithMockFirebaseUser(uid = "admin-test-uid-9242", email = "admin9242@test.com", admin = true)
        void returnsTrueForAdminUser() throws Exception {
            // given
            Long targetUserId = 102L;
            when(firebaseAdminService.getAdminStatus(targetUserId)).thenReturn(true);

            // when/then
            mockMvc.perform(get("/api/v1/admin/users/{targetUserId}/admin", targetUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.userId", is(102)))
                    .andExpect(jsonPath("$.data.admin", is(true)));
        }

        @Test
        @DisplayName("should return false for non-admin user")
        @WithMockFirebaseUser(uid = "admin-test-uid-9243", email = "admin9243@test.com", admin = true)
        void returnsFalseForNonAdminUser() throws Exception {
            // given
            Long targetUserId = 103L;
            when(firebaseAdminService.getAdminStatus(targetUserId)).thenReturn(false);

            // when/then
            mockMvc.perform(get("/api/v1/admin/users/{targetUserId}/admin", targetUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.userId", is(103)))
                    .andExpect(jsonPath("$.data.admin", is(false)));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/users/{targetUserId}/grant")
    class GrantAdmin {

        @Test
        @DisplayName("should grant admin privileges successfully")
        @WithMockFirebaseUser(uid = "admin-test-uid-9244", email = "admin9244@test.com", admin = true)
        void grantsAdminPrivilegesSuccessfully() throws Exception {
            // given
            Long targetUserId = 104L;

            when(securityService.getCurrentUserId(any(FirebaseUserPrincipal.class))).thenReturn(9244L);
            doNothing().when(firebaseAdminService).grantAdmin(targetUserId);

            // when/then
            mockMvc.perform(post("/api/v1/admin/users/{targetUserId}/grant", targetUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.userId", is(104)))
                    .andExpect(jsonPath("$.data.admin", is(true)));

            verify(firebaseAdminService).grantAdmin(targetUserId);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/users/{targetUserId}/revoke")
    class RevokeAdmin {

        @Test
        @DisplayName("should revoke admin privileges successfully")
        @WithMockFirebaseUser(uid = "admin-test-uid-9245", email = "admin9245@test.com", admin = true)
        void revokesAdminPrivilegesSuccessfully() throws Exception {
            // given
            Long targetUserId = 105L;

            when(securityService.getCurrentUserId(any(FirebaseUserPrincipal.class))).thenReturn(9245L);
            doNothing().when(firebaseAdminService).revokeAdmin(targetUserId);

            // when/then
            mockMvc.perform(post("/api/v1/admin/users/{targetUserId}/revoke", targetUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.userId", is(105)))
                    .andExpect(jsonPath("$.data.admin", is(false)));

            verify(firebaseAdminService).revokeAdmin(targetUserId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/orders/{orderId}")
    class GetOrderAdmin {

        @Test
        @DisplayName("should return order without ownership check")
        @WithMockFirebaseUser(uid = "admin-test-uid-9246", email = "admin9246@test.com", admin = true)
        void returnsOrderWithoutOwnershipCheck() throws Exception {
            // given
            UUID orderId = UUID.randomUUID();
            when(orderQueryService.getOrderDetailAdmin(orderId))
                    .thenReturn(Optional.of(createOrderDetailDto(orderId, "TSLA", OrderSide.BUY, OrderStatus.FILLED)));

            // when/then
            mockMvc.perform(get("/api/v1/admin/orders/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.symbol", is("TSLA")))
                    .andExpect(jsonPath("$.data.side", is("BUY")))
                    .andExpect(jsonPath("$.data.status", is("FILLED")));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        @WithMockFirebaseUser(uid = "admin-test-uid-9247", email = "admin9247@test.com", admin = true)
        void returns404WhenOrderNotFound() throws Exception {
            // given
            UUID orderId = UUID.randomUUID();
            when(orderQueryService.getOrderDetailAdmin(orderId)).thenReturn(Optional.empty());

            // when/then
            mockMvc.perform(get("/api/v1/admin/orders/{orderId}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", is("Order not found")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/orders/{orderId}/history")
    class GetOrderHistoryAdmin {

        @Test
        @DisplayName("should return order history without ownership check")
        @WithMockFirebaseUser(uid = "admin-test-uid-9248", email = "admin9248@test.com", admin = true)
        void returnsOrderHistoryWithoutOwnershipCheck() throws Exception {
            // given
            UUID orderId = UUID.randomUUID();
            UUID historyId1 = UUID.randomUUID();
            UUID historyId2 = UUID.randomUUID();

            when(orderHistoryQueryService.getOrderHistoryAdmin(orderId))
                    .thenReturn(List.of(
                            createOrderHistoryDto(historyId1, orderId, OrderStatus.NEW, "Order created"),
                            createOrderHistoryDto(historyId2, orderId, OrderStatus.FILLED, "Order filled")
                    ));

            // when/then
            mockMvc.perform(get("/api/v1/admin/orders/{orderId}/history", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].status", is("NEW")))
                    .andExpect(jsonPath("$.data[1].status", is("FILLED")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/history/{historyId}")
    class GetOrderHistoryEntryAdmin {

        @Test
        @DisplayName("should return history entry without ownership check")
        @WithMockFirebaseUser(uid = "admin-test-uid-9249", email = "admin9249@test.com", admin = true)
        void returnsHistoryEntryWithoutOwnershipCheck() throws Exception {
            // given
            UUID historyId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();

            when(orderHistoryQueryService.getOrderHistoryEntryAdmin(historyId))
                    .thenReturn(Optional.of(createOrderHistoryDto(historyId, orderId, OrderStatus.CANCELED, "Order cancelled")));

            // when/then
            mockMvc.perform(get("/api/v1/admin/history/{historyId}", historyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.status", is("CANCELED")))
                    .andExpect(jsonPath("$.data.message", is("Order cancelled")));
        }

        @Test
        @DisplayName("should return 404 when history entry not found")
        @WithMockFirebaseUser(uid = "admin-test-uid-9250", email = "admin9250@test.com", admin = true)
        void returns404WhenHistoryEntryNotFound() throws Exception {
            // given
            UUID historyId = UUID.randomUUID();
            when(orderHistoryQueryService.getOrderHistoryEntryAdmin(historyId)).thenReturn(Optional.empty());

            // when/then
            mockMvc.perform(get("/api/v1/admin/history/{historyId}", historyId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", is("Order history entry not found")));
        }
    }
}
