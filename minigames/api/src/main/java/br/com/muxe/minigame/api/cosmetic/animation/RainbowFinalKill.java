package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class RainbowFinalKill implements SceneAnimation {

    static final int[] WOOLS = {14, 1, 4, 5, 3, 11, 10};
    static final int PER_WAVE = 14;
    static final int[] WAVES = {0, 14};
    static final double LAUNCH_HEIGHT = 1;
    static final double SPEED = 0.2;
    static final double MIN_LIFT = 0.3;
    static final double MAX_LIFT = 0.44;
    static final int SPARKLE_END = 48;
    static final int SPARKLES = 5;
    static final double SPARKLE_RADIUS = 1.5;
    static final double SPARKLE_HEIGHT = 2;
    static final double HUE_STEP = 15;
    static final int END = 100;

    @Override
    public String id() {
        return "arco-iris";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec launch = origin.add(0, LAUNCH_HEIGHT, 0);
        double start = random.nextDouble() * 360;
        List<Actor> wool = new ArrayList<>();
        for (int wave : WAVES) {
            timeline.at(wave, () -> {
                stage.sound("random.levelup", launch, 1, 1.49f);
                for (int index = 0; index < PER_WAVE; index++) {
                    Actor item = stage.item(launch, Material.WOOL, color(index));
                    item.velocity(Kit.horizontal(direction(start, index), SPEED).add(0, Kit.between(random, MIN_LIFT, MAX_LIFT), 0));
                    wool.add(item);
                }
            });
        }
        timeline.repeat(0, SPARKLE_END, 1, tick -> {
            double[] color = Kit.hsv(hue(tick), 1, 1);
            for (int sparkle = 0; sparkle < SPARKLES; sparkle++) {
                double radius = SPARKLE_RADIUS * Math.sqrt(random.nextDouble());
                Vec at = origin.add(Kit.horizontal(random.nextDouble() * 360, radius)).add(0, random.nextDouble() * SPARKLE_HEIGHT, 0);
                Kit.dust(stage, at, color[0], color[1], color[2]);
            }
        });
        timeline.at(SPARKLE_END, () -> stage.sound("random.orb", launch, 1, 1));
        timeline.at(END, () -> wool.forEach(Actor::remove));
        timeline.lasts(END + 1);
    }

    static int color(int index) {
        return WOOLS[index % WOOLS.length];
    }

    static double direction(double start, int index) {
        return start + index * 360.0 / PER_WAVE;
    }

    static double hue(int tick) {
        return tick * HUE_STEP % 360;
    }
}
