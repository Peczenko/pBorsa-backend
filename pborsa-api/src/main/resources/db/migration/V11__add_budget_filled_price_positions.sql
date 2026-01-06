-- Add budget field to user_strategies
alter table user_strategies add column budget numeric(19, 4);

-- Add filled_avg_price to orders
alter table orders add column filled_avg_price numeric(19, 4);

-- Create strategy_positions table for P/L tracking
create table strategy_positions
(
    user_strategy_id bigint         not null primary key,
    total_shares     numeric(19, 8) not null default 0,
    total_cost_basis numeric(19, 4) not null default 0,
    realized_pnl     numeric(19, 4) not null default 0,
    version          bigint         not null default 0,
    created_at       timestamp(6) with time zone,
    updated_at       timestamp(6) with time zone,
    constraint fk_strategy_positions_user_strategy
        foreign key (user_strategy_id)
            references user_strategies (id)
            on delete cascade
);

-- Index for querying positions by user (via user_strategies join)
create index idx_strategy_positions_updated_at on strategy_positions (updated_at);

comment on table strategy_positions is 'Tracks position and P/L data for each user strategy';
comment on column strategy_positions.total_shares is 'Current number of shares held (long position)';
comment on column strategy_positions.total_cost_basis is 'Total cost basis of held shares (for avg cost calculation)';
comment on column strategy_positions.realized_pnl is 'Cumulative realized P/L from closed positions';
comment on column strategy_positions.version is 'Optimistic locking version for concurrent updates';

