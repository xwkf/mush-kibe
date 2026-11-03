CREATE TABLE quick_buy (
    player_id UUID PRIMARY KEY,
    slots TEXT[] NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
