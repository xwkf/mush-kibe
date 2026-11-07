package br.com.muxe.minigame.api.cosmetic.message;

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

    private final Map<UUID, Totals> totals = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Counter, Long>> thisMatch = new ConcurrentHashMap<>();

    public void load(UUID player, Totals stored) {
        totals.put(player, stored);
    }

    public OptionalLong record(UUID player, Counter counter) {
        long during = thisMatch.computeIfAbsent(player, id -> new EnumMap<>(Counter.class)).merge(counter, 1L, Long::sum);
        Totals stored = totals.get(player);
        return stored == null ? OptionalLong.empty() : OptionalLong.of(stored.of(counter) + during);
    }

    public record Totals(long kills, long finalKills, long deaths, long finalDeaths, long beds) {

        public static final Totals ZERO = new Totals(0, 0, 0, 0, 0);

        long of(Counter counter) {
            return switch (counter) {
                case KILLS -> kills;
                case FINAL_KILLS -> finalKills;
                case DEATHS -> deaths;
                case FINAL_DEATHS -> finalDeaths;
                case BEDS -> beds;
            };
        }
    }
}
