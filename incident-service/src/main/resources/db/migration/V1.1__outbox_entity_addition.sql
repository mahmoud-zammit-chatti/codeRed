CREATE TABLE outbox_event (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          title VARCHAR(255) NOT NULL,
                          payload TEXT,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          sent BOOLEAN NOT NULL DEFAULT FALSE
);