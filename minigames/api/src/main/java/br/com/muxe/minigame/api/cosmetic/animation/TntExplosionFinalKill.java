package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class TntExplosionFinalKill implements SceneAnimation {

    static final int RED = 0xFF0000;
    static final double BURST_HEIGHT = 1;
    static final int[] LAUNCHES = {0, 5, 10};
    static final int[] BLASTS = {19, 25, 29};
    static final double SIDEWAYS = 0.2;
    static final double UP = 0.6;

    @Override
    public String id() {
        return "explosao-de-tnt";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Kit.flash(stage, timeline, 0, origin.add(0, BURST_HEIGHT, 0), Kit.burst(Kit.LARGE_BALL, false, true, RED));
        for (int index = 0; index < LAUNCHES.length; index++) {
            Vec velocity = launch(random.nextDouble() * 360);
            int blast = BLASTS[index];
            Actor[] tnt = new Actor[1];
            timeline.at(LAUNCHES[index], () -> {
                stage.sound("creeper.primed", origin, 1, 1);
                tnt[0] = stage.tnt(origin, velocity);
            });
            timeline.at(blast, () -> {
                stage.particle("EXPLOSION_HUGE", origin, 1, Vec.ZERO, 1);
                stage.sound("random.explode", origin, 1, 1);
                tnt[0].remove();
            });
        }
        timeline.lasts(BLASTS[BLASTS.length - 1]);
    }

    static Vec launch(double degrees) {
        return Kit.horizontal(degrees, SIDEWAYS).add(0, UP, 0);
    }
}
