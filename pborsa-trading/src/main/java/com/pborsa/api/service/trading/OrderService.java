package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.api.domain.dto.trading.*;
import com.pborsa.api.exception.AlpacaException;
import com.pborsa.api.service.alpaca.AlpacaClientFactory;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.trader.ApiException;
import net.jacobpeterson.alpaca.openapi.trader.model.Order;
import net.jacobpeterson.alpaca.openapi.trader.model.PostOrderRequest;
import net.jacobpeterson.alpaca.openapi.trader.model.OrderSide;
import net.jacobpeterson.alpaca.openapi.trader.model.OrderType;
import net.jacobpeterson.alpaca.openapi.trader.model.TimeInForce;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.pborsa.api.domain.dto.trading.OrderResponse.buildRejectedOrderResponse;

/**
 * Service for handling order operations.
 * Supports market, limit, stop, and stop-limit orders.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final AlpacaClientFactory clientFactory;
    private final UserCredentialsService credentialsService;
    private final OrderMapper orderMapper;

    /**
     * Places a new order.
     *
     * @param userId  User ID
     * @param request Order request details
     * @return Order response with order details
     */
    public OrderResponse placeOrder(Long userId, TradingApiOrderRequest request) {
        log.info("Placing {} {} order for {} shares of {} for user: {}",
                request.type(), request.side(), request.quantity(), request.symbol(), userId);

        String clientOrderId = request.clientOrderId() != null
                ? request.clientOrderId()
                : UUID.randomUUID().toString();

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);

            // Convert domain enums to SDK enums
            OrderSide alpacaSide = convertOrderSide(request.side());
            OrderType alpacaType = convertOrderType(request.type());
            TimeInForce alpacaTif = convertTimeInForce(request.timeInForce());

            // Build PostOrderRequest
            PostOrderRequest postOrderRequest = new PostOrderRequest()
                    .symbol(request.symbol())
                    .qty(request.quantity().toString())
                    .side(alpacaSide)
                    .type(alpacaType)
                    .timeInForce(alpacaTif)
                    .extendedHours(request.extendedHours() != null && request.extendedHours())
                    .clientOrderId(clientOrderId);

            // Set optional prices
            if (request.limitPrice() != null) {
                postOrderRequest.limitPrice(request.limitPrice().toString());
            }
            if (request.stopPrice() != null) {
                postOrderRequest.stopPrice(request.stopPrice().toString());
            }

            // Place order using OpenAPI OrdersApi
            Order order = client.trader().orders().postOrder(postOrderRequest);

            log.info("Successfully placed order for {} shares of {} for user: {}",
                    request.quantity(), request.symbol(), userId);

            return orderMapper.toOrderResponse(order);
        } catch (ApiException e) {
            if (isInsufficientFunds(e)) {
                log.warn("Order rejected due to insufficient funds for user {} symbol {}: {}",
                        userId, request.symbol(), e.getMessage());
                return buildRejectedOrderResponse(request, clientOrderId, extractOrderErrorMessage(e));
            }
            log.error("Failed to place order for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.ORDER_FAILED,
                    "Failed to place order: " + e.getMessage(),
                    e
            );
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to place order for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.ORDER_FAILED,
                    "Failed to place order: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Async version of placeOrder.
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<OrderResponse> placeOrderAsync(Long userId, TradingApiOrderRequest request) {
        return CompletableFuture.supplyAsync(() -> placeOrder(userId, request));
    }

    /**
     * Places a market buy order.
     */
    public OrderResponse marketBuy(Long userId, String symbol, BigDecimal quantity) {
        TradingApiOrderRequest request = TradingApiOrderRequest.marketBuy(symbol, quantity);
        return placeOrder(userId, request);
    }

    /**
     * Places a market sell order.
     */
    public OrderResponse marketSell(Long userId, String symbol, BigDecimal quantity) {
        TradingApiOrderRequest request = TradingApiOrderRequest.marketSell(symbol, quantity);
        return placeOrder(userId, request);
    }

    /**
     * Places a limit buy order.
     */
    public OrderResponse limitBuy(Long userId, String symbol, BigDecimal quantity, BigDecimal limitPrice) {
        TradingApiOrderRequest request = TradingApiOrderRequest.limitBuy(symbol, quantity, limitPrice);
        return placeOrder(userId, request);
    }

    /**
     * Places a limit sell order.
     */
    public OrderResponse limitSell(Long userId, String symbol, BigDecimal quantity, BigDecimal limitPrice) {
        TradingApiOrderRequest request = TradingApiOrderRequest.limitSell(symbol, quantity, limitPrice);
        return placeOrder(userId, request);
    }

    /**
     * Gets all orders for a user with optional filters.
     */
    public List<OrderResponse> getOrders(Long userId, String status, Integer limit, 
                                          ZonedDateTime after, ZonedDateTime until, boolean nested) {
        log.debug("Fetching orders for user: {}", userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Get orders using OpenAPI OrdersApi - signature: (status, limit, after, until, direction, nested, symbols, side)
            List<Order> orders = client.trader().orders().getAllOrders(
                    status != null ? status : null,
                    limit,
                    after != null ? after.toString() : null,
                    until != null ? until.toString() : null,
                    null, // direction
                    nested,
                    null, // symbols
                    null  // side
            );

            return orders.stream()
                    .map(orderMapper::toOrderResponse)
                    .toList();
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch orders for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to fetch orders: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Gets open orders for a user.
     */
    public List<OrderResponse> getOpenOrders(Long userId) {
        return getOrders(userId, "open", null, null, null, false);
    }

    /**
     * Async version of getOrders.
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<List<OrderResponse>> getOrdersAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> getOrders(userId, null, null, null, null, false));
    }

    /**
     * Gets a specific order by ID.
     */
    public OrderResponse getOrder(Long userId, String orderId) {
        log.debug("Fetching order {} for user: {}", orderId, userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Get order by ID using OpenAPI OrdersApi
            Order order = client.trader().orders().getOrderByOrderID(UUID.fromString(orderId), false);

            return orderMapper.toOrderResponse(order);
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch order {} for user: {}", orderId, userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.ORDER_NOT_FOUND,
                    "Failed to fetch order: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Cancels an order.
     *
     * @return true if cancellation was successful
     */
    public boolean cancelOrder(Long userId, String orderId) {
        log.info("Canceling order {} for user: {}", orderId, userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Cancel order using OpenAPI OrdersApi
            client.trader().orders().deleteOrderByOrderID(UUID.fromString(orderId));

            log.info("Successfully canceled order {} for user: {}", orderId, userId);
            return true;
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to cancel order {} for user: {}", orderId, userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to cancel order: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Async version of cancelOrder.
     */
    @Async("alpacaAsyncExecutor")
    public CompletableFuture<Boolean> cancelOrderAsync(Long userId, String orderId) {
        return CompletableFuture.supplyAsync(() -> cancelOrder(userId, orderId));
    }

    /**
     * Cancels all orders.
     *
     * @return number of orders canceled
     */
    public int cancelAllOrders(Long userId) {
        log.info("Canceling all orders for user: {}", userId);

        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            
            // Cancel all orders using OpenAPI OrdersApi - returns CanceledOrderResponse list (only has id and status)
            List<net.jacobpeterson.alpaca.openapi.trader.model.CanceledOrderResponse> canceledResponses = 
                    client.trader().orders().deleteAllOrders();
            // CanceledOrderResponse doesn't contain Order objects, so fetch them by ID
            List<Order> canceled = canceledResponses.stream()
                    .map(response -> {
                        try {
                            return client.trader().orders().getOrderByOrderID(response.getId(), false);
                        } catch (Exception e) {
                            log.warn("Failed to fetch canceled order {}: {}", response.getId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();

            log.info("Successfully canceled {} orders for user: {}", canceled.size(), userId);
            return canceled.size();
        } catch (AlpacaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to cancel all orders for user: {}", userId, e);
            throw new AlpacaException(
                    AlpacaException.ErrorCode.API_ERROR,
                    "Failed to cancel all orders: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Converts our OrderSide to Alpaca's OrderSide enum.
     */
    private OrderSide convertOrderSide(com.pborsa.api.domain.dto.trading.OrderSide side) {
        if (side == null) {
            return null;
        }
        return OrderSide.valueOf(side.name());
    }

    /**
     * Converts our OrderType to Alpaca's OrderType enum.
     */
    private OrderType convertOrderType(com.pborsa.api.domain.dto.trading.OrderType type) {
        if (type == null) {
            return null;
        }
        return OrderType.valueOf(type.name());
    }

    /**
     * Converts our TimeInForce to Alpaca's TimeInForce enum.
     */
    private TimeInForce convertTimeInForce(com.pborsa.api.domain.dto.trading.TimeInForce tif) {
        if (tif == null) {
            return null;
        }
        return TimeInForce.valueOf(tif.name());
    }

    private boolean isInsufficientFunds(ApiException exception) {
        if (exception.getCode() == 403) {
            return true;
        }
        String body = exception.getResponseBody();
        if (body != null) {
            String normalized = body.toLowerCase();
            if (normalized.contains("insufficient") || normalized.contains("buying power")) {
                return true;
            }
        }
        String message = exception.getMessage();
        if (message == null) {
            return false;
        }
        String normalizedMessage = message.toLowerCase();
        return normalizedMessage.contains("insufficient") || normalizedMessage.contains("buying power");
    }

    private String extractOrderErrorMessage(ApiException exception) {
        String body = exception.getResponseBody();
        if (body != null && !body.isBlank()) {
            return body;
        }
        return exception.getMessage();
    }

}
