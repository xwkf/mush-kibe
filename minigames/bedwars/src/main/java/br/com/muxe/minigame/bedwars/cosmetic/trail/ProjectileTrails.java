package br.com.muxe.minigame.bedwars.cosmetic.trail;

import java.util.List;
import java.util.Optional;

public final class ProjectileTrails {

    private static final List<ProjectileTrail> ALL = List.of();

    private ProjectileTrails() {
    }

    public static Optional<ProjectileTrail> find(String id) {
        return ALL.stream().filter(trail -> trail.id().equals(id)).findFirst();
    }

    public static List<ProjectileTrail> all() {
        return ALL;
    }
}
