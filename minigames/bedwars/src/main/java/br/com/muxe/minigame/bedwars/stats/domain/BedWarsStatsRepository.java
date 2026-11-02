package br.com.muxe.minigame.bedwars.stats.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface BedWarsStatsRepository {

    void record(MatchOutcome outcome, Instant at, ZoneId zone);

    void addXp(UUID playerId, long xp, Instant at, ZoneId zone);

    PlayerStats totals(UUID playerId);

    PlayerStats period(UUID playerId, StatsPeriod period, Instant at, ZoneId zone);

    List<TopEntry> top(TopCategory category, String mode, StatsPeriod period, LocalDate start, int limit);

    Set<String> favouriteMaps(UUID playerId);

    boolean toggleFavouriteMap(UUID playerId, String arena, Instant at);
}
