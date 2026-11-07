package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class CookiesAndMilkFinalKill implements SceneAnimation {

    static final int TREATS = 15;
    static final double TURN = 22.5;
    static final double LAUNCH_HEIGHT = 1.5;
    static final double SPEED = 0.2;
    static final double LIFT = 0.1;
    static final int LIFE = 10;
    static final double CRUMB_DISTANCE = 2.15;
    static final int CRUMBLE = 12;
    static final int FIREWORK_COLOR = 0xE79041;
    static final float FIRST_PITCH = 0.55f;
    static final float PITCH_STEP = 0.05f;
    static final double[] MILK = {1, 1, 1};
    static final double[] COOKIE = {1, 0.5, 0};

    @Override
    public String id() {
        return "biscoitos-e-leite";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec launch = origin.add(0, LAUNCH_HEIGHT, 0);
        double start = random.nextDouble() * 360;
        Kit.flash(stage, timeline, 0, launch, Kit.burst(Kit.STAR, false, true, FIREWORK_COLOR));
        for (int index = 0; index < TREATS; index++) {
            treat(stage, timeline, origin, launch, index, start + TURN * (index + 1));
        }
        timeline.at(CRUMBLE, () -> {
            stage.sound("mob.zombie.woodbreak", origin, 1, 0.746f);
            stage.particle("EXPLOSION_HUGE", origin, 1, Vec.ZERO, 1);
        });
        timeline.lasts(TREATS - 1 + LIFE + 1);
    }

    static boolean milk(int index) {
        return index % 2 == 0;
    }

    static Vec velocity(double degrees) {
        return Kit.horizontal(degrees, SPEED).add(0, LIFT, 0);
    }

    static Vec crumbs(Vec origin, double degrees) {
        return origin.add(Kit.horizontal(degrees, CRUMB_DISTANCE));
    }

    static float pitch(int index) {
        return FIRST_PITCH + PITCH_STEP * index;
    }

    private static void treat(Stage stage, Timeline timeline, Vec origin, Vec launch, int index, double degrees) {
        Actor[] item = new Actor[1];
        timeline.at(index, () -> {
            item[0] = stage.item(launch, milk(index) ? Material.MILK_BUCKET : Material.COOKIE, 0);
            item[0].velocity(velocity(degrees));
            stage.sound("note.pling", origin, 1, pitch(index));
        });
        timeline.at(index + LIFE, () -> {
            double[] color = milk(index) ? MILK : COOKIE;
            Kit.dust(stage, crumbs(origin, degrees), color[0], color[1], color[2]);
            item[0].remove();
        });
    }
}
