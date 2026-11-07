package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class Spill {

    private Spill() {
    }

    static void drop(Stage stage, Timeline timeline, Vec at, Material material, int data, Vec velocity, int until) {
        Actor item = stage.item(at, material, data);
        item.velocity(velocity);
        timeline.at(until, item::remove);
    }

    static Vec scattered(RandomGenerator random, double sideways, double minUp, double maxUp) {
        return new Vec(Kit.between(random, -sideways, sideways), Kit.between(random, minUp, maxUp), Kit.between(random, -sideways, sideways));
    }
}
