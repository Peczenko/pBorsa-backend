# mock_trading_engine_server.py
import json
import logging
from concurrent import futures
import grpc

import trading_engine_pb2 as pb2
import trading_engine_pb2_grpc as pb2_grpc

logging.basicConfig(level=logging.INFO)

HEADER_PATH = "received_header.json"
TRADES_PATH = "received_trades_test.jsonl"

class TradingEngineService(pb2_grpc.TradingEngineServiceServicer):
    def ExecuteStrategy(self, request_iterator, context):
        header = None
        total_trades = 0

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
                logging.info("Header saved to %s", HEADER_PATH)

            elif chunk.HasField("trade_batch"):
                trades = chunk.trade_batch.trades
                total_trades += len(trades)
                logging.info("Received batch of %d trades (total=%d)", len(trades), total_trades)
                with open(TRADES_PATH, "a", encoding="utf-8") as tf:
                    for trade in trades:
                        tf.write(json.dumps({
                            "executionId": header.execution_id if header else "",
                            "symbol": header.symbol if header else "",
                            "timestamp": trade.timestamp.ToDatetime().isoformat(),
                            "price": trade.price,
                            "size": trade.size,
                            "exchange": trade.exchange,
                            "tradeId": trade.trade_id,
                            "tape": trade.tape,
                            "conditions": trade.conditions,
                        }) + "\n")

        msg = f"Received {total_trades} trades" + (f" for {header.execution_id}" if header else "")
        return pb2.ExecutionAck(accepted=True, message=msg, execution_id=header.execution_id if header else "")

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=4))
    pb2_grpc.add_TradingEngineServiceServicer_to_server(TradingEngineService(), server)
    server.add_insecure_port("0.0.0.0:9090")  # adjust if needed
    server.start()
    logging.info("Mock trading engine gRPC server listening on 0.0.0.0:9090")
    server.wait_for_termination()

if __name__ == "__main__":
    serve()
