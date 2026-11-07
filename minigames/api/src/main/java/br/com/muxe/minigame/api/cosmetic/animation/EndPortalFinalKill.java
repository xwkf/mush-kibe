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

public final class EndPortalFinalKill implements SceneAnimation {

    static final float SCREAM_VOLUME = 1.5f;
    static final int RINGS = 30;
    static final int RING_POINTS = 15;
    static final double RING_GROWTH = 0.05;
    static final double RING_HEIGHT = 0.1;
    static final int EYES = 20;
    static final int EYE_LAYERS = 5;
    static final double LAYER = 0.445;
    static final double EYE_SPREAD = 1.5;
    static final double EYE_DRIFT = 0.05;
    static final double EYE_LIFT = 0.05;
    static final float PORTAL_PITCH = 1.19f;
    static final int SHATTERS = 4;
    static final int SHATTER_EVERY = 7;
    static final int SHARDS = 8;
    static final double SWIRL_STEP = Math.PI / 20;
    static final double SWIRL_RADIUS = 5;
    static final double SWIRL_DROP = 0.4;
    static final double[] SWIRL_SPEEDS = {5, 7};
    static final int GROWL = 84;
    static final float GROWL_VOLUME = 1.2f;
    static final float GROWL_PITCH = 0.794f;
    static final int END = 114;

    @Override
    public String id() {
        return "portal-do-fim";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        stage.sound("mob.endermen.scream", origin, SCREAM_VOLUME, 1);
        timeline.repeat(0, RINGS - 1, 1, tick -> {
            for (Vec point : ring(origin, tick)) {
                stage.particle("SPELL_WITCH", point, 2, new Vec(0, 0.5, 0), 1);
            }
        });
        for (int shatter = 0; shatter < SHATTERS; shatter++) {
            timeline.at(shatter * SHATTER_EVERY, () -> shatter(stage, origin, random));
        }
        List<Actor> eyes = new ArrayList<>();
        for (int eye = 0; eye < EYES; eye++) {
            int layer = eye % EYE_LAYERS;
            timeline.at(eyeTick(eye), () -> {
                Vec at = origin.add(Kit.between(random, -EYE_SPREAD, EYE_SPREAD), layer * LAYER, Kit.between(random, -EYE_SPREAD, EYE_SPREAD));
                Actor item = stage.item(at, Material.EYE_OF_ENDER, 0);
                item.velocity(new Vec(Kit.between(random, -EYE_DRIFT, EYE_DRIFT), EYE_LIFT, Kit.between(random, -EYE_DRIFT, EYE_DRIFT)));
                stage.sound("mob.endermen.portal", origin, 1, PORTAL_PITCH);
                eyes.add(item);
            });
        }
        timeline.at(GROWL, () -> stage.sound("mob.endermen.idle", origin, GROWL_VOLUME, GROWL_PITCH));
        timeline.at(END, () -> eyes.forEach(Actor::remove));
    }

    static int eyeTick(int eye) {
        return eye * RINGS / EYES;
    }

    static List<Vec> ring(Vec origin, int tick) {
        List<Vec> points = new ArrayList<>(RING_POINTS);
        for (int point = 0; point < RING_POINTS; point++) {
            points.add(origin.add(Kit.horizontal(90 + point * 360.0 / RING_POINTS, RING_GROWTH * tick)).add(0, RING_HEIGHT, 0));
        }
        return points;
    }

    @SuppressWarnings("deprecation")
    private static void shatter(Stage stage, Vec origin, RandomGenerator random) {
        Vec center = new Vec(Math.floor(origin.x()) + 0.5, Math.floor(origin.y()), Math.floor(origin.z()) + 0.5);
        for (int shard = 0; shard < SHARDS; shard++) {
            Vec motion = new Vec(random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
            stage.particle("ITEM_CRACK", center, 0, motion, 1, Material.EYE_OF_ENDER.getId(), 0);
        }
        for (double angle = 0; angle < Math.PI * 2; angle += SWIRL_STEP) {
            Vec inward = new Vec(-Math.cos(angle), 0, -Math.sin(angle));
            Vec edge = center.add(inward.multiply(-SWIRL_RADIUS)).add(0, -SWIRL_DROP, 0);
            for (double speed : SWIRL_SPEEDS) {
                stage.particle("PORTAL", edge, 0, inward, speed);
            }
        }
    }
}
