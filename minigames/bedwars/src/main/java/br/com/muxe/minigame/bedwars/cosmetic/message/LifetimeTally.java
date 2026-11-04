package br.com.muxe.minigame.bedwars.cosmetic.message;

import br.com.muxe.minigame.bedwars.stats.domain.StatCounters;
import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LifetimeTally {

    public enum Counter {
        KILLS,
        FINAL_KILLS,
        DEATHS,
        FINAL_DEATHS,
        BEDS
    }

    private final Map<UUID, StatCounters> totals = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Counter, Long>> thisMatch = new ConcurrentHashMap<>();

    public void load(UUID player, StatCounters stored) {
        totals.put(player, stored);
    }

    public OptionalLong record(UUID player, Counter counter) {
        long during = thisMatch.computeIfAbsent(player, id -> new EnumMap<>(Counter.class)).merge(counter, 1L, Long::sum);
        StatCounters stored = totals.get(player);
        return stored == null ? OptionalLong.empty() : OptionalLong.of(stored(stored, counter) + during);
    }

    private static long stored(StatCounters stored, Counter counter) {
        return switch (counter) {
            case KILLS -> stored.kills();
            case FINAL_KILLS -> stored.finalKills();
            case DEATHS -> stored.deaths();
            case FINAL_DEATHS -> stored.finalDeaths();
            case BEDS -> stored.bedsBroken();
        };
    }
}
