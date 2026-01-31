#!/usr/bin/env python3
"""
Sample trading engine gRPC server:
- ExecuteStrategy: receives header + bar batches (OHLCV)
- SendLiveBars: receives periodic live bar updates from pBorsa API
- NotifyOrderStatusUpdate: receives order status updates from pBorsa API
- ExecuteBacktest: runs backtest against historical data and returns simulated orders
"""

import argparse
import logging
import random
from concurrent import futures
from pathlib import Path
from datetime import datetime, timezone
import json
import threading

import grpc
from google.protobuf.json_format import MessageToDict
from google.protobuf.timestamp_pb2 import Timestamp

import trading_engine_pb2 as pb2
import trading_engine_pb2_grpc as pb2_grpc


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(name)s - %(message)s",
)
logger = logging.getLogger("trading-engine-server")


def utc_now_compact() -> str:
    return datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")


def pb_to_dict(msg) -> dict:
    """
    Version-safe protobuf -> dict.
    (Some protobuf versions don't support including_default_value_fields, etc.)
    """
    try:
        # Newer protobuf versions
        return MessageToDict(
            msg,
            preserving_proto_field_name=True,
            including_default_value_fields=False,
            use_integers_for_enums=False,
        )
    except TypeError:
        # Older protobuf versions (no including_default_value_fields)
        return MessageToDict(
            msg,
            preserving_proto_field_name=True,
            use_integers_for_enums=False,
        )


def timestamp_from_datetime(dt: datetime) -> Timestamp:
    """Convert datetime to protobuf Timestamp."""
    ts = Timestamp()
    ts.FromDatetime(dt)
    return ts


