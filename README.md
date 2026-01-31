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

## Initial Setup After Application Start

### 0. Configure Firebase Credentials

The application requires Firebase Admin SDK credentials for authentication. You will receive a `firebase-service-account.json` file.

**Option A: Set via Environment Variable (Recommended)**
```bash
# Windows (PowerShell)
$env:FIREBASE_CREDENTIALS_PATH = "C:\path\to\firebase-service-account.json"

# Windows (CMD)
set FIREBASE_CREDENTIALS_PATH=C:\path\to\firebase-service-account.json

# Linux/Mac
export FIREBASE_CREDENTIALS_PATH=/path/to/firebase-service-account.json
```

**Option B: Set directly in application.properties**
```properties
firebase.credentials.path=C:/path/to/firebase-service-account.json
```

**Option C: Use Base64-encoded credentials (for containerized environments)**
```bash
# Encode the file
base64 -w 0 firebase-service-account.json > firebase-creds-base64.txt

# Set environment variable
export FIREBASE_CREDENTIALS_BASE64=$(cat firebase-creds-base64.txt)
```

Or in `application.properties`:
```properties
firebase.credentials.base64=eyJ0eXBlIjoic2VydmljZV9hY2NvdW50Iiw...
```

#### Firebase Configuration Properties

```properties
# Enable/disable Firebase authentication
firebase.enabled=true

# Path to service account JSON file
firebase.credentials.path=${FIREBASE_CREDENTIALS_PATH:}

# Alternative: Base64-encoded service account JSON
firebase.credentials.base64=${FIREBASE_CREDENTIALS_BASE64:}

# Firebase project ID (optional, auto-detected from credentials)
firebase.project-id=${FIREBASE_PROJECT_ID:}
```

> **Note**: Either `firebase.credentials.path` OR `firebase.credentials.base64` must be set. Path takes precedence if both are provided.

---

### 1. Obtain JWT Token for Authentication

All API endpoints (except public ones) require a Firebase JWT token. To obtain a token, send a POST request:

```bash
POST https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyDeBIrmh2HvbrAxbq96KvVQ9OuSnvIE2SU
Content-Type: application/json

{
    "email": "admin_user@gmail.com",
    "password": "admin_password",
    "returnSecureToken": true
}
```

**Response** will include an `idToken` field – use this as the Bearer token for all subsequent requests:
```
Authorization: Bearer <idToken>
```

### 2. Set Up Alpaca Credentials for Admin User

A default admin user with `id=0` is available. You **must** register Alpaca API credentials before using trading features:

```bash
POST http://localhost:8081/api/v1/credentials/0
Authorization: Bearer <idToken>
Content-Type: application/json

{
    "apiKey": "PKRC2DDEJG6B5GLSHXYV",
    "secretKey": "AHHMyK1G8KSP5WPshXsv3vekEVAqmjK3X8HQG7pR6z",
    "paperTrading": true
}
```

> **Note**: Replace with your actual Alpaca paper trading API keys.

---

## Configuration

### Application Properties

All configuration is in `pborsa-api/src/main/resources/application.properties`.

#### HTTP Port
```properties
server.port=8081
```
Change this to run the API on a different port.

#### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pborsa
spring.datasource.username=postgres
spring.datasource.password=postgres
```

#### gRPC Configuration (Trading Engine)
```properties
# Enable/disable gRPC client
trading.engine.grpc.enabled=true

# Trading engine gRPC address (host:port)
trading.engine.grpc.address=localhost:9090

# Batch size for streaming historical data
trading.engine.grpc.batch-size=1000

