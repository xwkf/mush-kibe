package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class EnchantedAnimation implements BedAnimation {

    static final int STALK = 8;
    static final int STEM = 0;
    static final int BLOSSOM = 10;
    static final double DROP_HEIGHT = 12;
    static final double LANDED = 0.53125;
    static final int FALL = 14;
    static final double PULL = (DROP_HEIGHT - LANDED) / (FALL * FALL);
    static final double[][] PETALS = {{0.698, 0.4, 1.0}, {1.0, 0.412, 0.706}, {0.541, 0.169, 0.886}, {1.0, 0.714, 0.882}};
    static final int PETALS_PER_TICK = 4;
    static final int ROOT = 14;
    static final int SPROUT = 17;
    static final int BLOOM = 31;
    static final int FLOWERS = 24;
    static final Material[] FLOWER_KINDS = {Material.YELLOW_FLOWER, Material.RED_ROSE, Material.RED_ROSE, Material.RED_ROSE, Material.RED_ROSE};
    static final int[] FLOWER_DATA = {0, 0, 2, 1, 7};
    static final int[] FLOWER_SHARE = {9, 7, 4, 3, 1};
    static final double FLOWER_LIFT = 0.78;
    static final int WILT = 75;
    static final int GREEN = 0x3CB371;

    @Override
    public String id() {
        return "encantado";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        Kit.BedDisplay display = Kit.bed(stage, bed);
        Actor lantern = HeadLantern.place(stage, bed, Vec.ZERO);
        List<Actor> garden = new ArrayList<>();
        List<Actor> stands = new ArrayList<>();
        for (int block = 0; block < STALK; block++) {
            Vec at = middle.add(0, DROP_HEIGHT + block, 0);
            Actor stand = stage.armorStand(at, 0, StandStyle.HIDDEN);
            Actor plant = stage.fallingBlock(at, Material.DOUBLE_PLANT, block == STALK - 1 ? BLOSSOM : STEM);
            plant.ride(stand);
            stands.add(stand);
            garden.add(plant);
        }
        timeline.at(0, () -> {
            stage.sound("note.pling", middle, 0.6f, SoundPitch.of(100));
            stage.sound("dig.grass", middle, 1, SoundPitch.of(37));
        });
        timeline.repeat(0, FALL - 1, 1, tick -> {
            double bottom = fall(tick + 1);
            for (int block = 0; block < STALK; block++) {
                stands.get(block).teleport(middle.add(0, bottom + block, 0), 0, 0);
            }
            for (int petal = 0; petal < PETALS_PER_TICK; petal++) {
                double[] color = PETALS[random.nextInt(PETALS.length)];
                Vec at = middle.add(bed.forward().multiply(Kit.between(random, -0.5, 0.6)))
                    .add(bed.right().multiply(Kit.between(random, -0.65, 0.65))).add(0, bottom + Kit.between(random, 0, STALK), 0);
                Kit.dust(stage, at, color[0], color[1], color[2]);
                if (petal == 0) {
                    stage.particle("FIREWORKS_SPARK", at, 0, Vec.ZERO, 0);
                }
            }
        });
        timeline.at(ROOT, () -> {
            garden.add(leaves(stage, bed.foot(), 0, stands));
            stage.sound("dig.grass", middle.add(0, 0.5, 0), 1, SoundPitch.of(50));
            stage.sound("random.orb", middle.add(0, 0.5, 0), 0.5f, SoundPitch.of(88));
            stage.sound("step.grass", bed.foot().add(0, 0.5, 0), 0.8f, SoundPitch.of(50));
            stage.particle("VILLAGER_HAPPY", middle.add(0, 0.6, 0), 15, new Vec(0.8, 0.3, 0.8), 1);
            stage.particle("VILLAGER_HAPPY", bed.foot().add(0, 0.5, 0), 8, new Vec(0.4, 0.3, 0.4), 1);
        });
        timeline.at(SPROUT, () -> {
            garden.add(leaves(stage, bed.head(), 3, stands));
            stage.sound("step.grass", bed.head().add(0, 0.5, 0), 0.8f, SoundPitch.of(69));
            stage.particle("VILLAGER_HAPPY", bed.head().add(0, 0.5, 0), 8, new Vec(0.4, 0.3, 0.4), 1);
        });
        List<Actor> flowers = new ArrayList<>();
        timeline.at(BLOOM, () -> {
            display.remove();
            lantern.remove();
            garden.forEach(Actor::remove);
            stands.forEach(Actor::remove);
            Vec heart = middle.add(0, 0.8, 0);
            stage.particle("EXPLOSION_LARGE", heart, 2, new Vec(0.5, 0.3, 0.5), 1);
            stage.particle("VILLAGER_HAPPY", heart, 30, new Vec(1.2, 0.8, 1.2), 1);
            for (double[] color : PETALS) {
                for (int petal = 0; petal < 5; petal++) {
                    Vec spread = new Vec(Kit.between(random, -1, 1), Kit.between(random, -0.75, 0.75), Kit.between(random, -1, 1));
                    Kit.dust(stage, heart.add(spread), color[0], color[1], color[2]);
                }
            }
            Vec bang = middle.add(0, 0.75, 0);
            stage.sound("random.explode", bang, 0.6f, SoundPitch.of(88));
            stage.sound("dig.grass", bang, 1, SoundPitch.of(37));
            stage.sound("fireworks.twinkle", bang, 1, SoundPitch.of(75));
            Vec start = middle.add(0, FLOWER_LIFT, 0);
            for (int index = 0; index < FLOWERS; index++) {
                int kind = flowerKind(index);
                Actor flower = stage.item(start, FLOWER_KINDS[kind], FLOWER_DATA[kind]);
                flower.velocity(new Vec(Kit.between(random, -0.24, 0.24), Kit.between(random, 0.46, 0.77), Kit.between(random, -0.24, 0.24)));
                flowers.add(flower);
            }
        });
        Kit.flash(stage, timeline, BLOOM, middle.add(0, 1.78, 0), Kit.burst(Kit.STAR, true, false, GREEN));
        timeline.at(WILT, () -> flowers.forEach(Actor::remove));
    }

    static double fall(int step) {
        return DROP_HEIGHT - PULL * step * step;
    }

    static int flowerKind(int index) {
        int remaining = index;
        for (int kind = 0; kind < FLOWER_SHARE.length; kind++) {
            if (remaining < FLOWER_SHARE[kind]) {
                return kind;
            }
            remaining -= FLOWER_SHARE[kind];
        }
        return 0;
    }

    private static Actor leaves(Stage stage, Vec at, int data, List<Actor> stands) {
        Actor stand = stage.armorStand(at, 0, StandStyle.HIDDEN);
        Actor leaves = stage.fallingBlock(at, Material.LEAVES, data);
        leaves.ride(stand);
        stands.add(stand);
        return leaves;
    }
}
