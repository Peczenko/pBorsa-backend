alter table orders
    alter column user_id type bigint using user_id::bigint;

alter table order_history
    alter column user_id type bigint using user_id::bigint;
