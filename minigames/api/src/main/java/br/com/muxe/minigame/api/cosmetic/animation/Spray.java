package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class Spray {

    private Spray() {
    }

    static void outwards(Stage stage, String type, Vec at, int count, Reach reach, RandomGenerator random, int... data) {
        for (int index = 0; index < count; index++) {
            stage.particle(type, at, 0, reach.pick(random), 1, data);
        }
    }

    record Reach(double slowest, double fastest, double rise) {

        Vec pick(RandomGenerator random) {
            return Kit.horizontal(random.nextDouble() * 360, Kit.between(random, slowest, fastest)).add(0, random.nextDouble() * rise, 0);
        }
    }
}
