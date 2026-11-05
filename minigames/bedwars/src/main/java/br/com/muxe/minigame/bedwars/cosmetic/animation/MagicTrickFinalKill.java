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
import org.bukkit.Material;

public final class MagicTrickFinalKill implements SceneAnimation {

    static final String TOP_HAT = "c9de5cc32628749b86ce325bf2ed64c736b3255337e9dbc9dd0dab688b7a3ca5";
    static final double HAT_BELOW = 0.78;
    static final double BRIM_BELOW = 1.03;
    static final int BLACK = 15;
    static final float CLOTH_PITCH = 0.746f;
    static final int APPEAR = 4;
    static final double RABBIT_ABOVE = 0.25;
    static final int RABBIT_TYPE = 18;
    static final byte WHITE = 1;
    static final int HOP_EVERY = 13;
    static final int HOPS = 5;
    static final int SOUNDED_HOPS = 3;
    static final double HOP_FORWARD = 0.67;
    static final double HOP_UP = 0.48;
    static final int END = 58;

    @Override
    public String id() {
        return "passe-de-magica";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec forward = scene.facing();
        float yaw = Vec.ZERO.yawTowards(forward);
        Vec spawn = origin.add(0, RABBIT_ABOVE, 0);
        stage.sound("dig.cloth", origin, 1, CLOTH_PITCH);
        Actor hat = stage.armorStand(origin.add(0, -HAT_BELOW, 0), yaw, new StandStyle(true, true, false, false, false));
        hat.equipSkull(Slot.HELMET, TOP_HAT);
        Actor brim = stage.armorStand(origin.add(0, -BRIM_BELOW, 0), yaw, new StandStyle(true, false, false, false, false));
        brim.equip(Slot.HELMET, Material.CARPET, BLACK);
        Actor[] rabbit = new Actor[1];
        Body[] hop = {Body.living(spawn, Vec.ZERO)};
        timeline.at(APPEAR, () -> {
            stage.particle("CLOUD", origin, 50, new Vec(0.25, 0.25, 0.25), 0);
            rabbit[0] = stage.mob("Rabbit", spawn, yaw);
            rabbit[0].watch(RABBIT_TYPE, WHITE);
        });
        for (int index = 0; index < HOPS; index++) {
            boolean sounded = index < SOUNDED_HOPS;
            timeline.at(hopStart(index), () -> {
                Vec from = hop[0].position();
                hop[0] = Body.living(from, launch(forward));
                rabbit[0].status(1);
                if (sounded) {
                    stage.sound("mob.rabbit.hurt", from, 1, 1);
                }
            });
        }
        timeline.repeat(APPEAR + 1, END - 1, 1, tick -> {
            if (!hop[0].landed()) {
                hop[0].step(stage);
            }
            rabbit[0].teleport(hop[0].position(), yaw, 0);
        });
        timeline.at(END, () -> {
            rabbit[0].remove();
            hat.remove();
            brim.remove();
        });
    }

    static int hopStart(int index) {
        return APPEAR + index * HOP_EVERY;
    }

    static Vec launch(Vec forward) {
        return forward.multiply(HOP_FORWARD).add(0, HOP_UP, 0);
    }
}
