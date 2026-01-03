# pBorsa Backend

Monorepo for the pBorsa trading backend (Spring Boot + Temporal + gRPC). The API module exposes REST + WebSocket endpoints, runs the Temporal workers for strategy/data streaming, and talks to Alpaca and the trading engine.

## Modules
- `pborsa-api` – main Spring Boot application (REST, WebSocket, Temporal workers for strategy/data streaming).
- `pborsa-domain` – shared DTOs/entities.
- `pborsa-temporal-api` – Temporal workflow/activity contracts.
- `pborsa-trading` – shared trading services (no workers now).
- `pborsa-trading-worker` – Temporal workers for trading/market-data workflows.

## Prerequisites
- JDK 21
- Docker (for Postgres + Temporal via `compose.yaml`)
- Gradle wrapper (use `./gradlew` / `gradlew.bat`)

## Quick start
1) Start infrastructure (Postgres + Temporal):
   ```bash
   docker compose up -d
   ```
2) Build everything:
   ```bash
   ./gradlew clean build
   ```
3) Run the API (hosts REST, WebSocket, and strategy/data-stream Temporal worker):
   ```bash
   ./gradlew :pborsa-api:bootRun
   ```
   API defaults: port `8081`, Temporal at `localhost:7233`, trading-engine gRPC at `localhost:9090` (see `pborsa-api/src/main/resources/application.properties`).

4) (Optional) Run the trading worker (executes trading/market-data workflows):
   ```bash
   ./gradlew :pborsa-trading-worker:bootRun
   ```

## API docs (Swagger)
After starting `pborsa-api`, open:
- Swagger UI: http://localhost:8081/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

Key endpoints (all documented in Swagger):
- Accounts: `/api/v1/account/{userId}/...` (info, buying power, cash, equity, positions).
- Credentials: `/api/v1/credentials/{userId}` (register/update keys, status, deactivate, refresh).
- Strategies: `POST /api/v1/strategies/{userId}/{strategyId}/start` (start historical data streaming workflow).

## Mock trading-engine (Python gRPC) for local testing
- A lightweight mock server lives at `client.py` in the repo root.
- It implements the `TradingEngineService.ExecuteStrategy` RPC and writes what it receives:
  - Header -> `received_header.json`
  - Bar batches -> append lines to `received_bars.jsonl`

Run it (from repo root):
```bash
python -m venv .venv
. .venv/Scripts/activate  # or source .venv/bin/activate on *nix
pip install grpcio grpcio-tools

# generate Python stubs if missing (or after updating proto file)
# Option 1: Use helper script (recommended)
python generate_python_proto.py
# Option 2: Manual command
# python -m grpc_tools.protoc -I pborsa-trading/src/main/proto --python_out=. --grpc_python_out=. pborsa-trading/src/main/proto/trading_engine.proto

# start mock server on 0.0.0.0:9090
python client.py
```

Point the API to it (already default): `trading.engine.grpc.address=localhost:9090`.
When you POST `/api/v1/strategies/{userId}/{strategyId}/start`, the mock will log batches and write the files above so you can inspect what was streamed.

## Configuration
- API properties: `pborsa-api/src/main/resources/application.properties`
  - `spring.temporal.connection.target` – Temporal address
  - `trading.engine.grpc.address` – trading engine gRPC endpoint
  - `alpaca.base-url` / `alpaca.market-data-url` – Alpaca endpoints
- Strategy worker tuning:
  - `temporal.workers.strategy.max-concurrent-activities`
  - `temporal.workers.strategy.max-concurrent-workflows`

## Notes
- Temporal task queues:
  - Strategy/data streaming: `STRATEGY_EXECUTION_TASK_QUEUE` (hosted by API)
  - Trading/market-data: `TRADING_TASK_QUEUE`, `MARKET_DATA_TASK_QUEUE` (hosted by trading-worker)
- Swagger annotations live on controllers for Accounts, Credentials, and Strategies to help frontend integration.
