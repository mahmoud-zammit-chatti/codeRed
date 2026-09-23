alter table outbox_event add column incident_ref uuid;
alter table outbox_event add constraint fk_incident_ref foreign key (incident_ref) references incident(id);