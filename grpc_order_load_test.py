import argparse
import random
import time
import uuid
from concurrent.futures import ThreadPoolExecutor, as_completed

import grpc

import trading_engine_pb2 as pb2
import trading_engine_pb2_grpc as pb2_grpc


def build_request(index, user_id, symbol, quantity, client_prefix):
    return pb2.OrderRequest(
        user_id=user_id,
        symbol=symbol,
        quantity=quantity,
        side=pb2.OrderSide.BUY,
        type=pb2.OrderType.MARKET,
        time_in_force=pb2.TimeInForce.DAY,
        limit_price=0.0,
        stop_price=0.0,
        extended_hours=False,
        client_order_id=f"{client_prefix}-{index}-{uuid.uuid4().hex[:8]}",
    )


def main():
    parser = argparse.ArgumentParser(description="Load test TradingOrderService.PlaceOrder")
    parser.add_argument("--target", default="localhost:9092", help="gRPC target host:port")
    parser.add_argument("--count", type=int, default=150, help="Number of requests to send")
    parser.add_argument("--workers", type=int, default=50, help="Thread pool size")
    parser.add_argument("--duration", type=float, default=10.0, help="Total duration to spread requests (seconds)")
    parser.add_argument("--timeout", type=float, default=10.0, help="Per-request timeout (seconds)")
    parser.add_argument("--user-id", default="1", help="User id")
    parser.add_argument(
        "--symbols",
        default="AAPL,MSFT,NVDA,AMZN,GOOGL,META,TSLA,AMD,NFLX,INTC",
        help="Comma-separated symbols (IEX equities)",
    )
    parser.add_argument("--min-qty", type=float, default=0.01, help="Minimum quantity")
    parser.add_argument("--max-qty", type=float, default=0.04, help="Maximum quantity")
    parser.add_argument("--seed", type=int, default=None, help="Random seed")
    parser.add_argument("--client-prefix", default="load-test", help="Client order id prefix")
    args = parser.parse_args()

    if args.seed is not None:
        random.seed(args.seed)

    symbols = [s.strip() for s in args.symbols.split(",") if s.strip()]
    if not symbols:
        raise SystemExit("At least one symbol is required.")
    if args.min_qty <= 0 or args.max_qty <= 0 or args.min_qty > args.max_qty:
        raise SystemExit("Invalid quantity range. Ensure 0 < min-qty <= max-qty.")

    channel = grpc.insecure_channel(args.target)
    stub = pb2_grpc.TradingOrderServiceStub(channel)

    start = time.monotonic()
    interval = args.duration / max(args.count, 1)
    statuses = {}
    success = 0
    errors = 0

    def send_one(i):
        symbol = random.choice(symbols)
        quantity = round(random.uniform(args.min_qty, args.max_qty), 4)
        req = build_request(i, args.user_id, symbol, quantity, args.client_prefix)
        return stub.PlaceOrder(req, timeout=args.timeout)

    with ThreadPoolExecutor(max_workers=args.workers) as executor:
        futures = []
        for i in range(args.count):
            target = start + (i * interval)
            sleep_for = target - time.monotonic()
            if sleep_for > 0:
                time.sleep(sleep_for)
            futures.append(executor.submit(send_one, i))
        for future in as_completed(futures):
            try:
                resp = future.result()
                success += 1
                status_name = pb2.OrderStatus.Name(resp.status)
                statuses[status_name] = statuses.get(status_name, 0) + 1
            except grpc.RpcError as exc:
                errors += 1
                statuses["RPC_ERROR"] = statuses.get("RPC_ERROR", 0) + 1
                print(f"RPC error: {exc.code()} {exc.details()}")

    elapsed = time.monotonic() - start
    print(f"Sent: {args.count}, Success: {success}, Errors: {errors}, Time: {elapsed:.2f}s")
    for name in sorted(statuses.keys()):
        print(f"{name}: {statuses[name]}")


if __name__ == "__main__":
    main()
