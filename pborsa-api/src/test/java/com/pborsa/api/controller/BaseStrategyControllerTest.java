package com.pborsa.api.controller;

import com.pborsa.api.TestDataConstants;
import com.pborsa.api.TestFixtures;
import com.pborsa.api.config.TestSecurityConfig;
import com.pborsa.api.strategy.controller.BaseStrategyController;
import com.pborsa.domain.dto.strategy.BaseStrategyDto;
import com.pborsa.api.strategy.service.BaseStrategyService;
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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link BaseStrategyController}.
 * <p>
 * Tests controller behavior with mocked service dependencies.
 * Uses @AutoConfigureMockMvc(addFilters = false) to disable security filters.
 */
@WebMvcTest(BaseStrategyController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("BaseStrategyController")
class BaseStrategyControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    private BaseStrategyService baseStrategyService;

    @Nested
    @DisplayName("GET /api/v1/strategies")
    class GetAllStrategies {

        @Test
        @DisplayName("should return list of active strategies")
        void returnsStrategyList() throws Exception {
            // given
            BaseStrategyDto strategy = TestFixtures.createBaseStrategyDto();
            when(baseStrategyService.getAllActiveStrategies()).thenReturn(List.of(strategy));

            // when/then
            mockMvc.perform(get("/api/v1/strategies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].code", is(TestDataConstants.TEST_BASE_STRATEGY_CODE)))
                    .andExpect(jsonPath("$.data[0].name", is(TestDataConstants.TEST_BASE_STRATEGY_NAME)))
                    .andExpect(jsonPath("$.data[0].active", is(true)));

            verify(baseStrategyService).getAllActiveStrategies();
        }

        @Test
        @DisplayName("should return empty list when no strategies exist")
        void returnsEmptyList() throws Exception {
            // given
            when(baseStrategyService.getAllActiveStrategies()).thenReturn(List.of());

            // when/then
            mockMvc.perform(get("/api/v1/strategies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("should return multiple strategies")
        void returnsMultipleStrategies() throws Exception {
            // given
            BaseStrategyDto strategy1 = TestFixtures.createBaseStrategyDto();
            BaseStrategyDto strategy2 = TestFixtures.createBaseStrategyDto(
                    TestDataConstants.TEST_MEAN_REVERSION_STRATEGY_ID,
                    TestDataConstants.TEST_MEAN_REVERSION_STRATEGY_CODE,
                    "Test Mean Reversion",
                    true
            );
            when(baseStrategyService.getAllActiveStrategies()).thenReturn(List.of(strategy1, strategy2));

            // when/then
            mockMvc.perform(get("/api/v1/strategies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/strategies/{code}")
    class GetStrategyByCode {

        @Test
        @DisplayName("should return strategy when code exists")
        void returnsStrategyWhenFound() throws Exception {
            // given
            String code = TestDataConstants.TEST_BASE_STRATEGY_CODE;
            BaseStrategyDto strategy = TestFixtures.createBaseStrategyDto();
            when(baseStrategyService.getStrategyByCode(code)).thenReturn(Optional.of(strategy));

            // when/then
            mockMvc.perform(get("/api/v1/strategies/{code}", code))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.code", is(code)))
                    .andExpect(jsonPath("$.data.name", is(TestDataConstants.TEST_BASE_STRATEGY_NAME)));

            verify(baseStrategyService).getStrategyByCode(code);
        }

        @Test
        @DisplayName("should return 404 when strategy not found")
        void returns404WhenNotFound() throws Exception {
            // given
            String code = "NONEXISTENT";
            when(baseStrategyService.getStrategyByCode(code)).thenReturn(Optional.empty());

            // when/then
            mockMvc.perform(get("/api/v1/strategies/{code}", code))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", is("Strategy not found: " + code)));
        }

        @Test
        @DisplayName("should handle lowercase code")
        void handlesLowercaseCode() throws Exception {
            // given
            String code = "test_momentum";
            BaseStrategyDto strategy = TestFixtures.createBaseStrategyDto();
            when(baseStrategyService.getStrategyByCode(code)).thenReturn(Optional.of(strategy));

            // when/then
            mockMvc.perform(get("/api/v1/strategies/{code}", code))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)));
        }
    }
}