class TradingEngineService(pb2_grpc.TradingEngineServiceServicer):
    def __init__(self, out_dir: Path):
        self.out_dir = out_dir
        self.out_dir.mkdir(parents=True, exist_ok=True)
        self._live_lock = threading.Lock()

    def ExecuteStrategy(self, request_iterator, context):
        header = None
        total_bars = 0

        # We'll store all bars then write a single JSON file at the end.
        # If you expect huge datasets, switch to NDJSON streaming (see note below).
        output = {
            "header": None,
            "bars": [],
            "meta": {
                "saved_at_utc": datetime.now(timezone.utc).isoformat(),
            },
        }

        for chunk in request_iterator:
            if chunk.HasField("header"):
                header = chunk.header
                output["header"] = pb_to_dict(header)

                logger.info(
                    "Received strategy execution header: executionId=%s userId=%d strategyId=%d symbol=%s",
                    header.execution_id,
                    header.user_id,
                    header.strategy_id,
                    header.symbol,
                )

            elif chunk.HasField("bar_batch"):
                bars = chunk.bar_batch.bars
                total_bars += len(bars)

                # Convert each bar protobuf message to dict and append
                output["bars"].extend(pb_to_dict(b) for b in bars)

                logger.info("Received batch of %d bars (total=%d)", len(bars), total_bars)

        msg = f"Received {total_bars} bars" + (f" for {header.execution_id}" if header else "")
        logger.info("ExecuteStrategy completed: %s", msg)

        # Save to JSON
        exec_id = header.execution_id if header else "unknown_execution"
        filename = f"strategy_{exec_id}_{utc_now_compact()}.json"
        path = self.out_dir / filename
        with path.open("w", encoding="utf-8") as f:
            json.dump(output, f, ensure_ascii=False, indent=2)

        logger.info("Saved strategy bars to %s", path)

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

        for symbol_bar in bars:
            bar = symbol_bar.bar
            logger.debug(
                "  %s: O=%.2f H=%.2f L=%.2f C=%.2f V=%d",
                symbol_bar.symbol,
                bar.open,
                bar.high,
                bar.low,
                bar.close,
                bar.volume,
            )

        # Append as NDJSON (one update per line)
        live_record = {
            "update_time": update_time,
            "strategy_ids": strategy_ids,
            "bars": [pb_to_dict(sb) for sb in bars],  # includes symbol + bar fields
            "saved_at_utc": datetime.now(timezone.utc).isoformat(),
        }

        day = datetime.now(timezone.utc).strftime("%Y-%m-%d")
        live_path = self.out_dir / f"live_bars_{day}.jsonl"

        with self._live_lock:
            with live_path.open("a", encoding="utf-8") as f:
                f.write(json.dumps(live_record, ensure_ascii=False) + "\n")

        logger.info("Appended live bars update to %s", live_path)

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

    def NotifyStrategyStatusUpdate(self, request, context):
        """Handle strategy status updates from pBorsa API."""
        strategy_id = request.strategy_id
        user_id = request.user_id
        symbol = request.symbol
        old_status = pb2.StrategyStatus.Name(request.old_status)
        new_status = pb2.StrategyStatus.Name(request.new_status)

        logger.info(
            "Strategy status update received - strategyId=%d userId=%d symbol=%s: %s -> %s",
            strategy_id,
            user_id,
            symbol,
            old_status,
            new_status,
        )

        return pb2.StrategyStatusNotificationAck(
            received=True,
            message="Strategy status update processed",
        )

    def ExecuteBacktest(self, request, context):
        """
        Execute a backtest against historical data and return simulated orders.
        This is a sample implementation that generates random trades for testing.
        """
        backtest_id = request.backtest_id
        user_id = request.user_id
        symbol = request.symbol
        strategy_code = request.base_strategy_code
        budget = request.budget

        history_before = list(request.history_before_start)
        history_testing = list(request.history_testing_range)

        logger.info(
            "ExecuteBacktest received: backtestId=%s userId=%d symbol=%s strategy=%s budget=%.2f",
            backtest_id,
            user_id,
            symbol,
            strategy_code,
            budget,
        )
        logger.info(
            "  History before start: %d bars, Testing range: %d bars",
            len(history_before),
            len(history_testing),
        )

        # Generate sample orders based on the testing data
        orders, pnl, max_drawdown, winning_trades = self._simulate_backtest(
            symbol=symbol,
            strategy_code=strategy_code,
            budget=budget,
            testing_bars=history_testing,
        )

        total_trades = len(orders)

        logger.info(
            "Backtest %s completed: %d trades, PnL=%.2f, MaxDrawdown=%.2f, WinRate=%.1f%%",
            backtest_id,
            total_trades,
            pnl,
            max_drawdown,
            (winning_trades / total_trades * 100) if total_trades > 0 else 0,
        )

        # Save backtest results to JSON
        output = {
            "backtest_id": backtest_id,
            "user_id": user_id,
            "symbol": symbol,
            "strategy_code": strategy_code,
            "budget": budget,
            "history_before_count": len(history_before),
            "history_testing_count": len(history_testing),
            "results": {
                "pnl": pnl,
                "max_drawdown": max_drawdown,
                "total_trades": total_trades,
                "winning_trades": winning_trades,
            },
            "orders": [
                {
                    "symbol": o.symbol,
                    "side": pb2.OrderSide.Name(o.side),
                    "quantity": o.quantity,
                    "price": o.price,
                    "executed_at": o.executed_at.ToDatetime().isoformat() if o.executed_at else None,
                }
                for o in orders
            ],
            "saved_at_utc": datetime.now(timezone.utc).isoformat(),
        }

        filename = f"backtest_{backtest_id}_{utc_now_compact()}.json"
        path = self.out_dir / filename
        with path.open("w", encoding="utf-8") as f:
            json.dump(output, f, ensure_ascii=False, indent=2)

        logger.info("Saved backtest results to %s", path)

        return pb2.BacktestResponse(
            backtest_id=backtest_id,
            success=True,
            message=f"Backtest completed with {total_trades} trades",
            orders=orders,
            pnl=pnl,
            max_drawdown=max_drawdown,
            total_trades=total_trades,
            winning_trades=winning_trades,
        )

    def _simulate_backtest(
        self,
        symbol: str,
        strategy_code: str,
        budget: float,
        testing_bars: list,
    ) -> tuple:
        """
        Simulate a simple backtest strategy.
        Returns: (orders, pnl, max_drawdown, winning_trades)

        This is a sample implementation that:
        - Generates trades at random intervals
        - Uses actual bar prices from the testing data
        - Calculates realistic P&L based on trades
        """
        orders = []

        if not testing_bars:
            return orders, 0.0, 0.0, 0

        # Strategy parameters based on strategy code
        trade_frequency = {
            "MOMENTUM_V1": 0.02,      # ~2% of bars trigger trades
            "MEAN_REVERSION_V1": 0.03, # ~3% of bars trigger trades
            "BREAKOUT_V1": 0.015,      # ~1.5% of bars trigger trades
        }.get(strategy_code, 0.02)

        # Calculate position size based on budget
        # Use first bar's close price as reference
        first_bar = testing_bars[0]
        ref_price = first_bar.close if first_bar.close > 0 else 100.0
        max_shares = int(budget / ref_price)
        shares_per_trade = max(1, max_shares // 10)  # Trade 10% of max position

        # Track position and P&L
        position = 0  # Current shares held
        cash = budget
        peak_value = budget
        max_drawdown = 0.0
        trade_pnls = []  # Track individual trade P&Ls
        entry_price = 0.0

        # Random seed based on backtest parameters for reproducibility
        random.seed(hash(f"{symbol}_{strategy_code}_{budget}"))

        for i, bar in enumerate(testing_bars):
            bar_time = bar.timestamp.ToDatetime() if bar.timestamp else datetime.now(timezone.utc)
            price = bar.close if bar.close > 0 else ref_price

            # Decide whether to trade on this bar
            if random.random() < trade_frequency:
                if position == 0:
                    # Open a long position (BUY)
                    qty = min(shares_per_trade, int(cash / price))
                    if qty > 0:
                        entry_price = price
                        position = qty
                        cash -= qty * price

                        ts = Timestamp()
                        ts.CopyFrom(bar.timestamp)

                        orders.append(pb2.BacktestOrder(
                            symbol=symbol,
                            quantity=float(qty),
                            side=pb2.OrderSide.BUY,
                            price=price,
                            executed_at=ts,
                        ))

                        logger.debug("  BUY %d @ %.2f at %s", qty, price, bar_time)

                elif position > 0:
                    # Close the position (SELL)
                    sell_price = price
                    trade_pnl = (sell_price - entry_price) * position
                    trade_pnls.append(trade_pnl)

                    cash += position * sell_price

                    ts = Timestamp()
                    ts.CopyFrom(bar.timestamp)

                    orders.append(pb2.BacktestOrder(
                        symbol=symbol,
                        quantity=float(position),
                        side=pb2.OrderSide.SELL,
                        price=sell_price,
                        executed_at=ts,
                    ))

                    logger.debug("  SELL %d @ %.2f at %s (PnL: %.2f)", position, sell_price, bar_time, trade_pnl)

                    position = 0
                    entry_price = 0.0

            # Calculate current portfolio value and drawdown
            current_value = cash + (position * price)
            if current_value > peak_value:
                peak_value = current_value
            drawdown = (peak_value - current_value) / peak_value if peak_value > 0 else 0
            max_drawdown = max(max_drawdown, drawdown)

        # Close any remaining position at last bar price
        if position > 0 and testing_bars:
            last_bar = testing_bars[-1]
            last_price = last_bar.close if last_bar.close > 0 else ref_price
            trade_pnl = (last_price - entry_price) * position
            trade_pnls.append(trade_pnl)
            cash += position * last_price

            ts = Timestamp()
            ts.CopyFrom(last_bar.timestamp)

            orders.append(pb2.BacktestOrder(
                symbol=symbol,
                quantity=float(position),
                side=pb2.OrderSide.SELL,
                price=last_price,
                executed_at=ts,
            ))

            logger.debug("  SELL (close) %d @ %.2f (PnL: %.2f)", position, last_price, trade_pnl)

        # Calculate final metrics
        total_pnl = cash - budget
        winning_trades = sum(1 for pnl in trade_pnls if pnl > 0)

        return orders, total_pnl, max_drawdown, winning_trades


def serve(port: int, out_dir: Path) -> None:
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pb2_grpc.add_TradingEngineServiceServicer_to_server(
        TradingEngineService(out_dir=out_dir),
        server,
    )

    server.add_insecure_port(f"0.0.0.0:{port}")
    server.start()

    logger.info("Trading engine gRPC server listening on 0.0.0.0:%d", port)
    logger.info("Saving JSON output to: %s", out_dir.resolve())
    logger.info("Available RPCs:")
    logger.info("  - ExecuteStrategy: streaming historical data for live strategies")
    logger.info("  - SendLiveBars: periodic live bar updates")
    logger.info("  - NotifyOrderStatusUpdate: order status notifications")
    logger.info("  - NotifyStrategyStatusUpdate: strategy status notifications")
    logger.info("  - ExecuteBacktest: run backtest against historical data")

    server.wait_for_termination()


def main() -> None:
    parser = argparse.ArgumentParser(description="Sample trading engine gRPC server")
    parser.add_argument("--port", type=int, default=9090, help="Port to listen on (default: 9090)")
    parser.add_argument(
        "--out-dir",
        type=Path,
        default=Path("./bar_dumps"),
        help="Directory for saved JSON/NDJSON files (default: ./bar_dumps)",
    )
    args = parser.parse_args()
    serve(args.port, args.out_dir)


if __name__ == "__main__":
    main()
