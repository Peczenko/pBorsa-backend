alter table user_api_credentials
    alter column user_id type bigint using user_id::bigint;

alter table user_api_credentials
    add constraint fk_user_api_credentials_user
        foreign key (user_id) references users (id);
