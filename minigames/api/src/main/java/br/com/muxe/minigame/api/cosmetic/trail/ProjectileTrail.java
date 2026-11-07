package br.com.muxe.minigame.api.cosmetic.trail;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import java.util.random.RandomGenerator;

public interface ProjectileTrail {

    String id();

    void tick(Flight flight, Stage stage, RandomGenerator random);

    default void land(Flight flight, Stage stage, RandomGenerator random) {
    }

    default int linger() {
        return 0;
    }
}
