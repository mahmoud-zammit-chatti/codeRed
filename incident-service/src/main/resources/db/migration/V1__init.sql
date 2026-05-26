CREATE TABLE incident (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          title VARCHAR(255) NOT NULL,
                          description TEXT,
                          severity VARCHAR(255) NOT NULL DEFAULT 'MEDIUM'
                              CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
                          status VARCHAR(255) NOT NULL DEFAULT 'PENDING'
                              CHECK (status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED')),
                          type VARCHAR(255) NOT NULL
                              CHECK (type IN ('FIRE', 'CRIME', 'MEDICAL', 'ACCIDENT', 'OTHER')),
                          location VARCHAR(255) NOT NULL,
                          reported_by VARCHAR(8) NOT NULL,
                          agent_id UUID ,
                          dispatched_unit_id UUID,
                          createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);