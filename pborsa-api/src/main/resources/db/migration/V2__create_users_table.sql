create table users
(
    id           varchar(128) not null
        primary key,
    email        varchar(255)
        constraint uk_users_email
            unique,
    display_name varchar(255),
    provider     varchar(255) not null,
    status       varchar(255) not null
        constraint users_status_check
            check ((status)::text = ANY
                   ((ARRAY ['ACTIVE'::character varying, 'DISABLED'::character varying, 'DELETED'::character varying])::text[])),
    created_at   timestamp(6) with time zone,
    updated_at   timestamp(6) with time zone
);
