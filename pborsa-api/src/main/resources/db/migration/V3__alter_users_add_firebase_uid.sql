alter table users rename column id to firebase_uid;

alter table users drop constraint users_pkey;

alter table users
    add column id bigserial;

update users
set id = nextval('users_id_seq')
where id is null;

alter table users
    alter column id set not null;

alter table users
    add constraint users_pkey primary key (id);

alter table users
    add constraint uk_users_firebase_uid unique (firebase_uid);
