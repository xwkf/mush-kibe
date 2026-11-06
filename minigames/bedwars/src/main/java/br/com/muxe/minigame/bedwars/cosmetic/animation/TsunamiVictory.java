package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class TsunamiVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final float RAIN_STEP = 0.01f;
    static final int RAIN_TICKS = 100;
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;
    static final int SPLASH_FIRST = 7;
    static final int SPLASH_EVERY = 7;
    static final int[] SPLASH_PITCHES = {37, 44, 50, 56, 63, 69, 75, 81, 88, 94, 100, 107, 113, 119, 126};

    @Override
    public String id() {
        return "tsunami";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Flood flood = new Flood(stage, scene.area(), scene.facing(), scene.origin().y());
        SeaLife life = new SeaLife(stage, flood, scene.area(), scene.origin().y(), random);
        timeline.at(0, () -> {
            scene.riders().forEach(stage::letFly);
            stage.sound("mob.enderdragon.growl", feet(scene, stage), 3, 1);
        });
        timeline.repeat(0, RAIN_TICKS, 1, tick -> stage.rain(rain(tick)));
        timeline.repeat(0, LENGTH - 1, 1, tick -> {
            if (!flood.done()) {
                flood.rise(tick);
            }
            life.live();
        });
        timeline.repeat(LAUNCH_FIRST, LENGTH - 1, LAUNCH_EVERY, tick -> stage.sound("fireworks.launch", feet(scene, stage), 3, 1));
        for (int splash = 0; splash < SPLASH_PITCHES.length; splash++) {
            float pitch = SoundPitch.of(SPLASH_PITCHES[splash]);
            timeline.at(SPLASH_FIRST + splash * SPLASH_EVERY, () -> stage.sound("random.splash", feet(scene, stage), 3, pitch));
        }
        timeline.lasts(LENGTH);
    }

    static float rain(int tick) {
        return Math.min(1, (tick + 1) * RAIN_STEP);
    }

    private static Vec feet(Scene scene, Stage stage) {
        return scene.riders().stream().findFirst().flatMap(stage::eyes).map(eyes -> eyes.add(0, -Riders.EYE_HEIGHT, 0)).orElse(scene.origin());
    }
}