# Max records per page when fetching historical data
trading.engine.grpc.page-limit=10000
```

**Example values:**
| Property | Default | Description |
|----------|---------|-------------|
| `trading.engine.grpc.enabled` | `true` | Set to `false` to disable gRPC client |
| `trading.engine.grpc.address` | `localhost:9090` | Trading engine gRPC endpoint |
| `trading.engine.grpc.batch-size` | `1000` | Number of trades per batch when streaming |
| `trading.engine.grpc.page-limit` | `10000` | Max historical data records per API call |

#### Temporal Configuration
```properties
temporal.enabled=true
spring.temporal.connection.target=localhost:7233
spring.temporal.namespace=default
```

---

## Infrastructure (Docker Compose)

### Exposed Ports

| Service | Host Port | Container Port | Description |
|---------|-----------|----------------|-------------|
| PostgreSQL (pBorsa) | `5432` | `5432` | Main application database |
| PostgreSQL (Temporal) | `5433` | `5432` | Temporal's internal database |
| Temporal Server | `7233` | `7233` | Temporal gRPC API |
| Temporal UI | `8088` | `8080` | Temporal Web UI |

### Database Connection
- **Host**: `localhost`
- **Port**: `5432`
- **Database**: `pborsa`
- **Username**: `postgres`
- **Password**: `postgres`

Connect via any PostgreSQL client:
```bash
psql -h localhost -p 5432 -U postgres -d pborsa
```

### Temporal UI
After starting Docker Compose, access the Temporal UI at:
- **URL**: http://localhost:8088

In the Temporal UI you can monitor:
- **Strategy Execution Workflows** – see historical data streaming progress
- **Order Request Workflows** – track order placement and status updates
- Workflow history, retries, and failures

---

## gRPC Architecture

### Proto File Location
```
pborsa-trading/src/main/proto/trading_engine.proto
```

### Services Defined

#### 1. TradingEngineService (pBorsa API → Trading Engine)
The pBorsa API acts as a **gRPC client** calling the trading engine:

| RPC Method | Direction | Description |
|------------|-----------|-------------|
| `ExecuteStrategy` | API → Engine | Stream historical bar data (OHLCV) for strategy execution |
| `SendLiveBars` | API → Engine | Send real-time bar updates (called periodically, e.g., every minute) |
| `NotifyOrderStatusUpdate` | API → Engine | Push order status changes to the trading engine |
| `NotifyStrategyStatusUpdate` | API → Engine | Push strategy status changes (e.g., when strategy is stopped) |

#### 2. TradingOrderService (Trading Engine → pBorsa API)
The pBorsa API exposes a **gRPC server** on port `9092` for receiving order requests:

| RPC Method | Direction | Description |
|------------|-----------|-------------|
| `PlaceOrder` | Engine → API | Submit order requests from trading engine |

### Key gRPC Entities

#### StrategyExecutionHeader
Sent at the start of strategy execution stream:
```protobuf
message StrategyExecutionHeader {
  string execution_id = 1;    // Unique execution UUID
  int64 user_id = 2;          // User ID
  int64 strategy_id = 3;      // User strategy ID
  string symbol = 4;          // Trading symbol (e.g., "AAPL")
  string timeframe = 5;       // Timeframe
  Timestamp start = 6;        // Historical data start time
  Timestamp end = 7;          // Historical data end time
}
```

#### Bar / BarBatch
Historical bar data (OHLCV) streamed to the trading engine:
```protobuf
message Bar {
  Timestamp timestamp = 1;
  double open = 2;
  double high = 3;
  double low = 4;
  double close = 5;
  int64 volume = 6;
  int64 trade_count = 7;
  double vwap = 8;
}

message BarBatch {
  repeated Bar bars = 1;
}

message StrategyExecutionChunk {
  oneof payload {
    StrategyExecutionHeader header = 1;  // Sent first
    BarBatch bar_batch = 2;              // Historical bars follow
  }
}
```

#### OrderRequest
Order submitted by trading engine:
```protobuf
message OrderRequest {
  int64 user_id = 1;
  string symbol = 2;
  double quantity = 3;
  OrderSide side = 4;          // BUY, SELL
  OrderType type = 5;          // MARKET, LIMIT, STOP, STOP_LIMIT
  TimeInForce time_in_force = 6;
  double limit_price = 7;
  double stop_price = 8;
  bool extended_hours = 9;
  string client_order_id = 10;
  int64 strategy_id = 11;
}
```

#### OrderStatusUpdate
Pushed to trading engine when order status changes:
```protobuf
message OrderStatusUpdate {
  string order_id = 1;
  string workflow_id = 2;
  string client_order_id = 3;  // Used to route to correct trading engine
  string alpaca_order_id = 4;
  OrderStatus status = 5;
  OrderStatusReason reason = 6;
  string message = 7;
  Timestamp updated_at = 8;
  Timestamp created_at = 9;
  string resume_token = 10;
}
```

#### StrategyStatusNotification
Pushed to trading engine when strategy status changes (currently triggered when strategy is STOPPED):
```protobuf
message StrategyStatusNotification {
  int64 strategy_id = 1;
  int64 user_id = 2;
  string symbol = 3;
  StrategyStatus old_status = 4;
  StrategyStatus new_status = 5;
  Timestamp updated_at = 6;
}

message StrategyStatusNotificationAck {
  bool received = 1;
  string message = 2;
}

