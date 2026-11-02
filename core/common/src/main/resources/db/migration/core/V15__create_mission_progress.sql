CREATE TABLE mission_progress (
    player_id UUID NOT NULL,
    game VARCHAR(32) NOT NULL,
    mission VARCHAR(64) NOT NULL,
    period_start DATE NOT NULL,
    progress BIGINT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (player_id, game, mission, period_start)
);
