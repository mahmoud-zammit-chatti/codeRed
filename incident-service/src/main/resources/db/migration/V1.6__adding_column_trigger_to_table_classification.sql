alter table classification add column trigger varchar(255)
check (trigger in ('MANUAL', 'AUTOMATIC'));