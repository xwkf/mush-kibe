package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.minigame.bedwars.cosmetic.trail.Flight;
import br.com.muxe.minigame.bedwars.cosmetic.trail.ProjectileTrail;
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

    @Override
    public void tick(Flight flight, Stage stage, RandomGenerator random) {
        stage.particle(particle, flight.position(), count, spread.next(random), SPEED);
    }
}
