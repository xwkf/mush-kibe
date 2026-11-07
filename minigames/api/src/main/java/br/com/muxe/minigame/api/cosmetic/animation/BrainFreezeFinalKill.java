package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class BrainFreezeFinalKill implements SceneAnimation {

    static final double HEAD_BELOW = 0.94;
    static final double ICE_BELOW = 1.69;
    static final double PARTICLE_ABOVE_ICE = 1.75;
    static final double BACK_PER_TICK = 0.2;
    static final double UP_PER_TICK = 0.25;
    static final int FLIGHT = 28;
    static final float FIRST_PITCH = 1.05f;
    static final float PITCH_STEP = 0.05f;
    static final int SHARDS = 10;

    @Override
    public String id() {
        return "brain-freeze";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec back = scene.facing();
        Actor head = stage.armorStand(origin.add(0, -HEAD_BELOW, 0), 0, new StandStyle(true, true, false, false, false));
        scene.skin().ifPresentOrElse(skin -> head.equipSkull(Slot.HELMET, skin), () -> head.equip(Slot.HELMET, Material.SKULL_ITEM, 3));
        Actor ice = stage.armorStand(origin.add(0, -ICE_BELOW, 0), 0, new StandStyle(true, false, false, false, false));
        ice.equip(Slot.HELMET, Material.ICE, 0);
        int iceBlock = Kit.blockId(Material.ICE, 0);
        timeline.repeat(1, FLIGHT - 1, 1, tick -> {
            Vec offset = flight(back, tick);
            head.teleport(origin.add(0, -HEAD_BELOW, 0).add(offset), 0, 0);
            ice.teleport(origin.add(0, -ICE_BELOW, 0).add(offset), 0, 0);
            if (tick % 2 == 0) {
                Vec at = origin.add(0, -ICE_BELOW + PARTICLE_ABOVE_ICE, 0).add(offset);
                stage.particle("BLOCK_CRACK", at, SHARDS, new Vec(0.2, 0.2, 0.2), 0, iceBlock);
                stage.sound("note.pling", at, 1, FIRST_PITCH + tick / 2 * PITCH_STEP);
            }
        });
        stage.sound("note.pling", origin, 1, FIRST_PITCH);
        timeline.at(FLIGHT, () -> {
            Vec at = origin.add(0, -ICE_BELOW + PARTICLE_ABOVE_ICE, 0).add(flight(back, FLIGHT));
            stage.sound("random.explode", at, 1, 1);
            for (int shard = 0; shard < SHARDS; shard++) {
                stage.sound("dig.glass", at, 1, 1);
            }
            stage.particle("EXPLOSION_HUGE", at, 1, Vec.ZERO, 1);
            stage.particle("BLOCK_CRACK", at, SHARDS * 3, new Vec(0.5, 0.5, 0.5), 0, iceBlock);
            head.remove();
            ice.remove();
        });
        timeline.lasts(FLIGHT + 1);
    }

    static Vec flight(Vec back, int tick) {
        return back.multiply(BACK_PER_TICK * tick).add(0, UP_PER_TICK * tick, 0);
    }
}
