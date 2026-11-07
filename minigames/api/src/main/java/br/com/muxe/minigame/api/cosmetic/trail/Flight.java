package br.com.muxe.minigame.api.cosmetic.trail;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Objects;

public record Flight(Vec position, Vec velocity, int age, ProjectileKind kind) {

    public Flight {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(velocity, "velocity");
        Objects.requireNonNull(kind, "kind");
        if (age < 0) {
            throw new IllegalArgumentException("age must not be negative");
        }
    }
}
