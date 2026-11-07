package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
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

public final class RabbitSpiralAnimation implements BedAnimation {

    static final String EGG = "1b80e43c4a148cbe256cb8329e9bdaaa4f30d70d8396d4194fe075f06851cc1e";
    static final byte[] VARIANTS = {2, 1, 0};
    static final int RABBIT_TYPE = 18;
    static final double RADIUS = 2;
    static final double BED_LIFT = 0.25;
    static final double PUMPKIN_LIFT = 0.09;
    static final double FIRST_RISE = 0.1;
    static final double RISE_GROWTH = 0.05;
    static final double FIRST_TURN = 30;
    static final double TURN_GROWTH = 0.2;
    static final int TRAIL = 4;
    static final double DROP_BELOW = 0.5;
    static final double DROP_SPREAD = 0.1;
    static final double DROP_SPEED = -0.3;
    static final int[] JUMPS = {9, 19, 29};
    static final int END = 35;
    static final int FALL_LIMIT = 60;

    @Override
    public String id() {
        return "espiral-de-coelhos";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Kit.BedDisplay display = Kit.bed(stage, bed);
        display.move(bed, new Vec(0, BED_LIFT, 0));
        Actor pumpkin = stage.armorStand(bed.head().add(0, PUMPKIN_LIFT, 0), 0, StandStyle.HIDDEN_SMALL);
        pumpkin.equip(Slot.HELMET, Material.JACK_O_LANTERN, 0);
        List<Actor> rabbits = new ArrayList<>();
        for (int index = 0; index < VARIANTS.length; index++) {
            Actor rabbit = stage.mob("Rabbit", rabbitAt(bed, 0, index), heading(bed, 0, index));
            rabbit.watch(RABBIT_TYPE, VARIANTS[index]);
            rabbits.add(rabbit);
        }
        List<Drop> drops = new ArrayList<>();
        timeline.repeat(1, END - 1, 1, offset -> {
            int tick = offset + 1;
            double rise = rise(tick);
            display.move(bed, new Vec(0, BED_LIFT + rise, 0));
            pumpkin.teleport(bed.head().add(0, PUMPKIN_LIFT + rise, 0), 0, 0);
            for (int index = 0; index < rabbits.size(); index++) {
                rabbits.get(index).teleport(rabbitAt(bed, tick, index), heading(bed, tick, index), 0);
            }
            if (tick >= TRAIL) {
                trail(bed, stage, random, tick, drops);
            }
            for (int jump : JUMPS) {
                if (tick == jump) {
                    for (int index = 0; index < rabbits.size(); index++) {
                        stage.sound("step.grass", rabbitAt(bed, tick - TRAIL, index), 1, 0.79f);
                    }
                }
                if (tick >= jump && tick < jump + rabbits.size()) {
                    rabbits.get(tick - jump).status(1);
                }
            }
        });
        timeline.at(END, () -> {
            display.remove();
            pumpkin.remove();
            rabbits.forEach(Actor::remove);
        });
        timeline.repeat(TRAIL + 1, END + FALL_LIMIT, 1, ignored -> {
            for (Drop drop : drops) {
                if (!drop.item().removed() && drop.body().step(stage)) {
                    drop.item().remove();
                }
            }
        });
        timeline.at(END + FALL_LIMIT, () -> drops.forEach(drop -> drop.item().remove()));
    }

    static double rise(int tick) {
        return FIRST_RISE * tick + RISE_GROWTH * tick * (tick - 1) / 2;
    }

    static double angle(BedFrame bed, int tick, int index) {
        double start = Math.toDegrees(Math.atan2(bed.right().z(), bed.right().x()));
        double turned = FIRST_TURN * tick + TURN_GROWTH * tick * (tick - 1) / 2;
        return start + index * 360.0 / VARIANTS.length - turned;
    }

    static Vec rabbitAt(BedFrame bed, int tick, int index) {
        return bed.around(bed.middle(), RADIUS, angle(bed, tick, index), rise(tick));
    }

    private static float heading(BedFrame bed, int tick, int index) {
        return rabbitAt(bed, tick, index).yawTowards(rabbitAt(bed, tick + 1, index));
    }

    private static void trail(BedFrame bed, Stage stage, RandomGenerator random, int tick, List<Drop> drops) {
        int dropper = random.nextInt(VARIANTS.length);
        for (int index = 0; index < VARIANTS.length; index++) {
            Vec behind = rabbitAt(bed, tick - TRAIL, index);
            if (index == dropper) {
                behind = behind.add(0, -DROP_BELOW, 0);
                Vec velocity = new Vec(Kit.between(random, -DROP_SPREAD, DROP_SPREAD), DROP_SPEED, Kit.between(random, -DROP_SPREAD, DROP_SPREAD));
                Actor item = stage.skullItem(behind, EGG);
                item.velocity(velocity);
                drops.add(new Drop(item, Body.item(behind, velocity)));
                stage.sound("mob.chicken.plop", behind, 1, 1);
            }
            stage.particle("CLOUD", behind, 0, Vec.ZERO, 1);
        }
    }

    private record Drop(Actor item, Body body) {
    }
}