enum StrategyStatus {
  STRATEGY_STATUS_UNSPECIFIED = 0;
  STRATEGY_CREATED = 1;
  STRATEGY_PREPARING = 2;
  STRATEGY_ACTIVE = 3;
  STRATEGY_PAUSED = 4;
  STRATEGY_STOPPED = 5;
  STRATEGY_START_FAILED = 6;
}
```

---

## Strategies

### Architecture: Base Strategies vs User Strategies

The system uses a **two-tier strategy architecture**:

#### Base Strategies (Templates)
Read-only catalog entries that define available trading strategies. These are system-defined and cannot be modified by users.

**Table**: `base_strategies`

| Field | Description |
|-------|-------------|
| `id` | Primary key |
| `code` | Unique identifier (e.g., `MOMENTUM_V1`) |
| `name` | Display name |
| `description` | Strategy description |
| `active` | Whether strategy is available for subscription |

#### User Strategies (Instances)
When a user wants to use a strategy, they create a **user strategy** by:
1. Selecting a base strategy
2. Choosing a stock symbol (e.g., `AAPL`)
3. Giving it a custom name
4. Setting a budget

**Table**: `user_strategies`

| Field | Description |
|-------|-------------|
| `id` | Primary key (used as `strategyId` in APIs) |
| `user_id` | Owner user ID |
| `base_strategy_id` | Reference to base strategy template |
| `name` | User's custom name for this strategy instance |
| `symbol` | Trading symbol (e.g., `AAPL`, `MSFT`) |
| `status` | Current status (`CREATED`, `PREPARING`, `ACTIVE`, `PAUSED`, `STOPPED`) |
| `budget` | Allocated budget for this strategy |

**Constraint**: A user can only have one instance of each base strategy per symbol (unique on `user_id` + `base_strategy_id` + `symbol`).

### Default Base Strategies

The system comes with 3 pre-configured base strategies:

| ID | Code | Name | Description |
|----|------|------|-------------|
| 1 | `MOMENTUM_V1` | Momentum V1 | Example momentum strategy |
| 2 | `MEAN_REVERSION_V1` | Mean Reversion V1 | Example mean reversion strategy |
| 3 | `BREAKOUT_V1` | Breakout V1 | Example breakout strategy |

### Example: Creating a User Strategy

```bash
POST http://localhost:8081/api/v1/strategies/{userId}/user-strategies
Authorization: Bearer <idToken>
Content-Type: application/json

{
    "baseStrategyId": 1,
    "name": "My AAPL Momentum Strategy",
    "symbol": "AAPL",
    "budget": 5000.00
}
```

This creates a user strategy that:
- Uses the **Momentum V1** base strategy
- Trades **AAPL** stock
- Has a **$5,000** budget
- Starts in `CREATED` status

---

## Strategy Lifecycle

### Status Transitions
User strategies follow a defined state machine:

```
CREATED → PREPARING → ACTIVE ⇄ PAUSED
                ↓         ↓
              STOPPED ← STOPPED
```

| From | Allowed Transitions |
|------|---------------------|
| `CREATED` | `PREPARING` |
| `PREPARING` | `ACTIVE`, `STOPPED` |
| `ACTIVE` | `PAUSED`, `STOPPED` |
| `PAUSED` | `ACTIVE`, `STOPPED` |
| `STOPPED` | (terminal state) |

### Strategy Execution Flow

When a strategy is activated (`CREATED` → `PREPARING`):

1. **Status Change**: Strategy status set to `PREPARING`
2. **Temporal Workflow Started**: `StrategyExecutionWorkflow` begins
3. **Historical Data Sent to Trading Engine** via gRPC `ExecuteStrategy`:
   - **Header**: `StrategyExecutionHeader` with execution context
   - **Historical Bars**: `BarBatch` chunks (OHLCV data for the specified period)
4. **Workflow Completes**: Strategy status set to `ACTIVE`
5. **Real-Time Updates Begin**: Live bar updates are sent via `SendLiveBars`

#### Data Shared with Trading Engine
When strategy execution starts, the following is streamed:
```java
StrategyExecutionContext {
    executionId,    // UUID for this execution
    userId,         // User ID
    strategyId,     // User strategy ID
    symbol,         // Trading symbol (e.g., "AAPL")
    budget,         // User's budget for this strategy
    start,          // Historical data start (default: 3 months ago)
    end             // Historical data end (default: now - 15 min)
}
```

### Real-Time Bar Updates

After a strategy becomes `ACTIVE`, the API sends real-time bar updates to the trading engine using the `SendLiveBars` RPC method.

#### How It Works

1. **Periodic Updates**: The API calls `SendLiveBars` periodically (e.g., every minute) with the latest bar data
2. **Multi-Symbol Support**: Each update can contain bars for multiple symbols
3. **Strategy Routing**: Updates include `strategy_ids` to indicate which strategies are interested in the data

#### LiveBarUpdate Message
```protobuf
message LiveBarUpdate {
  Timestamp update_time = 1;      // When the update was sent
  repeated SymbolBar bars = 2;    // Bar data for each symbol
  repeated int64 strategy_ids = 3; // Strategies interested in these symbols
}

