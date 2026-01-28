#!/usr/bin/env python3
"""
Sample trading engine gRPC server:
- ExecuteStrategy: receives header + bar batches (OHLCV), saves header to JSON and bars to JSONL
- SendLiveBars: receives periodic live bar updates from pBorsa API
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
BARS_PATH = "received_bars.jsonl"
LIVE_BARS_PATH = "received_live_bars.jsonl"


class TradingEngineService(pb2_grpc.TradingEngineServiceServicer):
    def ExecuteStrategy(self, request_iterator, context):
        header = None
        total_bars = 0

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

            elif chunk.HasField("bar_batch"):
                bars = chunk.bar_batch.bars
                total_bars += len(bars)

                logger.info(
                    "Received batch of %d bars (total=%d)", len(bars), total_bars
                )

                with open(BARS_PATH, "a", encoding="utf-8") as bf:
                    for bar in bars:
                        bf.write(
                            json.dumps(
                                {
                                    "executionId": header.execution_id if header else "",
                                    "symbol": header.symbol if header else "",
                                    "timestamp": bar.timestamp.ToDatetime().isoformat(),
                                    "open": bar.open,
                                    "high": bar.high,
                                    "low": bar.low,
                                    "close": bar.close,
                                    "volume": bar.volume,
                                    "tradeCount": bar.trade_count,
                                    "vwap": bar.vwap,
                                }
                            )
                            + "\n"
                        )

        msg = f"Received {total_bars} bars" + (
            f" for {header.execution_id}" if header else ""
        )
        return pb2.ExecutionAck(
            accepted=True,
            message=msg,
            execution_id=header.execution_id if header else "",
        )

    def SendLiveBars(self, request, context):
        """Handle periodic live bar updates from pBorsa API."""
        update_time = request.update_time.ToDatetime().isoformat()
        bars = request.bars
        strategy_ids = list(request.strategy_ids)

        logger.info(
            "Received live bar update: %d bars for %d strategies at %s",
            len(bars),
            len(strategy_ids),
            update_time,
        )

        with open(LIVE_BARS_PATH, "a", encoding="utf-8") as lf:
            for symbol_bar in bars:
                bar = symbol_bar.bar
                lf.write(
                    json.dumps(
                        {
                            "updateTime": update_time,
                            "symbol": symbol_bar.symbol,
                            "timeframe": symbol_bar.timeframe,
                            "strategyIds": strategy_ids,
                            "timestamp": bar.timestamp.ToDatetime().isoformat(),
                            "open": bar.open,
                            "high": bar.high,
                            "low": bar.low,
                            "close": bar.close,
                            "volume": bar.volume,
                            "tradeCount": bar.trade_count,
                            "vwap": bar.vwap,
                        }
                    )
                    + "\n"
                )

        return pb2.LiveBarUpdateAck(
            received=True,
            message=f"Processed {len(bars)} live bars",
            bars_processed=len(bars),
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
    logger.info("ExecuteStrategy + SendLiveBars + NotifyOrderStatusUpdate are ready")

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
