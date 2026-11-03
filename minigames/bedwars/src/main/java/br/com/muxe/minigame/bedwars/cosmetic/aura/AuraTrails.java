package br.com.muxe.minigame.bedwars.cosmetic.aura;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class AuraTrails {

    private final Map<UUID, Trail> trails = new HashMap<>();

    public void emit(UUID owner, AuraParticle particle, Vec position, AuraSink sink) {
        Trail trail = trails.computeIfAbsent(owner, ignored -> new Trail());
        boolean moved = trail.last != null && AuraPattern.moved(trail.last, position);
        Vec point = moved ? AuraPattern.feet(position) : position.add(AuraPattern.helix(trail.counter));
        trail.last = position;
        trail.counter++;
        sink.spawn(particle, point);
    }

    public void retain(Set<UUID> active) {
        trails.keySet().retainAll(active);
    }

    private static final class Trail {

        private Vec last;
        private long counter;
    }
}