message SymbolBar {
  string symbol = 1;              // Trading symbol (e.g., "AAPL")
  string timeframe = 2;           // Bar timeframe (e.g., "1Min")
  Bar bar = 3;                    // The OHLCV bar data
}
```

#### Response
```protobuf
message LiveBarUpdateAck {
  bool received = 1;              // Whether the update was accepted
  string message = 2;             // Status message
  int32 bars_processed = 3;       // Number of bars processed
}
```

---

### Strategy Status Notifications

When a strategy's status changes, the pBorsa API notifies the trading engine via the `NotifyStrategyStatusUpdate` gRPC method. This allows the trading engine to react to strategy lifecycle events (e.g., stop processing orders for a stopped strategy).

#### How It Works

1. **Event Published**: When a strategy status changes (e.g., user stops a strategy), a `StrategyStatusChangedEvent` is published
2. **Event Listener**: `StrategyStatusNotificationService` listens for these events asynchronously
3. **Filter**: Currently, only `STOPPED` status transitions trigger a notification to the trading engine
4. **Proto Mapping**: The event is converted to a `StrategyStatusNotification` protobuf message
5. **gRPC Call**: The notification is sent to the trading engine via `TradingEngineService.NotifyStrategyStatusUpdate`

#### Architecture

```
UserStrategyService                    TradingEngineOrderStatusClient
       │                                           │
       │ publishes StrategyStatusChangedEvent      │
       ▼                                           │
StrategyStatusNotificationService                  │
       │                                           │
       │ (filters for STOPPED status)              │
       │                                           │
       ▼                                           │
StrategyStatusProtoMapper                          │
       │                                           │
       │ (converts to protobuf)                    │
       ▼                                           ▼
       └──────────────────────────────────────────►│
                                                   │ notifyStrategyStatusUpdate()
                                                   ▼
                                            Trading Engine
                                            (port 9090)
```

#### RPC Method
```protobuf
service TradingEngineService {
  // API calls this when strategy status changes (e.g., strategy stopped)
  rpc NotifyStrategyStatusUpdate(StrategyStatusNotification) returns (StrategyStatusNotificationAck);
}
```

#### Message Format
```protobuf
message StrategyStatusNotification {
  int64 strategy_id = 1;          // User strategy ID
  int64 user_id = 2;              // User ID
  string symbol = 3;              // Trading symbol (e.g., "AAPL")
  StrategyStatus old_status = 4;  // Previous status
  StrategyStatus new_status = 5;  // New status (e.g., STRATEGY_STOPPED)
  Timestamp updated_at = 6;       // When the status changed
}
```

#### Status Values
| Proto Enum | Domain Status | Description |
|------------|---------------|-------------|
| `STRATEGY_CREATED` | `CREATED` | Strategy created, not yet activated |
| `STRATEGY_PREPARING` | `PREPARING` | Historical data transfer in progress |
| `STRATEGY_ACTIVE` | `ACTIVE` | Strategy is running and trading |
| `STRATEGY_PAUSED` | `PAUSED` | Temporarily paused by user |
| `STRATEGY_STOPPED` | `STOPPED` | Permanently stopped (terminal state) |
| `STRATEGY_START_FAILED` | `START_FAILED` | Failed to start execution |

#### Error Handling
- Notification failures do **not** affect the strategy status update (fire-and-forget pattern)
- Errors are logged but the strategy transition completes regardless
- The trading engine should handle missed notifications gracefully (e.g., via periodic reconciliation)

---

## API Documentation (Swagger)

After starting `pborsa-api`, open:
- **Swagger UI**: http://localhost:8081/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

Key endpoints (all documented in Swagger):
- **Accounts**: `/api/v1/account/{userId}/...` (info, buying power, cash, equity, positions)
- **Credentials**: `/api/v1/credentials/{userId}` (register/update keys, status, deactivate, refresh)
- **Strategies**: `/api/v1/strategies/{userId}/user-strategies` (manage user strategies)
- **Admin**: `/api/v1/admin/...` (admin-only endpoints)

---

## Python Testing Tools

### Setup
```bash
python -m venv .venv
.venv\Scripts\activate  # Windows
# or: source .venv/bin/activate  # Linux/Mac

