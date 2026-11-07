package br.com.muxe.minigame.api.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.trail.Flight;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileTrail;
import java.util.Objects;
import java.util.random.RandomGenerator;

public record ParticleTrail(String id, String particle, int count, ParticleSpread spread) implements ProjectileTrail {

    public static final double SPEED = 1;

    public static final ParticleTrail AGUA = single("agua", "WATER_SPLASH");
    public static final ParticleTrail ALDEAO_FURIOSO = single("aldeao-furioso", "VILLAGER_ANGRY");
    public static final ParticleTrail CHAMAS = single("chamas", "FLAME");
    public static final ParticleTrail CHROMA = new ParticleTrail("chroma", "SPELL_MOB", 5, ParticleSpread.RANDOM);
    public static final ParticleTrail CORACOES = single("coracoes", "HEART");
    public static final ParticleTrail CRITICO = single("critico", "CRIT");
    public static final ParticleTrail CRITICO_ENCANTADO = single("critico-encantado", "CRIT_MAGIC");
    public static final ParticleTrail ESPECTRO_VERDE = single("espectro-verde", "VILLAGER_HAPPY");
    public static final ParticleTrail FUMACA = single("fumaca", "SMOKE_LARGE");
    public static final ParticleTrail LAVA = single("lava", "LAVA");
    public static final ParticleTrail MAGIA = single("magia", "SPELL_WITCH");
    public static final ParticleTrail VAPOR = single("vapor", "CLOUD");
    public static final ParticleTrail FOGOS_DE_ARTIFICIO = single("fogos-de-artificio", "FIREWORKS_SPARK");
    public static final ParticleTrail SANGRENTO = tinted("sangrento", 1, 0, 0);
    public static final ParticleTrail FAISCA_COSMICA = new ParticleTrail("faisca-cosmica", "PORTAL", 5, ParticleSpread.fixed(new Vec(0.05, 0.05, 0.05)));

    public ParticleTrail {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(spread, "spread");
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }

    public static ParticleTrail single(String id, String particle) {
        return new ParticleTrail(id, particle, 0, ParticleSpread.NONE);
    }

    public static ParticleTrail tinted(String id, double red, double green, double blue) {
        return new ParticleTrail(id, "REDSTONE", 0, ParticleSpread.fixed(new Vec(red, green, blue)));
    }

    @Override
    public void tick(Flight flight, Stage stage, RandomGenerator random) {
        stage.particle(particle, flight.position(), count, spread.next(random), SPEED);
    }
}
