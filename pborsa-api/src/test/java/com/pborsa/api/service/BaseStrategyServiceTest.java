package com.pborsa.api.service;

import com.pborsa.api.TestDataConstants;
import com.pborsa.api.TestFixtures;
import com.pborsa.api.domain.dto.strategy.BaseStrategyDto;
import com.pborsa.api.domain.entity.BaseStrategyEntity;
import com.pborsa.api.repository.BaseStrategyRepository;
import com.pborsa.api.service.mapper.BaseStrategyMapper;
import com.pborsa.api.service.strategy.BaseStrategyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BaseStrategyService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BaseStrategyService")
class BaseStrategyServiceTest {

    @Mock
    private BaseStrategyRepository baseStrategyRepository;

    @Mock
    private BaseStrategyMapper baseStrategyMapper;

    @InjectMocks
    private BaseStrategyService baseStrategyService;

    private BaseStrategyEntity testEntity;
    private BaseStrategyDto testDto;

    @BeforeEach
    void setUp() {
        testEntity = TestFixtures.createBaseStrategyEntity();
        testDto = TestFixtures.createBaseStrategyDto();
    }

    @Nested
    @DisplayName("getAllActiveStrategies")
    class GetAllActiveStrategies {

        @Test
        @DisplayName("should return list of active strategies")
        void returnsActiveStrategies() {
            // given
            List<BaseStrategyEntity> entities = List.of(testEntity);
            List<BaseStrategyDto> dtos = List.of(testDto);

            when(baseStrategyRepository.findByActiveTrue()).thenReturn(entities);
            when(baseStrategyMapper.toBaseStrategyDtoList(entities)).thenReturn(dtos);

            // when
            List<BaseStrategyDto> result = baseStrategyService.getAllActiveStrategies();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().code()).isEqualTo(TestDataConstants.TEST_BASE_STRATEGY_CODE);

            verify(baseStrategyRepository).findByActiveTrue();
            verify(baseStrategyMapper).toBaseStrategyDtoList(entities);
        }

