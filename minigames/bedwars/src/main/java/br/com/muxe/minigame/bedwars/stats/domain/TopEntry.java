package br.com.muxe.minigame.bedwars.stats.domain;

import java.util.Objects;
import java.util.UUID;

public record TopEntry(int position, UUID playerId, String name, long value, long totalXp) {

    public TopEntry {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(name, "name");
        if (position < 1) {
            throw new IllegalArgumentException("A top position starts at 1");
        }
    }
}
