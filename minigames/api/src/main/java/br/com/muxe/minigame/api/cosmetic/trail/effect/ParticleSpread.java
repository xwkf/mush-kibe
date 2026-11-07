package br.com.muxe.minigame.api.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Objects;
import java.util.random.RandomGenerator;

@FunctionalInterface
public interface ParticleSpread {

    ParticleSpread NONE = fixed(Vec.ZERO);
    ParticleSpread RANDOM = random -> new Vec(random.nextFloat(), random.nextFloat(), random.nextFloat());

    Vec next(RandomGenerator random);

    static ParticleSpread fixed(Vec spread) {
        Objects.requireNonNull(spread, "spread");
        return random -> spread;
    }
}
