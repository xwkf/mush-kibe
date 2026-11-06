package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.trail.Flight;
import br.com.muxe.minigame.bedwars.cosmetic.trail.ProjectileTrail;
import java.util.Objects;
import java.util.random.RandomGenerator;

public record SteadyParticleTrail(String id, String particle, int count, Vec spread, double speed) implements ProjectileTrail {

    public static final SteadyParticleTrail FUMACA = still("fumaca", "SMOKE_LARGE");
    public static final SteadyParticleTrail LAVA = still("lava", "LAVA");
    public static final SteadyParticleTrail MAGIA = still("magia", "SPELL_WITCH");
    public static final SteadyParticleTrail VAPOR = still("vapor", "CLOUD");
    public static final SteadyParticleTrail FOGOS_DE_ARTIFICIO = still("fogos-de-artificio", "FIREWORKS_SPARK");
    public static final SteadyParticleTrail SANGRENTO = tinted("sangrento", 1, 0, 0);
    public static final SteadyParticleTrail FAISCA_COSMICA = new SteadyParticleTrail("faisca-cosmica", "PORTAL", 5, new Vec(0.05, 0.05, 0.05), 1);

    public SteadyParticleTrail {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(spread, "spread");
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }

    public static SteadyParticleTrail still(String id, String particle) {
        return new SteadyParticleTrail(id, particle, 0, Vec.ZERO, 1);
    }

    public static SteadyParticleTrail tinted(String id, double red, double green, double blue) {
        return new SteadyParticleTrail(id, "REDSTONE", 0, new Vec(red, green, blue), 1);
    }

    @Override
    public void tick(Flight flight, Stage stage, RandomGenerator random) {
        stage.particle(particle, flight.position(), count, spread, speed);
    }
}
