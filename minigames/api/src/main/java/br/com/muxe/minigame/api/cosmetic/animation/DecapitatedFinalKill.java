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

public final class DecapitatedFinalKill implements SceneAnimation {

    static final StandStyle STAND = new StandStyle(true, false, false, false, false);
    static final double NECK_START = -1.0 / 12;
    static final double RISE = 1.0 / 6;
    static final double STAND_BELOW_NECK = 1.1875;
    static final float SPIN = 360f * 17 / 256;
    static final int FLIGHT = 30;
    static final float FIRST_PITCH = 1.05f;
    static final float PITCH_STEP = 0.05f;
    static final int BLOOD = 10;

    @Override
    public String id() {
        return "decapitado";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        float yaw = VictimBody.facingKiller(scene);
        Actor head = stage.armorStand(stand(origin, 0), yaw, STAND);
        scene.skin().ifPresentOrElse(skin -> head.equipSkull(Slot.HELMET, skin), () -> head.equip(Slot.HELMET, Material.SKULL_ITEM, 3));
        int blood = Kit.blockId(Material.REDSTONE_BLOCK, 0);
        timeline.repeat(0, FLIGHT - 1, 1, tick -> {
            head.teleport(stand(origin, tick), yaw + SPIN * tick, 0);
            if (tick % 2 == 0) {
                Vec neck = neck(origin, tick);
                stage.particle("BLOCK_CRACK", neck, BLOOD, Vec.ZERO, 1, blood);
                stage.sound("note.pling", neck, 1, FIRST_PITCH + tick / 2 * PITCH_STEP);
            }
        });
        timeline.at(FLIGHT, () -> {
            Vec top = neck(origin, FLIGHT);
            stage.sound("random.explode", top, 1, 1);
            stage.particle("EXPLOSION_HUGE", top, 1, Vec.ZERO, 1);
            head.remove();
        });
    }

    static Vec neck(Vec origin, int tick) {
        return origin.add(0, NECK_START + RISE * tick, 0);
    }

    static Vec stand(Vec origin, int tick) {
        return neck(origin, tick).add(0, -STAND_BELOW_NECK, 0);
    }
}
