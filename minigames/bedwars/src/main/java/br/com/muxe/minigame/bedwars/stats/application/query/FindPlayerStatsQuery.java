package br.com.muxe.minigame.bedwars.stats.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import java.util.Objects;
import java.util.UUID;

public record FindPlayerStatsQuery(UUID playerId, StatsPeriod period) implements Query<PlayerStats> {

    public FindPlayerStatsQuery {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(period, "period");
    }

    public FindPlayerStatsQuery(UUID playerId) {
        this(playerId, StatsPeriod.TOTAL);
    }
}
