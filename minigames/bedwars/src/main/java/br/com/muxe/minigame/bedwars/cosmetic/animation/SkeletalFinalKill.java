package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class SkeletalFinalKill implements SceneAnimation {

    static final double START_ABOVE = 1.0625;
    static final double DESCENT = 2.5;
    static final int SINKING = 28;
    static final int BEAT_EVERY = 2;
    static final double DROP_ABOVE = 0.6;
    static final double DUST_ABOVE = 1.2;
    static final double SOUND_ABOVE = 1.35;
    static final double DROP_SPREAD = 0.1;
    static final double DROP_LIFT = 0.2;
    static final float FIRST_PITCH = 78 / 63f;
    static final float PITCH_STEP = 3 / 63f;
    static final int DUST = 10;
    static final int CLEAR_DROPS = 30;
    static final int BURST = 29;
    static final int BURST_PIECES = 20;
    static final double BURST_ABOVE = 2;
    static final double MIN_PUSH = 0.1;
    static final double MAX_PUSH = 0.22;
    static final double MIN_LIFT = 0.12;
    static final double MAX_LIFT = 0.22;
    static final int HEAD_GONE = 67;
    static final int END = 68;

    @Override
    public String id() {
        return "esqueletico";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        float yaw = VictimBody.facingKiller(scene);
        Actor head = stage.armorStand(standAt(origin, 0), yaw, new StandStyle(true, false, false, false, false));
        VictimBody.wearHead(scene, head);
        List<Actor> drops = new ArrayList<>();
        int wool = Kit.blockId(Material.WOOL, 0);
        timeline.repeat(1, SINKING, 1, offset -> head.teleport(standAt(origin, offset + 1), spin(yaw, offset + 1), 0));
        timeline.repeat(0, SINKING, BEAT_EVERY, tick -> {
            Vec stand = standAt(origin, tick);
            Actor drop = bone(stage, stand.add(0, DROP_ABOVE, 0), random);
            drop.velocity(new Vec(Kit.between(random, -DROP_SPREAD, DROP_SPREAD), DROP_LIFT, Kit.between(random, -DROP_SPREAD, DROP_SPREAD)));
            drops.add(drop);
            stage.sound("mob.skeleton.hurt", stand.add(0, SOUND_ABOVE, 0), 1, FIRST_PITCH + tick / BEAT_EVERY * PITCH_STEP);
            stage.particle("BLOCK_CRACK", stand.add(0, DUST_ABOVE, 0), DUST, Vec.ZERO, 1, wool);
        });
        timeline.at(CLEAR_DROPS, () -> drops.forEach(Actor::remove));
        List<Actor> pieces = new ArrayList<>();
        timeline.at(BURST, () -> {
            stage.sound("random.explode", origin, 1, 1);
            stage.particle("EXPLOSION_HUGE", origin, 1, Vec.ZERO, 1);
            double first = random.nextDouble() * 360;
            for (int piece = 0; piece < BURST_PIECES; piece++) {
                Actor bone = bone(stage, origin.add(0, BURST_ABOVE, 0), random);
                bone.velocity(burst(first + piece * 360.0 / BURST_PIECES, random));
                pieces.add(bone);
            }
        });
        timeline.at(HEAD_GONE, head::remove);
        timeline.at(END, () -> pieces.forEach(Actor::remove));
        timeline.lasts(END + 1);
    }

    static Vec standAt(Vec origin, int tick) {
        return origin.add(0, START_ABOVE - DESCENT * Math.min(tick, SINKING) / SINKING, 0);
    }

    static float spin(float yaw, int tick) {
        return yaw + 360f * Math.min(tick, SINKING) / SINKING;
    }

    static Vec burst(double degrees, RandomGenerator random) {
        return Kit.horizontal(degrees, Kit.between(random, MIN_PUSH, MAX_PUSH)).add(0, Kit.between(random, MIN_LIFT, MAX_LIFT), 0);
    }

    private static Actor bone(Stage stage, Vec position, RandomGenerator random) {
        return random.nextBoolean() ? stage.item(position, Material.BONE, 0) : stage.item(position, Material.INK_SACK, 15);
    }
}
