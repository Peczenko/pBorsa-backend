package com.pborsa.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pborsa.api.config.TestSecurityConfig;
import com.pborsa.api.security.WithMockFirebaseUser;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import com.pborsa.api.shared.security.SecurityService;
import com.pborsa.api.strategy.controller.UserStrategyController;
import com.pborsa.api.strategy.service.StrategyService;
import com.pborsa.domain.dto.strategy.*;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link UserStrategyController}.
 * <p>
 * Tests controller behavior with mocked service dependencies.
 * NOTE: The activateStrategy endpoint is SKIPPED because it triggers Temporal workflow.
 */
@WebMvcTest(UserStrategyController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("UserStrategyController")
class UserStrategyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StrategyService strategyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SecurityService securityService;

    private BaseStrategyDto createBaseStrategyDto() {
        return new BaseStrategyDto(
                1L,
                "TEST_MOMENTUM",
                "Test Momentum Strategy",
                "Test description",
                true,
                Instant.now(),
                Instant.now()
        );
    }

    private UserStrategyDto createUserStrategyDto(Long id, String name, String symbol, String status) {
        return new UserStrategyDto(
                id,
                name,
                createBaseStrategyDto(),
                symbol,
                status,
                new BigDecimal("5000.00"),
                Instant.now(),
                Instant.now()
        );
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/strategies")
    class GetUserStrategies {

        @Test
        @DisplayName("should return list of user strategies")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9250", email = "strategy9250@test.com")
        void returnsListOfUserStrategies() throws Exception {
            // given
            Long userId = 9250L;
            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.getStrategiesByUserId(userId))
                    .thenReturn(List.of(
                            createUserStrategyDto(1L, "TSLA Momentum", "TSLA", "ACTIVE"),
                            createUserStrategyDto(2L, "AAPL Momentum", "AAPL", "CREATED")
                    ));

            // when/then
            mockMvc.perform(get("/api/v1/users/{userId}/strategies", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].symbol", is("TSLA")))
                    .andExpect(jsonPath("$.data[1].symbol", is("AAPL")));
        }

        @Test
        @DisplayName("should return empty list when user has no strategies")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9251", email = "strategy9251@test.com")
        void returnsEmptyListWhenNoStrategies() throws Exception {
            // given
            Long userId = 9251L;
            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.getStrategiesByUserId(userId))
                    .thenReturn(List.of());

            // when/then
            mockMvc.perform(get("/api/v1/users/{userId}/strategies", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/strategies/{strategyId}")
    class GetUserStrategy {

        @Test
        @DisplayName("should return strategy when found")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9252", email = "strategy9252@test.com")
        void returnsStrategyWhenFound() throws Exception {
            // given
            Long userId = 9252L;
            Long strategyId = 1L;
            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.getStrategyById(userId, strategyId))
                    .thenReturn(Optional.of(createUserStrategyDto(strategyId, "TSLA Momentum", "TSLA", "ACTIVE")));

            // when/then
            mockMvc.perform(get("/api/v1/users/{userId}/strategies/{strategyId}", userId, strategyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.symbol", is("TSLA")))
                    .andExpect(jsonPath("$.data.status", is("ACTIVE")));
        }

        @Test
        @DisplayName("should return 404 when strategy not found")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9253", email = "strategy9253@test.com")
        void returns404WhenNotFound() throws Exception {
            // given
            Long userId = 9253L;
            Long strategyId = 999L;
            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.getStrategyById(userId, strategyId))
                    .thenReturn(Optional.empty());

            // when/then
            mockMvc.perform(get("/api/v1/users/{userId}/strategies/{strategyId}", userId, strategyId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", is("Strategy not found")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/{userId}/strategies")
    class CreateUserStrategy {

        @Test
        @DisplayName("should create strategy successfully")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9254", email = "strategy9254@test.com")
        void createsStrategySuccessfully() throws Exception {
            // given
            Long userId = 9254L;
            CreateUserStrategyRequest request = new CreateUserStrategyRequest(
                    "TEST_MOMENTUM",
                    "NVDA Momentum",
                    "NVDA",
                    new BigDecimal("10000.00")
            );

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.createUserStrategy(eq(userId), any(CreateUserStrategyRequest.class)))
                    .thenReturn(createUserStrategyDto(1L, "NVDA Momentum", "NVDA", "CREATED"));

            // when/then
            mockMvc.perform(post("/api/v1/users/{userId}/strategies", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.name", is("NVDA Momentum")))
                    .andExpect(jsonPath("$.data.symbol", is("NVDA")))
                    .andExpect(jsonPath("$.data.status", is("CREATED")));
        }

        @Test
        @DisplayName("should return 400 for invalid request")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9255", email = "strategy9255@test.com")
        void returns400ForInvalidRequest() throws Exception {
            // given - missing required fields
            Long userId = 9255L;
            String invalidRequest = "{}";

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);

            // when/then
            mockMvc.perform(post("/api/v1/users/{userId}/strategies", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{userId}/strategies/{strategyId}")
    class UpdateUserStrategy {

        @Test
        @DisplayName("should update strategy name")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9256", email = "strategy9256@test.com")
        void updatesStrategyName() throws Exception {
            // given
            Long userId = 9256L;
            Long strategyId = 1L;
            UpdateUserStrategyRequest request = new UpdateUserStrategyRequest("Updated Name", null);

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.updateUserStrategy(eq(userId), eq(strategyId), any(UpdateUserStrategyRequest.class)))
                    .thenReturn(Optional.of(createUserStrategyDto(strategyId, "Updated Name", "TSLA", "ACTIVE")));

            // when/then
            mockMvc.perform(patch("/api/v1/users/{userId}/strategies/{strategyId}", userId, strategyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.name", is("Updated Name")));
        }

        @Test
        @DisplayName("should return 404 when updating non-existent strategy")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9257", email = "strategy9257@test.com")
        void returns404WhenUpdatingNonExistent() throws Exception {
            // given
            Long userId = 9257L;
            Long strategyId = 999L;
            UpdateUserStrategyRequest request = new UpdateUserStrategyRequest("New Name", null);

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.updateUserStrategy(eq(userId), eq(strategyId), any(UpdateUserStrategyRequest.class)))
                    .thenReturn(Optional.empty());

            // when/then
            mockMvc.perform(patch("/api/v1/users/{userId}/strategies/{strategyId}", userId, strategyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{userId}/strategies/{strategyId}")
    class DeleteUserStrategy {

        @Test
        @DisplayName("should return bad request because deletion is temporarily disabled")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9258", email = "strategy9258@test.com")
        void returnsBadRequestBecauseDeletionDisabled() throws Exception {
            // given
            Long userId = 9258L;
            Long strategyId = 1L;

            // when/then - endpoint is temporarily disabled
            mockMvc.perform(delete("/api/v1/users/{userId}/strategies/{strategyId}", userId, strategyId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", is("Strategy removal is temporally disabled by API")));
        }
    }

    // NOTE: activateStrategy endpoint is SKIPPED - it triggers Temporal workflow

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/strategies/{strategyId}/pnl")
    class GetStrategyPnL {

        @Test
        @DisplayName("should return strategy P/L data")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9260", email = "strategy9260@test.com")
        void returnsStrategyPnL() throws Exception {
            // given
            Long userId = 9260L;
            Long strategyId = 1L;
            StrategyPnLDto pnl = StrategyPnLDto.of(
                    strategyId,
                    "TSLA",
                    new BigDecimal("500.00"),  // realized
                    new BigDecimal("150.00"),  // unrealized
                    new BigDecimal("100"),     // shares
                    new BigDecimal("250.00"),  // avg cost
                    new BigDecimal("251.50"),  // current price
                    new BigDecimal("25000.00"), // cost basis
                    Instant.now()
            );

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.getStrategyPnL(userId, strategyId))
                    .thenReturn(Optional.of(pnl));

            // when/then
            mockMvc.perform(get("/api/v1/users/{userId}/strategies/{strategyId}/pnl", userId, strategyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.strategyId", is(1)))
                    .andExpect(jsonPath("$.data.symbol", is("TSLA")))
                    .andExpect(jsonPath("$.data.realizedPnL", is(500.00)))
                    .andExpect(jsonPath("$.data.unrealizedPnL", is(150.00)))
                    .andExpect(jsonPath("$.data.totalPnL", is(650.00)));
        }

        @Test
        @DisplayName("should return 404 when strategy not found")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9261", email = "strategy9261@test.com")
        void returns404WhenStrategyNotFound() throws Exception {
            // given
            Long userId = 9261L;
            Long strategyId = 999L;

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.getStrategyPnL(userId, strategyId))
                    .thenReturn(Optional.empty());

            // when/then
            mockMvc.perform(get("/api/v1/users/{userId}/strategies/{strategyId}/pnl", userId, strategyId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)));
        }

        @Test
        @DisplayName("should return empty P/L for strategy with no position")
        @WithMockFirebaseUser(uid = "strategy-test-uid-9262", email = "strategy9262@test.com")
        void returnsEmptyPnLForNoPosition() throws Exception {
            // given
            Long userId = 9262L;
            Long strategyId = 2L;
            StrategyPnLDto emptyPnl = StrategyPnLDto.empty(strategyId, "AAPL");

            when(securityService.resolveTargetUserId(eq(userId), any(FirebaseUserPrincipal.class)))
                    .thenReturn(userId);
            when(strategyService.getStrategyPnL(userId, strategyId))
                    .thenReturn(Optional.of(emptyPnl));

            // when/then
            mockMvc.perform(get("/api/v1/users/{userId}/strategies/{strategyId}/pnl", userId, strategyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.realizedPnL", is(0)))
                    .andExpect(jsonPath("$.data.unrealizedPnL", is(0)))
                    .andExpect(jsonPath("$.data.totalPnL", is(0)));
        }
    }
}
