package br.com.muxe.core.common.arena.domain;

import java.time.Instant;
import java.util.Objects;

public record ArenaRecord(String game, String name, String mode, boolean ready, String definition, byte[] map, Instant updatedAt) {

    public ArenaRecord {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(definition, "definition");
        map = map == null ? new byte[0] : map.clone();
        Objects.requireNonNull(updatedAt, "updatedAt");
    }

    @Override
    public byte[] map() {
        return map.clone();
    }
}
