package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class Riders {

    static final double EYE_HEIGHT = 1.62;
    static final double FLAT_LOOK = 0.01;

    private Riders() {
    }

    static List<Rider> of(Scene scene, Stage stage) {
        if (scene.riders().isEmpty()) {
            return List.of(new Rider(List.of(), scene.origin(), scene.facing()));
        }
        List<Rider> riders = new ArrayList<>();
        riders.add(new Rider(List.of(scene.riders().getFirst()), scene.origin(), scene.facing()));
        for (UUID player : scene.riders().subList(1, scene.riders().size())) {
            Optional<Vec> eyes = stage.eyes(player);
            if (eyes.isEmpty()) {
                continue;
            }
            Vec ground = Ground.surface(stage::solid, eyes.get().add(0, -EYE_HEIGHT, 0));
            Vec facing = stage.look(player).map(look -> new Vec(look.x(), 0, look.z())).filter(look -> look.length() > FLAT_LOOK).map(Vec::normalize).orElse(scene.facing());
            riders.add(new Rider(List.of(player), ground, facing));
        }
        return riders;
    }

    record Rider(List<UUID> seated, Vec ground, Vec facing) {

        float yaw() {
            return Vec.ZERO.yawTowards(facing);
        }
    }
}
