package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.trail.Flight;
import br.com.muxe.minigame.bedwars.cosmetic.trail.ProjectileTrail;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public record ColorCycleTrail(String id, int packets, List<Vec> colors) implements ProjectileTrail {

    public static final String DUST = "REDSTONE";
    public static final double SPEED = 1;

    public static final ColorCycleTrail ULTRA = new ColorCycleTrail("ultra", 10, List.of(rgb(249, 39, 203)));
    public static final ColorCycleTrail ARCO_IRIS = new ColorCycleTrail("arco-iris", 10, List.of(
        rgb(255, 51, 51),
        rgb(255, 153, 51),
        rgb(255, 255, 51),
        rgb(51, 255, 51),
        rgb(51, 255, 255),
        rgb(51, 51, 255),
        rgb(255, 51, 255)));
    public static final ColorCycleTrail NATALINO = new ColorCycleTrail("natalino", 1, List.of(
        rgb(181, 37, 26),
        rgb(44, 127, 40),
        rgb(255, 255, 255)));

    public ColorCycleTrail {
        Objects.requireNonNull(id, "id");
        colors = List.copyOf(colors);
        if (packets < 1) {
            throw new IllegalArgumentException("packets must be positive");
        }
        if (colors.isEmpty()) {
            throw new IllegalArgumentException("colors must not be empty");
        }
    }

    public static Vec rgb(int red, int green, int blue) {
        return new Vec(red / 255.0, green / 255.0, blue / 255.0);
    }

    public Vec color(int age) {
        return colors.get(age % colors.size());
    }

    @Override
    public void tick(Flight flight, Stage stage, RandomGenerator random) {
        Vec color = color(flight.age());
        for (int packet = 0; packet < packets; packet++) {
            stage.particle(DUST, flight.position(), 0, color, SPEED);
        }
    }
}
