package com.pborsa.api.controller;

import com.pborsa.api.account.AccountController;
import com.pborsa.api.config.TestSecurityConfig;
import com.pborsa.domain.dto.account.AccountInfoDto;
import com.pborsa.domain.dto.account.AccountStatus;
import com.pborsa.domain.dto.account.PositionDto;
import com.pborsa.api.security.WithMockFirebaseUser;
import com.pborsa.trading.account.AccountService;
import com.pborsa.trading.account.PositionService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link AccountController}.
 * <p>
 * Tests controller behavior with mocked service dependencies.
 */
@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("AccountController")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private PositionService positionService;

    private AccountInfoDto createAccountInfo(Long userId) {
        return AccountInfoDto.builder()
                .accountId("account-" + userId)
                .accountNumber("ACC-" + userId)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .cash(new BigDecimal("10000.00"))
                .portfolioValue(new BigDecimal("25000.00"))
                .buyingPower(new BigDecimal("40000.00"))
                .equity(new BigDecimal("25000.00"))
                .lastEquity(new BigDecimal("24500.00"))
                .longMarketValue(new BigDecimal("15000.00"))
                .shortMarketValue(BigDecimal.ZERO)
                .initialMargin(new BigDecimal("7500.00"))
                .maintenanceMargin(new BigDecimal("3750.00"))
                .lastMaintenanceMargin(new BigDecimal("3700.00"))
                .daytradeCount(BigDecimal.ZERO)
                .patternDayTrader(false)
                .tradingBlocked(false)
                .transfersBlocked(false)
                .accountBlocked(false)
                .tradeSuspendedByUser(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private PositionDto createPosition(String symbol, BigDecimal quantity, BigDecimal avgPrice, BigDecimal unrealizedPnL) {
        return PositionDto.builder()
                .assetId("asset-" + symbol)
                .symbol(symbol)
                .exchange("NASDAQ")
                .assetClass("us_equity")
                .averageEntryPrice(avgPrice)
                .quantity(quantity)
                .side("long")
                .marketValue(avgPrice.multiply(quantity))
                .costBasis(avgPrice.multiply(quantity))
                .unrealizedPnL(unrealizedPnL)
                .unrealizedPnLPercent(new BigDecimal("5.00"))
                .unrealizedIntradayPnL(new BigDecimal("50.00"))
                .unrealizedIntradayPnLPercent(new BigDecimal("1.00"))
                .currentPrice(avgPrice.add(unrealizedPnL.divide(quantity, 2, BigDecimal.ROUND_HALF_UP)))
                .lastDayPrice(avgPrice)
                .changeToday(new BigDecimal("2.50"))
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}")
    class GetAccountInfo {

        @Test
        @DisplayName("should return account info")
        @WithMockFirebaseUser(uid = "account-test-uid-9260", email = "account9260@test.com")
        void returnsAccountInfo() throws Exception {
            // given
            Long userId = 9260L;
            when(accountService.getAccountInfo(userId)).thenReturn(createAccountInfo(userId));

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.accountId", is("account-9260")))
                    .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                    .andExpect(jsonPath("$.data.currency", is("USD")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}/buying-power")
    class GetBuyingPower {

        @Test
        @DisplayName("should return buying power")
        @WithMockFirebaseUser(uid = "account-test-uid-9261", email = "account9261@test.com")
        void returnsBuyingPower() throws Exception {
            // given
            Long userId = 9261L;
            when(accountService.getBuyingPower(userId)).thenReturn(new BigDecimal("50000.00"));

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/buying-power", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(50000.00)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}/cash")
    class GetCashBalance {

        @Test
        @DisplayName("should return cash balance")
        @WithMockFirebaseUser(uid = "account-test-uid-9262", email = "account9262@test.com")
        void returnsCashBalance() throws Exception {
            // given
            Long userId = 9262L;
            when(accountService.getCashBalance(userId)).thenReturn(new BigDecimal("15000.00"));

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/cash", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(15000.00)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}/equity")
    class GetEquity {

        @Test
        @DisplayName("should return equity")
        @WithMockFirebaseUser(uid = "account-test-uid-9263", email = "account9263@test.com")
        void returnsEquity() throws Exception {
            // given
            Long userId = 9263L;
            when(accountService.getEquity(userId)).thenReturn(new BigDecimal("30000.00"));

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/equity", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(30000.00)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}/can-trade")
    class CanTrade {

        @Test
        @DisplayName("should return true when trading allowed")
        @WithMockFirebaseUser(uid = "account-test-uid-9264", email = "account9264@test.com")
        void returnsTrueWhenTradingAllowed() throws Exception {
            // given
            Long userId = 9264L;
            when(accountService.canTrade(userId)).thenReturn(true);

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/can-trade", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(true)));
        }

        @Test
        @DisplayName("should return false when trading blocked")
        @WithMockFirebaseUser(uid = "account-test-uid-9265", email = "account9265@test.com")
        void returnsFalseWhenTradingBlocked() throws Exception {
            // given
            Long userId = 9265L;
            when(accountService.canTrade(userId)).thenReturn(false);

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/can-trade", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(false)));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/account/{userId}/refresh")
    class RefreshAccountInfo {

        @Test
        @DisplayName("should refresh and return account info")
        @WithMockFirebaseUser(uid = "account-test-uid-9266", email = "account9266@test.com")
        void refreshesAndReturnsAccountInfo() throws Exception {
            // given
            Long userId = 9266L;
            when(accountService.refreshAccountInfo(userId)).thenReturn(createAccountInfo(userId));

            // when/then
            mockMvc.perform(post("/api/v1/account/{userId}/refresh", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Account info refreshed")))
                    .andExpect(jsonPath("$.data.accountId", is("account-9266")));

            verify(accountService).refreshAccountInfo(userId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}/positions")
    class GetPositions {

        @Test
        @DisplayName("should return all positions")
        @WithMockFirebaseUser(uid = "account-test-uid-9267", email = "account9267@test.com")
        void returnsAllPositions() throws Exception {
            // given
            Long userId = 9267L;
            when(positionService.getAllPositions(userId)).thenReturn(List.of(
                    createPosition("TSLA", new BigDecimal("10"), new BigDecimal("250.00"), new BigDecimal("100.00")),
                    createPosition("AAPL", new BigDecimal("20"), new BigDecimal("175.00"), new BigDecimal("-50.00"))
            ));

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/positions", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].symbol", is("TSLA")))
                    .andExpect(jsonPath("$.data[1].symbol", is("AAPL")));
        }

        @Test
        @DisplayName("should return empty list when no positions")
        @WithMockFirebaseUser(uid = "account-test-uid-9268", email = "account9268@test.com")
        void returnsEmptyListWhenNoPositions() throws Exception {
            // given
            Long userId = 9268L;
            when(positionService.getAllPositions(userId)).thenReturn(List.of());

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/positions", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}/positions/{symbol}")
    class GetPosition {

        @Test
        @DisplayName("should return position for symbol")
        @WithMockFirebaseUser(uid = "account-test-uid-9269", email = "account9269@test.com")
        void returnsPositionForSymbol() throws Exception {
            // given
            Long userId = 9269L;
            String symbol = "NVDA";
            when(positionService.getPosition(userId, symbol))
                    .thenReturn(createPosition(symbol, new BigDecimal("5"), new BigDecimal("500.00"), new BigDecimal("250.00")));

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/positions/{symbol}", userId, symbol))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.symbol", is("NVDA")))
                    .andExpect(jsonPath("$.data.quantity", is(5)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}/portfolio-value")
    class GetPortfolioValue {

        @Test
        @DisplayName("should return portfolio value")
        @WithMockFirebaseUser(uid = "account-test-uid-9270", email = "account9270@test.com")
        void returnsPortfolioValue() throws Exception {
            // given
            Long userId = 9270L;
            when(positionService.calculateTotalPortfolioValue(userId)).thenReturn(new BigDecimal("75000.00"));

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/portfolio-value", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(75000.00)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}/unrealized-pnl")
    class GetUnrealizedPnL {

        @Test
        @DisplayName("should return unrealized P/L")
        @WithMockFirebaseUser(uid = "account-test-uid-9271", email = "account9271@test.com")
        void returnsUnrealizedPnL() throws Exception {
            // given
            Long userId = 9271L;
            when(positionService.calculateTotalUnrealizedPnL(userId)).thenReturn(new BigDecimal("1500.00"));

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/unrealized-pnl", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(1500.00)));
        }

        @Test
        @DisplayName("should return negative unrealized P/L")
        @WithMockFirebaseUser(uid = "account-test-uid-9272", email = "account9272@test.com")
        void returnsNegativeUnrealizedPnL() throws Exception {
            // given
            Long userId = 9272L;
            when(positionService.calculateTotalUnrealizedPnL(userId)).thenReturn(new BigDecimal("-500.00"));

            // when/then
            mockMvc.perform(get("/api/v1/account/{userId}/unrealized-pnl", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(-500.00)));
        }
    }
}
