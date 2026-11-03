package br.com.muxe.core.bukkit.cosmetic.animation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record Scene(Vec origin, Vec facing, Optional<String> skin, List<UUID> riders) {

    public Scene {
        Objects.requireNonNull(origin, "origin");
        Vec flat = new Vec(facing.x(), 0, facing.z()).normalize();
        facing = flat.length() == 0 ? new Vec(0, 0, 1) : flat;
        Objects.requireNonNull(skin, "skin");
        riders = List.copyOf(riders);
    }

    public Scene(Vec origin, Vec facing, Optional<String> skin) {
        this(origin, facing, skin, List.of());
    }

    public static Scene at(Vec origin, Vec facing) {
        return new Scene(origin, facing, Optional.empty());
    }

    public Vec right() {
        return new Vec(-facing.z(), 0, facing.x());
    }
}
