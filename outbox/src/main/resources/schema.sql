create table outbox_messages
(
    id            varchar(50) primary key not null,
    service_class varchar(100),
    method_name   varchar(100),
    param_types   varchar(max),
    param_values  varchar(max),
    lock_id       varchar(50),
    status        int,
    error_message varchar(max),
)