package br.com.muxe.minigame.bedwars.stats.domain;

import java.util.Objects;
import java.util.UUID;

public record MatchOutcome(
    UUID playerId,
    String mode,
    long bedsBroken,
    long bedsLost,
    long kills,
    long deaths,
    long assists,
    long finalKills,
    long finalDeaths,
    long finalAssists,
    MatchResult result,
    long xp
) {

    public MatchOutcome {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(result, "result");
        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException("A match outcome needs a mode");
        }
        if (PlayerStats.GENERAL.equals(mode)) {
            throw new IllegalArgumentException("The mode '" + PlayerStats.GENERAL + "' is reserved for the general stats");
        }
        if (bedsBroken < 0 || bedsLost < 0 || kills < 0 || deaths < 0 || assists < 0
            || finalKills < 0 || finalDeaths < 0 || finalAssists < 0 || xp < 0) {
            throw new IllegalArgumentException("Match outcome counters cannot be negative");
        }
    }
}
