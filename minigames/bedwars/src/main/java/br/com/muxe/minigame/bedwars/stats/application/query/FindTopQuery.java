package br.com.muxe.minigame.bedwars.stats.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import br.com.muxe.minigame.bedwars.stats.domain.TopCategory;
import br.com.muxe.minigame.bedwars.stats.domain.TopEntry;
import java.util.List;
import java.util.Objects;

public record FindTopQuery(TopCategory category, String mode, StatsPeriod period, int limit) implements Query<List<TopEntry>> {

    public static final int MAX_LIMIT = 100;

    public FindTopQuery {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(period, "period");
        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException("A top needs a mode");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("A top limit must be between 1 and " + MAX_LIMIT + ", got: " + limit);
        }
    }
}
