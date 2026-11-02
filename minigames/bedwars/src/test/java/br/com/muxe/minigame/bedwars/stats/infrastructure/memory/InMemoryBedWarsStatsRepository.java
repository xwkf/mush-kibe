package br.com.muxe.minigame.bedwars.stats.infrastructure.memory;

import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.stats.domain.MatchOutcome;
import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import br.com.muxe.minigame.bedwars.stats.domain.StatCounters;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import br.com.muxe.minigame.bedwars.stats.domain.StatsSlot;
import br.com.muxe.minigame.bedwars.stats.domain.TopCategory;
import br.com.muxe.minigame.bedwars.stats.domain.TopEntry;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public final class InMemoryBedWarsStatsRepository implements BedWarsStatsRepository {

    private static final StatsSlot TOTAL_GENERAL = new StatsSlot(PlayerStats.GENERAL, StatsPeriod.TOTAL, StatsPeriod.TOTAL_START);

    private final Map<UUID, Map<StatsSlot, StatCounters>> rows = new HashMap<>();
    private final Map<UUID, Set<String>> favourites = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();

    public void name(UUID playerId, String name) {
        names.put(playerId, name);
    }

    public void put(UUID playerId, StatsSlot slot, StatCounters counters) {
        rows.computeIfAbsent(playerId, ignored -> new HashMap<>()).put(slot, counters);
    }

    public int rowCount() {
        return rows.values().stream().mapToInt(Map::size).sum();
    }

    @Override
    public void record(MatchOutcome outcome, Instant at, ZoneId zone) {
        Map<StatsSlot, StatCounters> slots = rows.computeIfAbsent(outcome.playerId(), ignored -> new HashMap<>());
        for (StatsSlot slot : StatsSlot.touchedBy(outcome, at, zone)) {
            slots.put(slot, slots.getOrDefault(slot, StatCounters.ZERO).plus(outcome));
        }
    }

    @Override
    public void addXp(UUID playerId, long xp, Instant at, ZoneId zone) {
        Map<StatsSlot, StatCounters> slots = rows.computeIfAbsent(playerId, ignored -> new HashMap<>());
        for (StatsPeriod period : StatsPeriod.values()) {
            StatsSlot slot = new StatsSlot(PlayerStats.GENERAL, period, period.start(at, zone));
            StatCounters current = slots.getOrDefault(slot, StatCounters.ZERO);
            slots.put(slot, new StatCounters(current.games(), current.bedsBroken(), current.bedsLost(), current.kills(), current.deaths(), current.assists(),
                current.finalKills(), current.finalDeaths(), current.finalAssists(), current.wins(), current.losses(), current.winstreak(),
                current.bestWinstreak(), current.xp() + xp));
        }
    }

    @Override
    public PlayerStats totals(UUID playerId) {
        return stats(playerId, StatsPeriod.TOTAL, StatsPeriod.TOTAL_START);
    }

    @Override
    public PlayerStats period(UUID playerId, StatsPeriod period, Instant at, ZoneId zone) {
        return stats(playerId, period, period.start(at, zone));
    }

    @Override
    public List<TopEntry> top(TopCategory category, String mode, StatsPeriod period, LocalDate start, int limit) {
        StatsSlot wanted = new StatsSlot(mode, period, start);
        List<Map.Entry<UUID, Long>> ranked = new ArrayList<>();
        rows.forEach((playerId, slots) -> {
            long value = category.valueOf(slots.getOrDefault(wanted, StatCounters.ZERO));
            if (value > 0) {
                ranked.add(Map.entry(playerId, value));
            }
        });
        ranked.sort(Comparator.<Map.Entry<UUID, Long>>comparingLong(Map.Entry::getValue).reversed()
            .thenComparing(Map.Entry::getKey));
        List<TopEntry> entries = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : ranked.subList(0, Math.min(limit, ranked.size()))) {
            UUID playerId = entry.getKey();
            long totalXp = rows.get(playerId).getOrDefault(TOTAL_GENERAL, StatCounters.ZERO).xp();
            entries.add(new TopEntry(entries.size() + 1, playerId, names.getOrDefault(playerId, playerId.toString()), entry.getValue(), totalXp));
        }
        return List.copyOf(entries);
    }

    @Override
    public Set<String> favouriteMaps(UUID playerId) {
        return Collections.unmodifiableSet(new TreeSet<>(favourites.getOrDefault(playerId, Set.of())));
    }

    @Override
    public boolean toggleFavouriteMap(UUID playerId, String arena, Instant at) {
        Set<String> arenas = favourites.computeIfAbsent(playerId, ignored -> new TreeSet<>());
        if (arenas.remove(arena)) {
            return false;
        }
        arenas.add(arena);
        return true;
    }

    private PlayerStats stats(UUID playerId, StatsPeriod period, LocalDate start) {
        Map<String, StatCounters> byMode = new HashMap<>();
        rows.getOrDefault(playerId, Map.of()).forEach((slot, counters) -> {
            if (slot.period() == period && slot.start().equals(start)) {
                byMode.put(slot.mode(), counters);
            }
        });
        return new PlayerStats(byMode);
    }
}
