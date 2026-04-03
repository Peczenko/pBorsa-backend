package com.pborsa.api.order.grpc;

import com.pborsa.api.order.service.OrderExecutionResult;
import com.pborsa.api.order.service.OrderExecutionService;
import com.pborsa.api.tradingengine.v1.TradingOrderServiceGrpc;
import com.pborsa.domain.dto.trading.OrderExecutionRequest;
import com.pborsa.domain.dto.trading.TradingApiOrderRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradingOrderGrpcService extends TradingOrderServiceGrpc.TradingOrderServiceImplBase {

    private final OrderExecutionService orderExecutionService;

    @Override
    public void placeOrder(com.pborsa.api.tradingengine.v1.OrderRequest grpcRequest,
                           StreamObserver<com.pborsa.api.tradingengine.v1.OrderResponse> responseObserver) {
        try {
            String validationError = validateOrderRequest(grpcRequest);
            if (validationError != null) {
                log.warn("Order request validation failed for user {}: {}", grpcRequest.getUserId(), validationError);
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription(validationError)
                        .asRuntimeException());
                return;
            }

            TradingApiOrderRequest dto = mapToDto(grpcRequest);
            log.info("Received gRPC placeOrder request for user {}: {}", grpcRequest.getUserId(), dto);

            OrderExecutionResult result =
                    orderExecutionService.startExecution(OrderExecutionRequest.builder()
                            .userId(grpcRequest.getUserId())
                            .order(dto)
                            .build());

            responseObserver.onNext(mapResultProto(result));
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Failed to place order over gRPC for user {}", grpcRequest.getUserId(), e);
            responseObserver.onError(e);
        }
    }

    private String validateOrderRequest(com.pborsa.api.tradingengine.v1.OrderRequest request) {
        if (request.getClientOrderId() == null || request.getClientOrderId().isBlank()) {
            return "client_order_id is required and cannot be empty";
        }
        if (request.getStrategyId() <= 0) {
            return "strategy_id is required and must be a positive value";
        }
        return null;
    }

    private TradingApiOrderRequest mapToDto(com.pborsa.api.tradingengine.v1.OrderRequest req) {
        return TradingApiOrderRequest.builder()
                .symbol(req.getSymbol())
                .quantity(BigDecimal.valueOf(req.getQuantity()))
                .side(mapSide(req.getSide()))
                .type(mapType(req.getType()))
                .timeInForce(mapTif(req.getTimeInForce()))
                .limitPrice(req.getLimitPrice() != 0d ? BigDecimal.valueOf(req.getLimitPrice()) : null)
                .stopPrice(req.getStopPrice() != 0d ? BigDecimal.valueOf(req.getStopPrice()) : null)
                .extendedHours(req.getExtendedHours())
                .clientOrderId(req.getClientOrderId().isBlank() ? null : req.getClientOrderId())
                .userStrategyId(req.getStrategyId() > 0 ? req.getStrategyId() : null)
                .build();
    }

    private com.pborsa.api.tradingengine.v1.OrderResponse mapResultProto(
            OrderExecutionResult result) {
        com.pborsa.api.tradingengine.v1.OrderStatus status = result.accepted()
                ? com.pborsa.api.tradingengine.v1.OrderStatus.PENDING_NEW
                : com.pborsa.api.tradingengine.v1.OrderStatus.REJECTED;

        com.pborsa.api.tradingengine.v1.OrderResponse.Builder builder =
                com.pborsa.api.tradingengine.v1.OrderResponse.newBuilder()
                        .setStatus(status);

        if (result.workflowId() != null) {
            builder.setWorkflowId(result.workflowId());
        }
        return builder.build();
    }

    private com.pborsa.domain.dto.trading.OrderSide mapSide(com.pborsa.api.tradingengine.v1.OrderSide side) {
        return switch (side) {
            case BUY -> com.pborsa.domain.dto.trading.OrderSide.BUY;
            case SELL -> com.pborsa.domain.dto.trading.OrderSide.SELL;
            default -> com.pborsa.domain.dto.trading.OrderSide.BUY;
        };
    }

    private com.pborsa.domain.dto.trading.OrderType mapType(com.pborsa.api.tradingengine.v1.OrderType type) {
        return switch (type) {
            case LIMIT -> com.pborsa.domain.dto.trading.OrderType.LIMIT;
            case STOP -> com.pborsa.domain.dto.trading.OrderType.STOP;
            case STOP_LIMIT -> com.pborsa.domain.dto.trading.OrderType.STOP_LIMIT;
            case MARKET, ORDER_TYPE_UNSPECIFIED, UNRECOGNIZED -> com.pborsa.domain.dto.trading.OrderType.MARKET;
        };
    }

    private com.pborsa.domain.dto.trading.TimeInForce mapTif(com.pborsa.api.tradingengine.v1.TimeInForce tif) {
        return switch (tif) {
            case GTC -> com.pborsa.domain.dto.trading.TimeInForce.GTC;
            case OPG -> com.pborsa.domain.dto.trading.TimeInForce.OPG;
            case CLS -> com.pborsa.domain.dto.trading.TimeInForce.CLS;
            case IOC -> com.pborsa.domain.dto.trading.TimeInForce.IOC;
            case FOK -> com.pborsa.domain.dto.trading.TimeInForce.FOK;
            case DAY, TIF_UNSPECIFIED, UNRECOGNIZED -> com.pborsa.domain.dto.trading.TimeInForce.DAY;
        };
    }

}
