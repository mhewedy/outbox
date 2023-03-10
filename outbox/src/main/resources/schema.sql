create table outbox_messages
(
    id            varchar(36) primary key not null,
    service_class varchar(100)            not null,
    method_name   varchar(100)            not null,
    param_types   varchar(max),
    param_values  varchar(max),
    lock_id       varchar(36),
    status        int                     not null,
    error_message varchar(max),
    created_date  datetime,
    modified_date datetime
)