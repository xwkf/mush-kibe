package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.trail.Flight;
import br.com.muxe.minigame.bedwars.cosmetic.trail.ProjectileTrail;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class FortuneSpiralTrail implements ProjectileTrail {

    public static final FortuneSpiralTrail ESPIRAL_DA_FORTUNA = new FortuneSpiralTrail();

    public static final String ID = "espiral-da-fortuna";
    public static final Spiral SPIRAL = new Spiral(0.5, Math.PI, 2 * Math.PI / 9);
    public static final Vec GOLD = ColorCycleTrail.rgb(255, 198, 23);
    public static final int DUST_PACKETS = 2;
    public static final double SPEED = 1;
    public static final Material SUNFLOWER = Material.DOUBLE_PLANT;

    private final DroppedItems items = new DroppedItems();

    private FortuneSpiralTrail() {
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void tick(Flight flight, Stage stage, RandomGenerator random) {
        items.expire(stage, flight.age());
        Vec point = flight.position().add(SPIRAL.offset(flight.velocity(), flight.age()));
        for (int packet = 0; packet < DUST_PACKETS; packet++) {
            stage.particle(ColorCycleTrail.DUST, point, 0, GOLD, SPEED);
        }
        items.drop(stage, point, SUNFLOWER, 0, flight.age(), random);
    }

    @Override
    public void land(Flight flight, Stage stage, RandomGenerator random) {
        items.forget(stage);
    }

    @Override
    public int linger() {
        return DroppedItems.LIFETIME;
    }
}
