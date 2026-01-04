-- Refactor strategies to support base strategies (templates) and user strategies (instances).

-- Step 1: Rename strategies table to base_strategies
alter table strategies rename to base_strategies;

-- Step 2: Rename sequence
alter sequence strategies_id_seq rename to base_strategies_id_seq;

-- Step 3: Add code column to base_strategies (unique identifier for each strategy type)
alter table base_strategies add column code varchar(32);

-- Update existing strategies with codes
update base_strategies set code = 'MOMENTUM_V1' where id = 1;
update base_strategies set code = 'MEAN_REVERSION_V1' where id = 2;
update base_strategies set code = 'BREAKOUT_V1' where id = 3;

-- Make code not null and unique after populating
alter table base_strategies alter column code set not null;
alter table base_strategies add constraint uk_base_strategies_code unique (code);

-- Step 4: Create user_strategies table
create sequence user_strategies_id_seq start with 1 increment by 1;

create table user_strategies
(
    id                bigint       not null default nextval('user_strategies_id_seq')
        primary key,
    user_id           bigint       not null,
    base_strategy_id  bigint       not null,
    name              varchar(128) not null,
    symbol            varchar(16)  not null,
    status            varchar(32)  not null default 'ACTIVE',
    created_at        timestamp(6) with time zone,
    updated_at        timestamp(6) with time zone,
    constraint fk_user_strategies_user
        foreign key (user_id)
        references users (id),
    constraint fk_user_strategies_base_strategy
        foreign key (base_strategy_id)
        references base_strategies (id),
    constraint uk_user_strategies_user_base_symbol
        unique (user_id, base_strategy_id, symbol)
);

create index idx_user_strategies_user_id on user_strategies (user_id);
create index idx_user_strategies_base_strategy_id on user_strategies (base_strategy_id);
create index idx_user_strategies_status on user_strategies (status);

-- Step 5: Add user_strategy_id to orders table
alter table orders add column user_strategy_id bigint;

-- Step 6: Create foreign key for user_strategy_id
alter table orders
    add constraint fk_orders_user_strategy
        foreign key (user_strategy_id)
        references user_strategies (id);

create index idx_orders_user_strategy_id on orders (user_strategy_id);

-- Step 7: Drop old strategy_id foreign key and column from orders
-- First drop the index
drop index if exists idx_orders_strategy_id;

-- Drop the foreign key constraint
alter table orders drop constraint if exists fk_orders_strategy;

-- Drop the column
alter table orders drop column if exists strategy_id;



