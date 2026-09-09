CREATE TABLE classification(
    id uuid PRIMARY KEY,
    incident_id uuid REFERENCES incident(id),
    suggested_severity VARCHAR(255) NOT NULL DEFAULT 'MEDIUM'
        CHECK (suggested_severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    suggested_type VARCHAR(255) NOT NULL
        CHECK (suggested_type IN ('FIRE', 'CRIME', 'MEDICAL', 'ACCIDENT', 'OTHER')),
    confidence  INT,
    model_version float,
    classification_status varchar(255) NOT NULL DEFAULT 'PENDING'
        CHECK (classification_status IN ('PENDING', 'CONFIRMED', 'EDITEDANDCONFIRMED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);