package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.random.RandomGenerator;

public final class ElectrifiedFinalKill implements SceneAnimation {

    static final List<Integer> STRIKES = List.of(5, 12, 19, 29, 36);
    static final int FIRST_FLICKER = 5;
    static final int LAST_FLICKER = 35;
    static final int SPARK_EVERY = 5;
    static final int DEATH = 41;
    static final int END = 59;
    static final double BOLT_ABOVE = 1.94;
    static final double SPARK_ABOVE = 1.5;
    static final double SKELETON_BELOW = 0.0625;
    static final int SPARKS = 25;
    static final float BUZZ_PITCH = 110 / 63f;
    static final int DEATH_STATUS = 3;

    @Override
    public String id() {
        return "eletrizante";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        float yaw = VictimBody.facingKiller(scene);
        Actor body = VictimBody.spawn(stage, scene, origin, yaw);
        Actor skeleton = stage.mob("Skeleton", origin.add(0, -SKELETON_BELOW, 0), yaw);
        skeleton.watch(VictimBody.FLAGS, VictimBody.INVISIBLE);
        STRIKES.forEach(tick -> timeline.at(tick, () -> stage.silentLightning(origin.add(0, BOLT_ABOVE, 0))));
        timeline.repeat(FIRST_FLICKER, LAST_FLICKER, SPARK_EVERY, offset -> {
            Vec sparks = origin.add(0, SPARK_ABOVE, 0);
            stage.particle("CRIT", sparks, SPARKS, Vec.ZERO, 1);
            stage.particle("CRIT_MAGIC", sparks, SPARKS, Vec.ZERO, 1);
        });
        timeline.repeat(FIRST_FLICKER, LAST_FLICKER, 1, offset -> {
            boolean bones = shocked(offset + 1);
            body.watch(VictimBody.FLAGS, bones ? VictimBody.INVISIBLE : VictimBody.VISIBLE);
            skeleton.watch(VictimBody.FLAGS, bones ? VictimBody.VISIBLE : VictimBody.INVISIBLE);
            stage.sound("game.neutral.hurt", origin, 1, BUZZ_PITCH);
        });
        timeline.at(DEATH, () -> {
            skeleton.status(DEATH_STATUS);
            stage.sound("random.explode", origin, 1, 1);
            stage.sound("mob.skeleton.death", origin, 1, 1);
            stage.particle("EXPLOSION_HUGE", origin, 1, Vec.ZERO, 1);
        });
        timeline.at(END, () -> {
            skeleton.remove();
            body.remove();
        });
        timeline.lasts(END + 1);
    }

    static boolean shocked(int toggles) {
        return toggles % 2 == 1;
    }
}
