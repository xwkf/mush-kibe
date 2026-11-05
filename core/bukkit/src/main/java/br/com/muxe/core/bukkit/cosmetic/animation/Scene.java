package br.com.muxe.core.bukkit.cosmetic.animation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public record Scene(Vec origin, Vec facing, Optional<String> skin, List<UUID> riders, Area area, Optional<SignedSkin> body, OptionalInt dye) {

    public static final double REACH = 16;
    public static final int DYES = 16;

    public Scene {
        Objects.requireNonNull(origin, "origin");
        Vec flat = new Vec(facing.x(), 0, facing.z()).normalize();
        facing = flat.length() == 0 ? new Vec(0, 0, 1) : flat;
        Objects.requireNonNull(skin, "skin");
        riders = List.copyOf(riders);
        Objects.requireNonNull(area, "area");
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(dye, "dye");
        if (dye.isPresent() && (dye.getAsInt() < 0 || dye.getAsInt() >= DYES)) {
            throw new IllegalArgumentException("A dye is a wool colour from 0 to 15, got " + dye.getAsInt());
        }
    }

    public Scene(Vec origin, Vec facing, Optional<String> skin, List<UUID> riders, Area area, Optional<SignedSkin> body) {
        this(origin, facing, skin, riders, area, body, OptionalInt.empty());
    }

    public Scene(Vec origin, Vec facing, Optional<String> skin, List<UUID> riders, Area area) {
        this(origin, facing, skin, riders, area, Optional.empty());
    }

    public Scene(Vec origin, Vec facing, Optional<String> skin, List<UUID> riders) {
        this(origin, facing, skin, riders, Area.around(origin, REACH));
    }

    public Scene(Vec origin, Vec facing, Optional<String> skin) {
        this(origin, facing, skin, List.of());
    }

    public static Scene at(Vec origin, Vec facing) {
        return new Scene(origin, facing, Optional.empty());
    }

    public Scene withBody(Optional<SignedSkin> value) {
        return new Scene(origin, facing, skin, riders, area, value, dye);
    }

    public Scene withDye(OptionalInt value) {
        return new Scene(origin, facing, skin, riders, area, body, value);
    }

    public Vec right() {
        return new Vec(-facing.z(), 0, facing.x());
    }
}
