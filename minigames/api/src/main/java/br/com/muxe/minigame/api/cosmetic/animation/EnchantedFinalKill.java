package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class EnchantedFinalKill implements SceneAnimation {

    static final int[] FIREWORK_COLORS = {0xF08080};
    static final double FIREWORK_ABOVE = 1;
    static final double[] LEAF_HEIGHTS = {0.1875, 0.78125, 1.375};
    static final int LEAVES_PER_HEIGHT = 4;
    static final double LEAF_TWIST = 30;
    static final double LEAF_RADIUS = 0.2;
    static final double MIN_LEAF_PUSH = 0.08;
    static final double MAX_LEAF_PUSH = 0.145;
    static final double MIN_LEAF_LIFT = 0.31;
    static final double MAX_LEAF_LIFT = 0.39;
    static final int LEAF_LIMIT = 40;
    static final int[] LEAF_KINDS = {0, 3};
    static final double PUFF_ABOVE = 0.2;
    static final int PUFF = 15;
    static final int MUSHROOMS = 9;

    @Override
    public String id() {
        return "encantado";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Kit.flash(stage, timeline, 0, origin.add(0, FIREWORK_ABOVE, 0), Kit.burst(Kit.BURST, false, true, FIREWORK_COLORS));
        stage.sound("dig.grass", origin, 1, 50 / 63f);
        stage.sound("fireworks.blast", origin, 0.8f, 75 / 63f);
        double twist = random.nextDouble() * 360;
        for (int height = 0; height < LEAF_HEIGHTS.length; height++) {
            for (int leaf = 0; leaf < LEAVES_PER_HEIGHT; leaf++) {
                double degrees = twist + height * LEAF_TWIST + leaf * 360.0 / LEAVES_PER_HEIGHT;
                leaf(stage, timeline, origin.add(Kit.horizontal(degrees, LEAF_RADIUS)).add(0, LEAF_HEIGHTS[height], 0), degrees, random);
            }
        }
        double first = random.nextDouble() * 360;
        for (int index = 0; index < MUSHROOMS; index++) {
            Mushroom.grow(stage, timeline, origin, index, first + index * Mushroom.SPACING, random);
        }
        timeline.lasts(Mushroom.start(MUSHROOMS - 1) + Mushroom.LIFE + 1);
    }

    static Vec throwing(double degrees, RandomGenerator random) {
        return Kit.horizontal(degrees, Kit.between(random, MIN_LEAF_PUSH, MAX_LEAF_PUSH)).add(0, Kit.between(random, MIN_LEAF_LIFT, MAX_LEAF_LIFT), 0);
    }

    static void puff(Stage stage, Vec at, float volume, float pitch) {
        stage.particle("CLOUD", at, PUFF, new Vec(0.01, 0.01, 0.01), 0.1);
        stage.sound("mob.chicken.plop", at, volume, pitch);
    }

    private static void leaf(Stage stage, Timeline timeline, Vec start, double degrees, RandomGenerator random) {
        Vec velocity = throwing(degrees, random);
        Actor item = stage.item(start, Material.LEAVES, LEAF_KINDS[random.nextInt(LEAF_KINDS.length)]);
        item.velocity(velocity);
        Body body = Body.item(start, velocity);
        float pitch = (float) Kit.between(random, 1.0, 1.35);
        timeline.repeat(1, LEAF_LIMIT, 1, offset -> {
            if (item.removed()) {
                return;
            }
            if (body.step(stage)) {
                puff(stage, body.position().add(0, PUFF_ABOVE, 0), 0.25f, pitch);
                item.remove();
            } else if (offset + 1 == LEAF_LIMIT) {
                item.remove();
            }
        });
    }
}
