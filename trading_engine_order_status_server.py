#!/usr/bin/env python3
"""
Example trading engine gRPC server that receives order status updates from pBorsa API.

The API will call NotifyOrderStatusUpdate when an order status changes.
The trading engine should implement this RPC method to receive updates.

Usage:
    python trading_engine_order_status_server.py [--port 9090]
"""

import argparse
import logging
from concurrent import futures

import grpc

import trading_engine_pb2 as pb2
import trading_engine_pb2_grpc as pb2_grpc

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class TradingEngineService(pb2_grpc.TradingEngineServiceServicer):
    """Trading engine service implementation."""

    def ExecuteStrategy(self, request_iterator, context):
        """Existing method for strategy execution (unchanged)."""
        header = None
        total_trades = 0

        for chunk in request_iterator:
            if chunk.HasField("header"):
                header = chunk.header
                logger.info("Received strategy execution header: %s", header.execution_id)

            elif chunk.HasField("trade_batch"):
                trades = chunk.trade_batch.trades
                total_trades += len(trades)
                logger.info("Received batch of %d trades (total=%d)", len(trades), total_trades)

        msg = f"Received {total_trades} trades" + (f" for {header.execution_id}" if header else "")
        return pb2.ExecutionAck(accepted=True, message=msg, execution_id=header.execution_id if header else "")

    def NotifyOrderStatusUpdate(self, request, context):
        """
        Called by pBorsa API when an order status changes.
        
        The API sends the client_order_id in the request, so the trading engine
        can identify which order this update is for.
        
        Args:
            request: OrderStatusUpdate message containing order details and status
            context: gRPC context
            
        Returns:
            OrderStatusUpdateAck to acknowledge receipt
        """
        client_order_id = request.client_order_id
        order_id = request.order_id
        status = pb2.OrderStatus.Name(request.status)
        
        logger.info(
            "Order status update received - clientOrderId=%s orderId=%s status=%s",
            client_order_id, order_id, status
        )
        
        if request.message:
            logger.info("  Message: %s", request.message)
        
        if request.reason != pb2.OrderStatusReason.ORDER_STATUS_REASON_UNSPECIFIED:
            logger.info("  Reason: %s", pb2.OrderStatusReason.Name(request.reason))
        
        # Here you would:
        # 1. Look up the order by client_order_id in your system
        # 2. Update the order status
        # 3. Trigger any necessary actions (e.g., update UI, trigger callbacks, etc.)
        
        # Example: Update order in your system
        # order = find_order_by_client_id(client_order_id)
        # if order:
        #     order.status = status
        #     order.save()
        #     notify_order_status_change(order)
        
        return pb2.OrderStatusUpdateAck(received=True, message="Order status update processed")


def serve(port: int):
    """Start the gRPC server."""
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pb2_grpc.add_TradingEngineServiceServicer_to_server(TradingEngineService(), server)
    server.add_insecure_port(f"0.0.0.0:{port}")
    server.start()
    logger.info("Trading engine gRPC server listening on 0.0.0.0:%d", port)
    logger.info("Ready to receive order status updates from pBorsa API")
    server.wait_for_termination()


def main():
    parser = argparse.ArgumentParser(
        description="Trading engine gRPC server for receiving order status updates"
    )
    parser.add_argument(
        "--port",
        type=int,
        default=9090,
        help="Port to listen on (default: 9090)"
    )
    
    args = parser.parse_args()
    serve(args.port)


if __name__ == "__main__":
    main()

