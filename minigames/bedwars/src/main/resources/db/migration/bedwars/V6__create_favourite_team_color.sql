CREATE TABLE favourite_team_color (
    player_id UUID PRIMARY KEY,
    team_color VARCHAR(16) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
