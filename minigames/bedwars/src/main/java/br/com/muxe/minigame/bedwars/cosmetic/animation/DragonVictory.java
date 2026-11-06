package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import java.util.List;
import java.util.random.RandomGenerator;

public final class DragonVictory implements SceneAnimation {

    static final int LENGTH = 200;

    @Override
    public String id() {
        return "dragao";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        List<DragonRide> rides = Riders.of(scene, stage).stream().map(DragonRide::new).toList();
        Tunnel tunnel = new Tunnel(stage, random);
        timeline.at(0, () -> rides.forEach(ride -> ride.start(stage)));
        timeline.repeat(1, LENGTH - 1, 1, tick -> rides.forEach(ride -> {
            ride.fly();
            tunnel.dig(ride.position(), tick);
            ride.growl(stage, random);
        }));
        timeline.repeat(1, LENGTH - 1, 1, tunnel::update);
        timeline.at(LENGTH, () -> {
            rides.forEach(DragonRide::remove);
            tunnel.clear();
        });
    }
}
