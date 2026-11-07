package br.com.muxe.minigame.api.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.trail.Flight;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileTrail;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public record RandomParticleTrail(String id, List<String> particles) implements ProjectileTrail {

    public static final double SPEED = 1;

    public static final RandomParticleTrail MEGA_LOUCURA = new RandomParticleTrail("mega-loucura", List.of(
        "EXPLOSION_NORMAL",
        "EXPLOSION_LARGE",
        "FIREWORKS_SPARK",
        "WATER_SPLASH",
        "CRIT",
        "CRIT_MAGIC",
        "SMOKE_NORMAL",
        "SMOKE_LARGE",
        "SPELL",
        "SPELL_INSTANT",
        "SPELL_MOB",
        "SPELL_WITCH",
        "DRIP_WATER",
        "DRIP_LAVA",
        "VILLAGER_ANGRY",
        "VILLAGER_HAPPY",
        "TOWN_AURA",
        "NOTE",
        "PORTAL",
        "ENCHANTMENT_TABLE",
        "FLAME",
        "LAVA",
        "CLOUD",
        "REDSTONE",
        "SNOWBALL",
        "SNOW_SHOVEL",
        "SLIME",
        "HEART"));

    public RandomParticleTrail {
        Objects.requireNonNull(id, "id");
        particles = List.copyOf(particles);
        if (particles.isEmpty()) {
            throw new IllegalArgumentException("particles must not be empty");
        }
    }

    @Override
    public void tick(Flight flight, Stage stage, RandomGenerator random) {
        stage.particle(particles.get(random.nextInt(particles.size())), flight.position(), 0, Vec.ZERO, SPEED);
    }
}
