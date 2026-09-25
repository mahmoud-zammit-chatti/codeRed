alter table outbox_event add column trigger varchar(255)
    check (trigger in ('MANUAL', 'AUTOMATIC'));