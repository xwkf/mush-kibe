package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.Body;
import br.com.muxe.minigame.api.cosmetic.animation.Kit;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class TreasureAnimation implements BedAnimation {

    static final int SINK = 14;
    static final double PUMPKIN_DROP = -0.16;
    static final int SAND = 2;
    static final int SAND_RING = 3;
    static final int SAND_STRAY = 5;
    static final double RING_CHANCE = 0.7;
    static final int MARK = 7;
    static final int[][] CROSS = {{0, 0}, {-1, 1}, {1, -1}, {1, 1}, {-1, -1}};
    static final int[] DIGS = {2, 3, 7, 9, 11};
    static final float[] DIG_PITCH = {0.90f, 0.97f, 0.83f, 0.76f, 0.70f};
    static final int WHISTLE = 10;
    static final int DROP = 10;
    static final double DROP_HEIGHT = 8.62;
    static final double FALL_GROWTH = 0.03;
    static final double SPIN = 5.6;
    static final Vec SHOVEL_TILT = new Vec(0, 0, 135);
    static final int IMPACT = 28;
    static final double BURIED = -1.38;
    static final int DIAMONDS = 10;
    static final int GOLD = 10;
    static final double LOOT_LIFT = 0.28;
    static final double LOOT_SPREAD = 0.25;
    static final double LOOT_MIN_RISE = 0.46;
    static final double LOOT_MAX_RISE = 0.74;
    static final int PULL_OUT = 59;
    static final int END = 95;

    @Override
    public String id() {
        return "tesouro-encontrado";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        Kit.BedDisplay display = Kit.bed(stage, bed);
        Actor pumpkin = stage.armorStand(bed.head().add(0, PUMPKIN_DROP, 0), 0, StandStyle.HIDDEN_SMALL);
        pumpkin.equip(Slot.HELMET, Material.JACK_O_LANTERN, 0);
        timeline.repeat(1, SINK - 1, 1, offset -> {
            double sink = sink(offset + 1);
            display.move(bed, new Vec(0, -sink, 0));
            pumpkin.teleport(bed.head().add(0, PUMPKIN_DROP - sink, 0), 0, 0);
        });
        timeline.repeat(4, SINK, 1, tick -> stage.particle("BLOCK_DUST", middle, 6, new Vec(0.8, 0.1, 0.8), 0.08, Kit.blockId(Material.SANDSTONE, 0)));
        timeline.at(SINK, () -> {
            display.remove();
            pumpkin.remove();
        });
        ground(bed, stage, timeline, random);
        for (int dig = 0; dig < DIGS.length; dig++) {
            float pitch = DIG_PITCH[dig];
            timeline.at(DIGS[dig], () -> stage.sound("dig.sand", middle, 0.8f, pitch));
        }
        shovel(bed, stage, timeline);
        timeline.at(IMPACT, () -> {
            stage.particle("EXPLOSION_HUGE", middle, 1, Vec.ZERO, 1);
            stage.particle("FIREWORKS_SPARK", middle, 30, Vec.ZERO, 0.3);
            stage.sound("random.explode", middle, 1, 1.10f);
            stage.sound("random.anvil_land", middle, 0.5f, 1.40f);
            stage.sound("random.levelup", middle, 0.8f, 1.49f);
        });
        List<Loot> loot = new ArrayList<>();
        timeline.at(IMPACT, () -> {
            for (int piece = 0; piece < DIAMONDS + GOLD; piece++) {
                Vec start = middle.add(0, LOOT_LIFT, 0);
                Vec velocity = new Vec(Kit.between(random, -LOOT_SPREAD, LOOT_SPREAD), Kit.between(random, LOOT_MIN_RISE, LOOT_MAX_RISE),
                    Kit.between(random, -LOOT_SPREAD, LOOT_SPREAD));
                Actor item = stage.item(start, piece < DIAMONDS ? Material.DIAMOND : Material.GOLD_INGOT, 0);
                item.velocity(velocity);
                loot.add(new Loot(item, Body.item(start, velocity)));
            }
        });
        timeline.repeat(IMPACT + 1, END - 1, 1, tick -> {
            for (Loot piece : loot) {
                if (piece.body().step(stage)) {
                    Vec at = piece.body().position();
                    stage.sound("random.orb", at, 0.3f, (float) Kit.between(random, 1.22, 1.79));
                    stage.particle("FIREWORKS_SPARK", at, 3, Vec.ZERO, 0.05);
                }
            }
        });
        timeline.at(END, () -> loot.forEach(piece -> piece.item().remove()));
    }

    static double sink(int tick) {
        return Math.min(1, tick / (double) SINK);
    }

    static double shovelHeight(int tick) {
        if (tick >= IMPACT) {
            return BURIED;
        }
        int fallen = Math.max(0, tick - DROP);
        return DROP_HEIGHT - FALL_GROWTH * fallen * (fallen + 1);
    }

    private static void shovel(BedFrame bed, Stage stage, Timeline timeline) {
        Vec middle = bed.middle();
        float yaw = bed.yaw() + 180;
        timeline.at(WHISTLE, () -> stage.sound("random.bow", middle.add(0, 10, 0), 1, 0.49f));
        Actor[] shovel = new Actor[1];
        timeline.at(DROP, () -> {
            shovel[0] = stage.armorStand(middle.add(0, DROP_HEIGHT, 0), yaw, StandStyle.HIDDEN);
            shovel[0].pose(Pose.STANDING.withHead(SHOVEL_TILT));
            shovel[0].equip(Slot.HELMET, Material.DIAMOND_SPADE, 0);
        });
        timeline.repeat(DROP, IMPACT, 1, offset -> {
            int tick = DROP + offset;
            Vec at = middle.add(0, shovelHeight(tick), 0);
            shovel[0].teleport(at, (float) (yaw - SPIN * offset), 0);
            if (tick < IMPACT) {
                stage.particle("CRIT", at.add(0, 0.5, 0), 2, new Vec(0.1, 0.3, 0.1), 0);
            }
        });
        timeline.at(PULL_OUT, () -> {
            shovel[0].remove();
            stage.sound("mob.chicken.plop", middle, 0.6f, 1.19f);
            stage.particle("EXPLOSION_NORMAL", middle.add(0, 0.5, 0), 8, Vec.ZERO, 0.02);
        });
    }

    private static void ground(BedFrame bed, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec floor = bed.foot().add(0, -1, 0);
        List<int[]> patch = new ArrayList<>();
        List<int[]> ring = new ArrayList<>();
        List<int[]> outer = new ArrayList<>();
        for (int forward = -3; forward <= 3; forward++) {
            for (int right = -3; right <= 3; right++) {
                int distance = Math.max(Math.abs(forward), Math.abs(right));
                List<int[]> target = distance <= 1 ? patch : distance == 2 ? ring : outer;
                target.add(new int[] {forward, right});
            }
        }
        timeline.at(SAND, () -> {
            cover(bed, stage, floor, patch, Material.SANDSTONE, 0);
            stage.sound("dig.sand", floor, 0.8f, 1.03f);
            stage.sound("dig.sand", floor, 0.8f, 0.81f);
        });
        timeline.at(SAND_RING, () -> {
            cover(bed, stage, floor, ring.stream().filter(cell -> random.nextDouble() < RING_CHANCE).toList(), Material.SANDSTONE, 0);
            stage.sound("dig.sand", floor, 0.8f, 1.02f);
        });
        timeline.at(SAND_STRAY, () -> {
            cover(bed, stage, floor, List.of(outer.get(random.nextInt(outer.size()))), Material.SANDSTONE, 0);
            stage.sound("dig.sand", floor, 0.8f, 1.08f);
        });
        timeline.at(MARK, () -> {
            cover(bed, stage, floor, List.of(CROSS), Material.WOOL, 14);
            stage.sound("dig.cloth", floor, 1, 1);
            stage.sound("note.pling", floor, 0.6f, 1.49f);
        });
    }

    private static void cover(BedFrame bed, Stage stage, Vec floor, List<int[]> cells, Material material, int data) {
        for (int[] cell : cells) {
            Vec center = floor.add(bed.forward().multiply(cell[0])).add(bed.right().multiply(cell[1]));
            int x = (int) Math.floor(center.x());
            int y = (int) Math.floor(center.y());
            int z = (int) Math.floor(center.z());
            if (stage.solid(x, y, z)) {
                stage.block(x, y, z, material, data);
                stage.particle("BLOCK_DUST", new Vec(x + 0.5, y + 1, z + 0.5), 4, new Vec(0.8, 0.1, 0.8), 0.08, Kit.blockId(material, data));
            }
        }
    }

    private record Loot(Actor item, Body body) {
    }
}