        @Test
        @DisplayName("should return empty list when no active strategies exist")
        void returnsEmptyListWhenNoActiveStrategies() {
            // given
            when(baseStrategyRepository.findByActiveTrue()).thenReturn(List.of());
            when(baseStrategyMapper.toBaseStrategyDtoList(List.of())).thenReturn(List.of());

            // when
            List<BaseStrategyDto> result = baseStrategyService.getAllActiveStrategies();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllStrategies")
    class GetAllStrategies {

        @Test
        @DisplayName("should return all strategies including inactive")
        void returnsAllStrategies() {
            // given
            BaseStrategyEntity inactiveEntity = TestFixtures.createInactiveBaseStrategyEntity();
            List<BaseStrategyEntity> entities = List.of(testEntity, inactiveEntity);
            List<BaseStrategyDto> dtos = List.of(testDto, TestFixtures.createInactiveBaseStrategyDto());

            when(baseStrategyRepository.findAll()).thenReturn(entities);
            when(baseStrategyMapper.toBaseStrategyDtoList(entities)).thenReturn(dtos);

            // when
            List<BaseStrategyDto> result = baseStrategyService.getAllStrategies();

            // then
            assertThat(result).hasSize(2);
            verify(baseStrategyRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getStrategyByCode")
    class GetStrategyByCode {

        @Test
        @DisplayName("should return strategy when code exists")
        void returnsStrategyWhenCodeExists() {
            // given
            String code = TestDataConstants.TEST_BASE_STRATEGY_CODE;
            when(baseStrategyRepository.findByCode(code.toUpperCase())).thenReturn(Optional.of(testEntity));
            when(baseStrategyMapper.toBaseStrategyDto(testEntity)).thenReturn(testDto);

            // when
            Optional<BaseStrategyDto> result = baseStrategyService.getStrategyByCode(code);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().code()).isEqualTo(code);
        }

        @Test
        @DisplayName("should return empty when code does not exist")
        void returnsEmptyWhenCodeNotFound() {
            // given
            String code = "NONEXISTENT";
            when(baseStrategyRepository.findByCode(code.toUpperCase())).thenReturn(Optional.empty());

            // when
            Optional<BaseStrategyDto> result = baseStrategyService.getStrategyByCode(code);

            // then
            assertThat(result).isEmpty();
            verify(baseStrategyMapper, never()).toBaseStrategyDto(any());
        }

        @Test
        @DisplayName("should convert code to uppercase")
        void convertsCodeToUppercase() {
            // given
            String code = "test_momentum";
            when(baseStrategyRepository.findByCode("TEST_MOMENTUM")).thenReturn(Optional.of(testEntity));
            when(baseStrategyMapper.toBaseStrategyDto(testEntity)).thenReturn(testDto);

            // when
            baseStrategyService.getStrategyByCode(code);

            // then
            verify(baseStrategyRepository).findByCode("TEST_MOMENTUM");
        }

        @Test
        @DisplayName("should throw exception when code is null")
        void throwsExceptionWhenCodeIsNull() {
            assertThatThrownBy(() -> baseStrategyService.getStrategyByCode(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Strategy code is required");
        }

        @Test
        @DisplayName("should throw exception when code is blank")
        void throwsExceptionWhenCodeIsBlank() {
            assertThatThrownBy(() -> baseStrategyService.getStrategyByCode("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Strategy code is required");
        }
    }

    @Nested
    @DisplayName("getStrategyById")
    class GetStrategyById {

        @Test
        @DisplayName("should return strategy when id exists")
        void returnsStrategyWhenIdExists() {
            // given
            Long id = TestDataConstants.TEST_BASE_STRATEGY_ID;
            when(baseStrategyRepository.findById(id)).thenReturn(Optional.of(testEntity));
            when(baseStrategyMapper.toBaseStrategyDto(testEntity)).thenReturn(testDto);

            // when
            Optional<BaseStrategyDto> result = baseStrategyService.getStrategyById(id);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(id);
        }

        @Test
        @DisplayName("should return empty when id does not exist")
        void returnsEmptyWhenIdNotFound() {
            // given
            Long id = 999L;
            when(baseStrategyRepository.findById(id)).thenReturn(Optional.empty());

            // when
            Optional<BaseStrategyDto> result = baseStrategyService.getStrategyById(id);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw exception when id is null")
        void throwsExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> baseStrategyService.getStrategyById(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Strategy ID is required");
        }
    }

    @Nested
    @DisplayName("isStrategyActiveByCode")
    class IsStrategyActiveByCode {

        @Test
        @DisplayName("should return true when strategy is active")
        void returnsTrueWhenActive() {
            // given
            String code = TestDataConstants.TEST_BASE_STRATEGY_CODE;
            testEntity.setActive(true);
            when(baseStrategyRepository.findByCode(code.toUpperCase())).thenReturn(Optional.of(testEntity));

            // when
            boolean result = baseStrategyService.isStrategyActiveByCode(code);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when strategy is inactive")
        void returnsFalseWhenInactive() {
            // given
            String code = TestDataConstants.TEST_INACTIVE_STRATEGY_CODE;
            BaseStrategyEntity inactiveEntity = TestFixtures.createInactiveBaseStrategyEntity();
            when(baseStrategyRepository.findByCode(code.toUpperCase())).thenReturn(Optional.of(inactiveEntity));

            // when
            boolean result = baseStrategyService.isStrategyActiveByCode(code);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when strategy does not exist")
        void returnsFalseWhenNotFound() {
            // given
            String code = "NONEXISTENT";
            when(baseStrategyRepository.findByCode(code.toUpperCase())).thenReturn(Optional.empty());

            // when
            boolean result = baseStrategyService.isStrategyActiveByCode(code);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getStrategyEntityByCode")
    class GetStrategyEntityByCode {

        @Test
        @DisplayName("should return entity when code exists")
        void returnsEntityWhenCodeExists() {
            // given
            String code = TestDataConstants.TEST_BASE_STRATEGY_CODE;
            when(baseStrategyRepository.findByCode(code.toUpperCase())).thenReturn(Optional.of(testEntity));

            // when
            Optional<BaseStrategyEntity> result = baseStrategyService.getStrategyEntityByCode(code);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo(code);
        }

        @Test
        @DisplayName("should return empty when code is null")
        void returnsEmptyWhenCodeIsNull() {
            // when
            Optional<BaseStrategyEntity> result = baseStrategyService.getStrategyEntityByCode(null);

            // then
            assertThat(result).isEmpty();
            verify(baseStrategyRepository, never()).findByCode(any());
        }

        @Test
        @DisplayName("should return empty when code is blank")
        void returnsEmptyWhenCodeIsBlank() {
            // when
            Optional<BaseStrategyEntity> result = baseStrategyService.getStrategyEntityByCode("   ");

            // then
            assertThat(result).isEmpty();
            verify(baseStrategyRepository, never()).findByCode(any());
        }
    }
}