pip install grpcio grpcio-tools
```

### Generate Python Proto Stubs
After modifying the proto file, regenerate Python code:
```bash
python generate_python_proto.py
```
This generates:
- `trading_engine_pb2.py` – Message classes
- `trading_engine_pb2_grpc.py` – Service stubs

### Python Files

| File | Purpose | How to Run |
|------|---------|------------|
| `generate_python_proto.py` | Regenerates Python gRPC stubs from proto file | `python generate_python_proto.py` |
| `client.py` | **Mock trading engine server** – receives strategy execution streams and saves data to files | `python client.py` |
| `trading_engine_order_status_server.py` | Mock server that receives order status updates from pBorsa API | `python trading_engine_order_status_server.py --port 9090` |
| `grpc_order_load_test.py` | Load test for `TradingOrderService.PlaceOrder` RPC | `python grpc_order_load_test.py --target localhost:9092 --count 100` |
| `trading_engine_pb2.py` | Auto-generated protobuf message classes | (imported by other scripts) |
| `trading_engine_pb2_grpc.py` | Auto-generated gRPC service stubs | (imported by other scripts) |

### Testing Strategy Execution

1. Start the mock trading engine:
   ```bash
   python client.py
   ```
   This listens on `0.0.0.0:9090` and saves received data to:
   - `received_header.json` – Strategy execution header
   - `received_trades_test.jsonl` – Trade data (JSON lines format)

2. Start pBorsa API and activate a strategy via REST API

3. Check the output files to verify data was streamed correctly

### Testing Order Status Updates

1. Start the order status server:
   ```bash
   python trading_engine_order_status_server.py --port 9090
   ```

2. When orders are placed and their status changes, the server logs updates

### Load Testing Orders

```bash
python grpc_order_load_test.py \
    --target localhost:9092 \
    --count 150 \
    --workers 50 \
    --duration 10 \
    --user-id 1 \
    --strategy-id 2 \
    --symbols AAPL,MSFT,NVDA
```

---

## Order Reconciliation

The API includes a background scheduler that periodically reconciles local order statuses with Alpaca's actual order statuses. This catches any status updates that may have been missed due to network issues, restarts, or timing gaps.

### How It Works

1. **Scheduler** (`OrderReconciliationScheduler`) runs at a configurable interval
2. **Query** finds all orders that:
   - Have `updatedAt` older than `exclude-recent-minutes` (default: 1 min)
   - Are **not** in terminal status (`FILLED`, `CANCELED`, `EXPIRED`, `REJECTED`)
3. **For each user's orders**, the processor:
   - Fetches open orders from Alpaca
   - Fetches recently closed orders from Alpaca (last 500)
   - Compares local status with remote status
   - If status differs → updates local database
   - If status unchanged but should be closed → cancels stale order on Alpaca

### Configuration

```properties
# Reconciliation interval in milliseconds (default: 900000 = 15 minutes)
orders.reconcile.interval-ms=900000

# Exclude orders updated within this many minutes (default: 1)
orders.reconcile.exclude-recent-minutes=1
```

| Property | Default | Description |
|----------|---------|-------------|
| `orders.reconcile.interval-ms` | `900000` (15 min) | How often reconciliation runs |
| `orders.reconcile.exclude-recent-minutes` | `1` | Skip orders updated within X minutes (avoids racing with real-time updates) |

### Disabling Reconciliation

To effectively disable order reconciliation, set a very large interval:

```properties
# Disable reconciliation (run once per ~24 days)
orders.reconcile.interval-ms=2147483647
```

Or set to a very long interval like 24 hours:
```properties
# Run reconciliation once per day
orders.reconcile.interval-ms=86400000
```

---

## Notes

- **Temporal Task Queues**:
  - Strategy/data streaming: `STRATEGY_EXECUTION_TASK_QUEUE` (hosted by API)
  - Trading/market-data: `TRADING_TASK_QUEUE`, `MARKET_DATA_TASK_QUEUE` (hosted by trading-worker)

- **gRPC Ports Summary**:
  - `9090` – Trading engine (external, configurable via `trading.engine.grpc.address`)
  - `9092` – pBorsa API's TradingOrderService (receives order requests from trading engine)

- Swagger annotations live on controllers for Accounts, Credentials, Strategies, and Admin endpoints
