package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class Mushroom {

    static final List<String> CAPS = List.of(
        "578897b83f51fb96b59ba418ff0868cef7bdf661e315ba5dbac51d876d1d15d",
        "2278b4061f63755ba5a85c1d2491c261d6bd4a5d0536a17bdad934570f3cbfee"
    );
    static final double SPACING = 40;
    static final int FIRST = 3;
    static final int EVERY = 2;
    static final int LIFE = 23;
    static final double MIN_RADIUS = 0.6;
    static final double MAX_RADIUS = 0.9;
    static final double HEAD_ABOVE_BASE = 1.4375;
    static final int[] HOP = {11, 9, 6, 3, 1, -2, -4, -7, -9, -8};
    static final double STEP = 1 / 32.0;
    static final double FIRST_SPIN = 67.5;
    static final double SPIN_DECAY = 0.82;
    static final int SPINNING = 20;
    static final float FIRST_PITCH = 31 / 63f;
    static final float PITCH_STEP = 5 / 63f;
    static final double DIRT_ABOVE = 0.2;
    static final double PUFF_ABOVE = 0.3;

    private Mushroom() {
    }

    static int start(int index) {
        return FIRST + index * EVERY;
    }

    static double lift(int ticks) {
        int total = 0;
        for (int tick = 0; tick < Math.min(ticks, HOP.length); tick++) {
            total += HOP[tick];
        }
        return total * STEP;
    }

    static double drift(int ticks) {
        return Math.min(ticks, HOP.length) * STEP;
    }

    static double turned(int ticks) {
        double turned = 0;
        for (int tick = 0; tick < Math.min(ticks, SPINNING); tick++) {
            turned += FIRST_SPIN * Math.pow(SPIN_DECAY, tick);
        }
        return turned;
    }

    static void grow(Stage stage, Timeline timeline, Vec origin, int index, double degrees, RandomGenerator random) {
        Vec ground = origin.add(Kit.horizontal(degrees, Kit.between(random, MIN_RADIUS, MAX_RADIUS)));
        Vec outward = Kit.horizontal(degrees, 1);
        float yaw = origin.yawTowards(ground);
        String cap = CAPS.get(random.nextInt(CAPS.size()));
        float pitch = (float) Kit.between(random, 1.0, 1.4);
        int start = start(index);
        Actor[] stand = new Actor[1];
        timeline.at(start, () -> {
            stand[0] = stage.armorStand(ground.add(0, -HEAD_ABOVE_BASE, 0), yaw, new StandStyle(true, false, false, false, false));
            stand[0].equipSkull(Slot.HELMET, cap);
            stage.particle("BLOCK_DUST", ground.add(0, DIRT_ABOVE, 0), 8, new Vec(0.2, 0.05, 0.2), 0.05, Kit.blockId(Material.DIRT, 0));
            stage.sound("random.pop", ground, 0.6f, FIRST_PITCH + index * PITCH_STEP);
        });
        timeline.repeat(start + 1, start + SPINNING, 1, offset -> {
            int ticks = offset + 1;
            Vec at = ground.add(outward.multiply(drift(ticks))).add(0, lift(ticks) - HEAD_ABOVE_BASE, 0);
            stand[0].teleport(at, (float) (yaw - turned(ticks)), 0);
        });
        timeline.at(start + LIFE, () -> {
            EnchantedFinalKill.puff(stage, ground.add(outward.multiply(drift(HOP.length))).add(0, PUFF_ABOVE, 0), 0.5f, pitch);
            stand[0].remove();
        });
    }
}
