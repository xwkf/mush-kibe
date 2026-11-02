CREATE TABLE player_stats (
    player_id UUID NOT NULL,
    mode VARCHAR(32) NOT NULL,
    period VARCHAR(16) NOT NULL,
    period_start DATE NOT NULL,
    games BIGINT NOT NULL DEFAULT 0,
    beds_broken BIGINT NOT NULL DEFAULT 0,
    beds_lost BIGINT NOT NULL DEFAULT 0,
    kills BIGINT NOT NULL DEFAULT 0,
    deaths BIGINT NOT NULL DEFAULT 0,
    assists BIGINT NOT NULL DEFAULT 0,
    final_kills BIGINT NOT NULL DEFAULT 0,
    final_deaths BIGINT NOT NULL DEFAULT 0,
    final_assists BIGINT NOT NULL DEFAULT 0,
    wins BIGINT NOT NULL DEFAULT 0,
    losses BIGINT NOT NULL DEFAULT 0,
    winstreak BIGINT NOT NULL DEFAULT 0,
    best_winstreak BIGINT NOT NULL DEFAULT 0,
    xp BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (player_id, mode, period, period_start)
);

CREATE INDEX player_stats_top_idx ON player_stats (mode, period, period_start);

CREATE TABLE favourite_maps (
    player_id UUID NOT NULL,
    arena VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (player_id, arena)
);
