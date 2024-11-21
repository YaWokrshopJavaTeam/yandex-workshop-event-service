DROP TABLE IF EXISTS events;

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    name              VARCHAR(120)                        NOT NULL,
    description        VARCHAR(7000)                       NOT NULL,
    created_date_time         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    start_date_time         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date_time         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    location        VARCHAR(120)                              NOT NULL,
    owner_id        BIGINT                                NOT NULL,
    PRIMARY KEY (id)
);