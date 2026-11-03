CREATE TABLE preferences (
    player_id UUID PRIMARY KEY,
    show_level BOOLEAN NOT NULL,
    tnt_timer BOOLEAN NOT NULL,
    chest_deposit VARCHAR(16) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
