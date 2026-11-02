package br.com.muxe.minigame.bedwars.stats.domain;

import java.util.Map;

public record PlayerStats(Map<String, StatCounters> byMode) {

    public static final String GENERAL = "geral";
    public static final PlayerStats EMPTY = new PlayerStats(Map.of());

    public PlayerStats {
        byMode = Map.copyOf(byMode);
    }

    public StatCounters of(String mode) {
        return byMode.getOrDefault(mode, StatCounters.ZERO);
    }

    public StatCounters general() {
        return of(GENERAL);
    }
}
