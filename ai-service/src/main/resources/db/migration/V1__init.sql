

CREATE TABLE ai_eval(

    id UUID primary key DEFAULT gen_random_uuid(),
    incident_id UUID NOT NULL,
    evaluation varchar(255) NOT NULL check(evaluation IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    recommanded_action varchar(255) NOT NULL CHECK (recommanded_action IN ('FIRE', 'CRIME', 'MEDICAL', 'ACCIDENT', 'OTHER')),
    confidence_score INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);