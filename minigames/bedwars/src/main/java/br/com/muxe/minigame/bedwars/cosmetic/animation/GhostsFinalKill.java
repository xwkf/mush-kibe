package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class GhostsFinalKill implements SceneAnimation {

    static final int[] ANNOUNCES = {0, 4, 9};
    static final double SPAWN_RADIUS = 1;
    static final double MIN_SPAWN_HEIGHT = 0.25;
    static final double MAX_SPAWN_HEIGHT = 0.75;
    static final double FIRST_LIFT = 0.3125;
    static final double HEAD_ABOVE_BASE = 1.75;
    static final double TAIL_ABOVE_BASE = 1.25;
    static final double SOUL_HEIGHT = 1.25;
    static final int LIFE = 47;
    static final int TRAIL_EVERY = 5;
    static final float DEATH_PITCH = 0.5f;
    static final float LOOP_PITCH = 0.75f;
    static final float MIN_FIZZ_PITCH = 1.5f;
    static final float MAX_FIZZ_PITCH = 2;

    @Override
    public String id() {
        return "fantasmas";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        stage.particle("CLOUD", origin.add(0, SOUL_HEIGHT, 0), 30, new Vec(0.15, 0.3, 0.15), 0.1);
        for (int announce : ANNOUNCES) {
            Vec spawn = origin.add(Kit.horizontal(random.nextDouble() * 360, random.nextDouble() * SPAWN_RADIUS))
                .add(0, Kit.between(random, MIN_SPAWN_HEIGHT, MAX_SPAWN_HEIGHT), 0);
            ghost(stage, timeline, origin, spawn, announce, random);
        }
        timeline.lasts(ANNOUNCES[ANNOUNCES.length - 1] + LIFE + 1);
    }

    private static void ghost(Stage stage, Timeline timeline, Vec origin, Vec spawn, int announce, RandomGenerator random) {
        float yaw = (float) (random.nextDouble() * 360 - 180);
        GhostDrift drift = new GhostDrift(origin, spawn.add(0, FIRST_LIFT, 0), yaw, random);
        float fizz = (float) Kit.between(random, MIN_FIZZ_PITCH, MAX_FIZZ_PITCH);
        Actor[] ghost = new Actor[1];
        timeline.at(announce, () -> {
            Vec head = spawn.add(0, HEAD_ABOVE_BASE, 0);
            stage.particle("CLOUD", head, 15, new Vec(0.01, 0.01, 0.01), 0.1);
            stage.sound("mob.bat.death", head, 1, DEATH_PITCH);
            stage.sound("mob.bat.loop", head, 1, LOOP_PITCH);
        });
        int born = announce + 1;
        timeline.at(born, () -> {
            ghost[0] = stage.armorStand(spawn, yaw, StandStyle.HIDDEN);
            ghost[0].equipSkull(Slot.HELMET, HauntedVictory.GHOST);
            ghost[0].teleport(drift.position(), yaw, 0);
        });
        timeline.repeat(born + 1, born + LIFE - 1, 1, tick -> {
            drift.step();
            ghost[0].teleport(drift.position(), drift.yaw(), 0);
            if ((tick + 1) % TRAIL_EVERY == 0) {
                stage.particle("CLOUD", drift.position().add(0, TAIL_ABOVE_BASE, 0), 1, new Vec(0.1, 0.1, 0.1), 0.01);
            }
        });
        timeline.at(born + LIFE, () -> {
            Vec head = drift.position().add(0, HEAD_ABOVE_BASE, 0);
            ghost[0].remove();
            stage.particle("CLOUD", head, 15, new Vec(0.01, 0.01, 0.01), 0.1);
            stage.sound("random.fizz", head, 2, fizz);
        });
    }
}
