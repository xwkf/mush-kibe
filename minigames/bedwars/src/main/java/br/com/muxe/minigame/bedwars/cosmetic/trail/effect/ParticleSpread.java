package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public enum ParticleSpread {
    NONE {
        @Override
        public Vec next(RandomGenerator random) {
            return Vec.ZERO;
        }
    },
    RANDOM {
        @Override
        public Vec next(RandomGenerator random) {
            return new Vec(random.nextFloat(), random.nextFloat(), random.nextFloat());
        }
    };

    public abstract Vec next(RandomGenerator random);
}
