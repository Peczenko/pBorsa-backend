-- Strategy catalog and order linkage.

create sequence strategies_id_seq start with 1 increment by 1;

create table strategies
(
    id          bigint       not null default nextval('strategies_id_seq')
        primary key,
    name        varchar(128) not null,
    description varchar(512),
    active      boolean      not null default true,
    created_at  timestamp(6) with time zone,
    updated_at  timestamp(6) with time zone
);

alter table orders
    add column strategy_id bigint;

alter table orders
    add constraint fk_orders_strategy
        foreign key (strategy_id)
        references strategies (id);

create index idx_orders_strategy_id
    on orders (strategy_id);

insert into strategies (id, name, description, active, created_at, updated_at)
values (1, 'Momentum V1', 'Example momentum strategy.', true, now(), now()),
       (2, 'Mean Reversion V1', 'Example mean reversion strategy.', true, now(), now()),
       (3, 'Breakout V1', 'Example breakout strategy.', true, now(), now());

select setval('strategies_id_seq', 3, true);
