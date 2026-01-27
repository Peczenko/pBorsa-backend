#!/usr/bin/env python3
"""
Sample trading engine gRPC server:
- ExecuteStrategy: receives header + quote batches, saves header to JSON and quotes to JSONL
- NotifyOrderStatusUpdate: receives order status updates from pBorsa API

Usage:
  python sample_trading_engine.py
  python sample_trading_engine.py --port 9090
"""

import argparse
import json
import logging
from concurrent import futures

import grpc

import trading_engine_pb2 as pb2
import trading_engine_pb2_grpc as pb2_grpc


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(name)s - %(message)s",
)
logger = logging.getLogger("trading-engine-server")

HEADER_PATH = "received_header.json"
QUOTES_PATH = "received_quotes.jsonl"


class TradingEngineService(pb2_grpc.TradingEngineServiceServicer):
    def ExecuteStrategy(self, request_iterator, context):
        header = None
        total_quotes = 0

        for chunk in request_iterator:
            if chunk.HasField("header"):
                header = chunk.header

                header_obj = {
                    "executionId": header.execution_id,
                    "userId": header.user_id,
                    "strategyId": header.strategy_id,
                    "symbol": header.symbol,
                    "timeframe": header.timeframe,
                    "start": header.start.ToDatetime().isoformat(),
                    "end": header.end.ToDatetime().isoformat(),
                }

                with open(HEADER_PATH, "w", encoding="utf-8") as hf:
                    json.dump(header_obj, hf, indent=2)

                logger.info("Received strategy execution header: %s", header.execution_id)
                logger.info("Header saved to %s", HEADER_PATH)

            elif chunk.HasField("quote_batch"):
                quotes = chunk.quote_batch.quotes
                total_quotes += len(quotes)

                logger.info(
                    "Received batch of %d quotes (total=%d)", len(quotes), total_quotes
                )

                with open(QUOTES_PATH, "a", encoding="utf-8") as qf:
                    for quote in quotes:
                        qf.write(
                            json.dumps(
                                {
                                    "executionId": header.execution_id if header else "",
                                    "symbol": header.symbol if header else "",
                                    "timestamp": quote.timestamp.ToDatetime().isoformat(),
                                    "bidPrice": quote.bid_price,
                                    "bidSize": quote.bid_size,
                                    "askPrice": quote.ask_price,
                                    "askSize": quote.ask_size,
                                    "bidExchange": quote.bid_exchange,
                                    "askExchange": quote.ask_exchange,
                                    "tape": quote.tape,
                                    "conditions": quote.conditions,
                                }
                            )
                            + "\n"
                        )

        msg = f"Received {total_quotes} quotes" + (
            f" for {header.execution_id}" if header else ""
        )
        return pb2.ExecutionAck(
            accepted=True,
            message=msg,
            execution_id=header.execution_id if header else "",
        )

    def NotifyOrderStatusUpdate(self, request, context):
        client_order_id = request.client_order_id
        order_id = request.order_id
        status = pb2.OrderStatus.Name(request.status)

        logger.info(
            "Order status update received - clientOrderId=%s orderId=%s status=%s",
            client_order_id,
            order_id,
            status,
        )

        if getattr(request, "message", ""):
            logger.info("  Message: %s", request.message)

        if request.reason != pb2.OrderStatusReason.ORDER_STATUS_REASON_UNSPECIFIED:
            logger.info("  Reason: %s", pb2.OrderStatusReason.Name(request.reason))

        return pb2.OrderStatusUpdateAck(
            received=True,
            message="Order status update processed",
        )


def serve(port: int) -> None:
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pb2_grpc.add_TradingEngineServiceServicer_to_server(TradingEngineService(), server)

    server.add_insecure_port(f"0.0.0.0:{port}")
    server.start()

    logger.info("Trading engine gRPC server listening on 0.0.0.0:%d", port)
    logger.info("ExecuteStrategy + NotifyOrderStatusUpdate are ready")

    server.wait_for_termination()


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Sample trading engine gRPC server"
    )
    parser.add_argument(
        "--port",
        type=int,
        default=9090,
        help="Port to listen on (default: 9090)",
    )
    args = parser.parse_args()
    serve(args.port)


if __name__ == "__main__":
    main()
