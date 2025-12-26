package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.account.PositionDto;
import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.exception.AlpacaException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.service.mapper.AccountMapper;
import com.pborsa.api.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.trader.model.Position;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing trading positions.
 * Handles position retrieval, closing, and management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PositionService {

    private final AlpacaClientFactory clientFactory;
    private final UserCredentialsService credentialsService;
    private final AccountMapper accountMapper;
    private final OrderMapper orderMapper;
    private final OrderService orderService;

    /**
     * Gets all open positions for a user.
     *
     * @param userId User ID
     * @return List of positions
     */
    public List<PositionDto> getAllPositions(String userId) {
        log.debug("Fetching all positions for user: {}", userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Get positions using OpenAPI PositionsApi
            List<Position> positions = client.trader().positions().getAllOpenPositions();

            return positions.stream()
                    .map(accountMapper::toPositionDto)
                    .toList();
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch positions for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to fetch positions: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Async version of getAllPositions.
     *
     * @param userId User ID
     * @return CompletableFuture with list of positions
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<List<PositionDto>> getAllPositionsAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> getAllPositions(userId));
    }

    /**
     * Gets a specific position by symbol.
     *
     * @param userId User ID
     * @param symbol Stock symbol
     * @return Optional containing the position if found
     */
    public Optional<PositionDto> getPositionBySymbol(String userId, String symbol) {
        log.debug("Fetching position for symbol {} for user: {}", symbol, userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Get position by symbol using OpenAPI PositionsApi
            Position position = client.trader().positions().getOpenPosition(symbol);
            return Optional.of(accountMapper.toPositionDto(position));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                log.debug("No position found for symbol: {} for user: {}", symbol, userId);
                return Optional.empty();
            }
            log.error("Failed to fetch position for symbol {} for user: {}", symbol, userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to fetch position: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Gets a specific position by symbol (non-optional version for controller).
     *
     * @param userId User ID
     * @param symbol Stock symbol
     * @return Position DTO
     * @throws AlpacaException if position not found
     */
    public PositionDto getPosition(String userId, String symbol) {
        return getPositionBySymbol(userId, symbol)
                .orElseThrow(() -> new AlpacaException(
                        AlpacaException.ErrorCode.SYMBOL_NOT_FOUND,
                        "No position found for symbol: " + symbol
                ));
    }

    /**
     * Closes a position by symbol.
     *
     * @param userId User ID
     * @param symbol Stock symbol to close
     * @return Order response for the close order
     */
    public OrderResponse closePosition(String userId, String symbol) {
        log.info("Closing position for symbol {} for user: {}", symbol, userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Close position using OpenAPI PositionsApi
            net.jacobpeterson.alpaca.openapi.trader.model.Order closedOrder = 
                    client.trader().positions().deleteOpenPosition(symbol, null, null);

            log.info("Successfully closed position for symbol {} for user: {}", symbol, userId);

            return orderMapper.toOrderResponse(closedOrder);
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to close position for symbol {} for user: {}", symbol, userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to close position: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Closes a partial position.
     *
     * @param userId   User ID
     * @param symbol   Stock symbol to close
     * @param quantity Number of shares to close
     * @return Order response for the close order
     */
    public OrderResponse closePartialPosition(String userId, String symbol, BigDecimal quantity) {
        log.info("Closing {} shares of {} for user: {}", quantity, symbol, userId);
        
        // For partial closes, we need to use a sell order instead
        try {
            // Use OrderService to place a market sell order for the partial quantity
            TradingApiOrderRequest sellRequest = TradingApiOrderRequest.marketSell(symbol, quantity);
            return orderService.placeOrder(userId, sellRequest);
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to close partial position for {} for user: {}", symbol, userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to close partial position: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Async version of closePosition.
     *
     * @param userId User ID
     * @param symbol Stock symbol to close
     * @return CompletableFuture with order response
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<OrderResponse> closePositionAsync(String userId, String symbol) {
        return CompletableFuture.supplyAsync(() -> closePosition(userId, symbol));
    }

    /**
     * Closes all positions.
     *
     * @param userId       User ID
     * @param cancelOrders Whether to cancel open orders as well
     * @return List of order responses for the close orders
     */
    public List<OrderResponse> closeAllPositions(String userId, boolean cancelOrders) {
        log.info("Closing all positions for user: {}, cancelOrders: {}", userId, cancelOrders);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Close all positions using OpenAPI PositionsApi - returns PositionClosedReponse, need to extract orders
            List<net.jacobpeterson.alpaca.openapi.trader.model.PositionClosedReponse> responses = 
                    client.trader().positions().deleteAllOpenPositions(cancelOrders);
            // Extract orders from responses (body property contains the Order)
            List<net.jacobpeterson.alpaca.openapi.trader.model.Order> closedOrders = responses.stream()
                    .map(net.jacobpeterson.alpaca.openapi.trader.model.PositionClosedReponse::getBody)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            log.info("Successfully submitted close requests for {} positions for user: {}",
                    closedOrders.size(), userId);

            return closedOrders.stream()
                    .map(orderMapper::toOrderResponse)
                    .toList();
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to close all positions for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to close all positions: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Calculates total unrealized P/L across all positions.
     *
     * @param userId User ID
     * @return Total unrealized profit/loss as BigDecimal
     */
    public BigDecimal calculateTotalUnrealizedPnL(String userId) {
        return BigDecimal.valueOf(getTotalUnrealizedPnL(userId));
    }

    /**
     * Calculates total unrealized P/L across all positions.
     *
     * @param userId User ID
     * @return Total unrealized profit/loss
     */
    public double getTotalUnrealizedPnL(String userId) {
        return getAllPositions(userId).stream()
                .mapToDouble(p -> p.unrealizedPnL() != null ? p.unrealizedPnL().doubleValue() : 0.0)
                .sum();
    }

    /**
     * Calculates total portfolio value from all positions.
     *
     * @param userId User ID
     * @return Total portfolio value as BigDecimal
     */
    public BigDecimal calculateTotalPortfolioValue(String userId) {
        return getAllPositions(userId).stream()
                .map(p -> p.marketValue() != null ? p.marketValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Checks if user has any open positions.
     *
     * @param userId User ID
     * @return true if user has open positions
     */
    public boolean hasOpenPositions(String userId) {
        return !getAllPositions(userId).isEmpty();
    }

}
