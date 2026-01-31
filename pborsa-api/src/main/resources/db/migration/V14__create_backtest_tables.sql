-- V14: Create backtest tables for strategy backtesting feature

CREATE SEQUENCE backtests_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE backtests (
    id                  BIGINT PRIMARY KEY DEFAULT nextval('backtests_id_seq'),
    user_id             BIGINT NOT NULL REFERENCES users(id),
    base_strategy_id    BIGINT NOT NULL REFERENCES base_strategies(id),
    name                VARCHAR(128) NOT NULL,
    symbol              VARCHAR(16) NOT NULL,
    budget              NUMERIC(19,4) NOT NULL,
    testing_start       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    testing_end         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    pnl                 NUMERIC(19,4),
    max_drawdown        NUMERIC(19,4),
    total_trades        INTEGER,
    winning_trades      INTEGER,
    error_message       VARCHAR(1024),
    created_at          TIMESTAMP(6) WITH TIME ZONE,
    updated_at          TIMESTAMP(6) WITH TIME ZONE,
    completed_at        TIMESTAMP(6) WITH TIME ZONE
);

CREATE SEQUENCE backtest_orders_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE backtest_orders (
    id              BIGINT PRIMARY KEY DEFAULT nextval('backtest_orders_id_seq'),
    backtest_id     BIGINT NOT NULL REFERENCES backtests(id) ON DELETE CASCADE,
    symbol          VARCHAR(16) NOT NULL,
    side            VARCHAR(8) NOT NULL,
    quantity        NUMERIC(19,8) NOT NULL,
    price           NUMERIC(19,4) NOT NULL,
    executed_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at      TIMESTAMP(6) WITH TIME ZONE
);

CREATE INDEX idx_backtests_user_id ON backtests(user_id);
CREATE INDEX idx_backtests_status ON backtests(status);
CREATE INDEX idx_backtest_orders_backtest_id ON backtest_orders(backtest_id);
