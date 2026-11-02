CREATE TABLE arenas (
    game VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    mode VARCHAR(32) NOT NULL,
    ready BOOLEAN NOT NULL DEFAULT FALSE,
    definition TEXT NOT NULL,
    map BYTEA NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (game, name)
);
