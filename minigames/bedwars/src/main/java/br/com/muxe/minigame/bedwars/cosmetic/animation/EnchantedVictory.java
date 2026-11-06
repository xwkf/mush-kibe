package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class EnchantedVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final double AHEAD = 7;
    static final double MUSHROOM_RADIUS = 17;
    static final double GNOME_RADIUS = 10;
    static final int FIRST_SMALL = 1;
    static final int LAST_SMALL = 119;
    static final int SMALL_EVERY = 2;
    static final int GIANTS = 10;
    static final int FIRST_GIANT = 2;
    static final int GIANT_EVERY = 8;
    static final int GNOMES = 15;
    static final int FIRST_GNOME = 6;
    static final int GNOME_EVERY = 3;
    static final double MIN_CHEER = 1.2;
    static final double MAX_CHEER = 1.6;
    static final Vec DUST_SPREAD = new Vec(0.2, 0.05, 0.2);
    static final Vec CLOUD_SPREAD = new Vec(0.3, 0.5, 0.3);

    @Override
    public String id() {
        return "encantado";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Grove grove = new Grove(stage, anchor(scene));
        List<Gnome> gnomes = new ArrayList<>();
        timeline.repeat(FIRST_SMALL, LAST_SMALL, SMALL_EVERY, index -> plant(stage, grove, random));
        for (int giant = 0; giant < GIANTS; giant++) {
            int start = FIRST_GIANT + giant * GIANT_EVERY;
            timeline.at(start, () -> grove.spot(random, MUSHROOM_RADIUS).ifPresent(spot -> grow(stage, timeline, grove, random, spot, start)));
        }
        for (int gnome = 0; gnome < GNOMES; gnome++) {
            int arrival = FIRST_GNOME + gnome * GNOME_EVERY;
            timeline.at(arrival - 1, () -> grove.spot(random, GNOME_RADIUS).ifPresent(spot -> {
                stage.particle("CLOUD", spot.add(0, 1, 0), 15, CLOUD_SPREAD, 0.05);
                stage.sound("mob.chicken.plop", spot, 0.5f, cheer(random));
                timeline.at(arrival, () -> gnomes.add(Gnome.spawn(stage, spot, random.nextFloat() * Gnome.TURN - Gnome.TURN / 2)));
            }));
        }
        timeline.repeat(FIRST_GNOME + 1, LENGTH - 1, 1, tick -> gnomes.forEach(gnome -> gnome.walk(grove::solid, random)));
        timeline.at(LENGTH, () -> gnomes.forEach(Gnome::remove));
        timeline.lasts(LENGTH);
    }

    static Vec anchor(Scene scene) {
        return scene.origin().add(scene.facing().multiply(AHEAD));
    }

    static float cheer(RandomGenerator random) {
        return (float) Kit.between(random, MIN_CHEER, MAX_CHEER);
    }

    private static void plant(Stage stage, Grove grove, RandomGenerator random) {
        grove.spot(random, MUSHROOM_RADIUS).ifPresent(spot -> {
            int x = (int) Math.floor(spot.x());
            int y = (int) Math.floor(spot.y());
            int z = (int) Math.floor(spot.z());
            if (!grove.free(x, y, z)) {
                return;
            }
            Material mushroom = random.nextBoolean() ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM;
            grove.grow(List.of(new BlockChange(x, y, z, mushroom, 0)));
            stage.particle("BLOCK_DUST", spot.add(0, 0.2, 0), 6, DUST_SPREAD, 0.05, Kit.blockId(Material.DIRT, 0));
            stage.sound("random.pop", spot.add(0, 0.125, 0), 0.3f, cheer(random));
        });
    }

    private static void grow(Stage stage, Timeline timeline, Grove grove, RandomGenerator random, Vec spot, int start) {
        int height = GiantMushroom.MIN_HEIGHT + random.nextInt(GiantMushroom.MAX_HEIGHT - GiantMushroom.MIN_HEIGHT + 1);
        GiantMushroom mushroom = new GiantMushroom((int) Math.floor(spot.x()), (int) Math.floor(spot.y()), (int) Math.floor(spot.z()), height, random.nextBoolean());
        mushroom.grow(stage, grove, 0);
        for (int step = 1; step < mushroom.steps(); step++) {
            int at = step;
            timeline.at(start + GiantMushroom.delay(step), () -> mushroom.grow(stage, grove, at));
        }
    }
}
