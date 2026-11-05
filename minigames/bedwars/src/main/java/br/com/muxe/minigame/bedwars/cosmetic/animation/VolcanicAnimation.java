package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class VolcanicAnimation implements BedAnimation {

    static final double[] ERUPTION = {1.91, 3.34, 4.19, 4.47, 4.50};
    static final double PUMPKIN_DROP = -0.16;
    static final int BOB_EVERY = 4;
    static final double LOWEST = 4.1;
    static final double HIGHEST = 5.8;
    static final int FLAMES = 15;
    static final double VENT = 0.1;
    static final double FLAME_SPREAD = 0.07;
    static final double MIN_FLAME_RISE = 0.26;
    static final double MAX_FLAME_RISE = 0.73;
    static final double SECOND_POP_CHANCE = 0.25;
    static final double MIN_POP = 0.746;
    static final double MAX_POP = 1.238;
    static final int[] HISSES = {77, 79, 82};
    static final float[] HISS_PITCH = {1.11f, 1.87f, 1.97f};
    static final int END = 82;

    @Override
    public String id() {
        return "vulcanica";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        Kit.BedDisplay display = Kit.bed(stage, bed);
        Actor pumpkin = stage.armorStand(bed.head().add(0, PUMPKIN_DROP, 0), 0, StandStyle.HIDDEN_SMALL);
        pumpkin.equip(Slot.HELMET, Material.JACK_O_LANTERN, 0);
        double[] heights = heights(random);
        timeline.at(0, () -> {
            stage.particle("EXPLOSION_HUGE", middle, 0, Vec.ZERO, 1);
            stage.sound("random.explode", middle, 1, 2);
        });
        timeline.repeat(0, END - 1, 1, tick -> {
            display.move(bed, new Vec(0, heights[tick], 0));
            pumpkin.teleport(bed.head().add(0, PUMPKIN_DROP + heights[tick], 0), 0, 0);
        });
        timeline.repeat(0, END, 1, tick -> {
            for (int flame = 0; flame < FLAMES; flame++) {
                Vec vent = middle.add(Kit.between(random, -VENT, VENT), 0, Kit.between(random, -VENT, VENT));
                Vec jet = new Vec(Kit.between(random, -FLAME_SPREAD, FLAME_SPREAD), Kit.between(random, MIN_FLAME_RISE, MAX_FLAME_RISE),
                    Kit.between(random, -FLAME_SPREAD, FLAME_SPREAD));
                stage.particle("FLAME", vent, 0, jet, 1);
            }
            stage.particle("LAVA", middle, 0, Vec.ZERO, 1);
            stage.sound("liquid.lavapop", middle, 1, (float) Kit.between(random, MIN_POP, MAX_POP));
            if (random.nextDouble() < SECOND_POP_CHANCE) {
                stage.sound("liquid.lavapop", middle, 1, (float) Kit.between(random, MIN_POP, MAX_POP));
            }
        });
        for (int hiss = 0; hiss < HISSES.length; hiss++) {
            float pitch = HISS_PITCH[hiss];
            timeline.at(HISSES[hiss], () -> stage.sound("random.fizz", middle, 1, pitch));
        }
        timeline.at(END, () -> {
            display.remove();
            pumpkin.remove();
        });
    }

    static double[] heights(RandomGenerator random) {
        double[] heights = new double[END];
        System.arraycopy(ERUPTION, 0, heights, 0, ERUPTION.length);
        double from = ERUPTION[ERUPTION.length - 1];
        for (int start = ERUPTION.length; start < END; start += BOB_EVERY) {
            double to = Kit.between(random, LOWEST, HIGHEST);
            for (int step = 1; step <= BOB_EVERY && start + step - 1 < END; step++) {
                heights[start + step - 1] = from + (to - from) * step / BOB_EVERY;
            }
            from = to;
        }
        return heights;
    }
}
