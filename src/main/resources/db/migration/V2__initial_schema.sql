CREATE TABLE organizing_team_members (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    CONSTRAINT pk_organizing_team_members PRIMARY KEY (id),
    CONSTRAINT organizing_team_members_unique_ids UNIQUE (event_id, user_id)
);