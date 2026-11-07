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

public final class CursedSkeletonFinalKill implements SceneAnimation {

    static final double RING_RADIUS = 2;
    static final double RING_ABOVE = 0.1;
    static final int RING_POINTS = 32;
    static final int RING_LAYERS = 3;
    static final double RING_LAYER_GAP = 0.1;
    static final int RING_EVERY = 5;
    static final int RING_UNTIL = 45;
    static final int BONES = 16;
    static final int BONE_EVERY = 2;
    static final double BONE_ABOVE = 0.1875;
    static final double MIN_BONE_PUSH = 0.22;
    static final double MAX_BONE_PUSH = 0.3;
    static final double MIN_BONE_LIFT = 0.15;
    static final double MAX_BONE_LIFT = 0.23;
    static final int SKULLS = 4;
    static final int SKULL_EVERY = 8;
    static final double SKULL_ABOVE = 0.5;
    static final double SKULL_PUSH = 0.3;
    static final double SKULL_LIFT = 0.2;
    static final int FALLING_SKULL = 9;
    static final double FALLING_SKULL_ABOVE = 3;
    static final double FALLING_SKULL_SPEED = 0.12;
    static final double FIREWORK_ABOVE = 1;
    static final int DEATH_CRY = 48;
    static final int END = 51;
    static final int BLACK = 0x000000;

    @Override
    public String id() {
        return "esqueleto-amaldicoado";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        stage.sound("mob.blaze.death", origin, 1.5f, 31 / 63f);
        Kit.flash(stage, timeline, 0, origin.add(0, FIREWORK_ABOVE, 0), Kit.burst(Kit.LARGE_BALL, false, true, BLACK));
        stage.silentLightning(origin);
        stage.sound("ambient.weather.thunder", origin, 1.5f, 1);
        timeline.repeat(0, RING_UNTIL, RING_EVERY, tick -> ring(stage, origin));
        List<Actor> thrown = new ArrayList<>();
        double first = random.nextDouble() * 360;
        timeline.repeat(0, (BONES - 1) * BONE_EVERY, BONE_EVERY, tick -> {
            Actor bone = stage.item(origin.add(0, BONE_ABOVE, 0), Material.BONE, 0);
            bone.velocity(bone(first + tick / BONE_EVERY * 360.0 / BONES, random));
            thrown.add(bone);
            stage.sound("mob.skeleton.step", origin, 0.8f, 1);
        });
        timeline.repeat(0, (SKULLS - 1) * SKULL_EVERY, SKULL_EVERY, tick -> {
            Actor skull = stage.item(origin.add(0, SKULL_ABOVE, 0), Material.SKULL_ITEM, 0);
            skull.velocity(Kit.horizontal(first + tick / SKULL_EVERY * 360.0 / SKULLS, SKULL_PUSH).add(0, SKULL_LIFT, 0));
            thrown.add(skull);
        });
        timeline.at(FALLING_SKULL, () -> {
            Actor skull = stage.item(origin.add(0, FALLING_SKULL_ABOVE, 0), Material.SKULL_ITEM, 0);
            skull.velocity(new Vec(0, -FALLING_SKULL_SPEED, 0));
            thrown.add(skull);
            stage.sound("mob.skeleton.say", origin, 1, 50 / 63f);
        });
        timeline.at(DEATH_CRY, () -> stage.sound("mob.skeleton.death", origin, 1.2f, 56 / 63f));
        timeline.at(END, () -> thrown.forEach(Actor::remove));
        timeline.lasts(END + 1);
    }

    static Vec bone(double degrees, RandomGenerator random) {
        return Kit.horizontal(degrees, Kit.between(random, MIN_BONE_PUSH, MAX_BONE_PUSH)).add(0, Kit.between(random, MIN_BONE_LIFT, MAX_BONE_LIFT), 0);
    }

    static List<Vec> ringPoints(Vec origin) {
        List<Vec> points = new ArrayList<>();
        for (int point = 0; point < RING_POINTS; point++) {
            double degrees = point * 360.0 / RING_POINTS;
            for (int layer = 0; layer < RING_LAYERS; layer++) {
                points.add(origin.add(Kit.horizontal(degrees, RING_RADIUS + layer * RING_LAYER_GAP)).add(0, RING_ABOVE, 0));
            }
        }
        return points;
    }

    private static void ring(Stage stage, Vec origin) {
        ringPoints(origin).forEach(point -> Kit.dust(stage, point, 0, 0, 0));
    }
}
