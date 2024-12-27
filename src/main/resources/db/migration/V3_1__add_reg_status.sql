ALTER TABLE events ADD COLUMN registration_status VARCHAR(15) DEFAULT 'OPEN' NOT NULL CHECK
(registration_status IN ('OPEN', 'CLOSED', 'SUSPENDED'));