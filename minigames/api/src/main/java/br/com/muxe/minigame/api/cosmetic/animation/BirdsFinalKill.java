package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class BirdsFinalKill implements SceneAnimation {

    static final List<Bird> BIRDS = List.of(
        new Bird("ecad83e2628dc07da6508b526b1d72caf71aa88fd912d658b9c385ec9a42bf98", 14),
        new Bird("f17b1bd369110c5bd651ba8361d926b8543d3e34b1807ee3149faaf396426850", 3),
        new Bird("21f24d2598218f21cf59c14ec91f7bc6e75a0131a9e2ced02b9d24e409432189", 4),
        new Bird("e3a8010a8eaafc5fd31ab0d982e15eb843907e4101320ef37a1d5d3bada4eb1b", 3)
    );
    static final double FIRST_BASE = -1.12;
    static final double BASE_STEP = 0.92;
    static final double HEAD_ABOVE_BASE = 1.58;
    static final double INNER_RADIUS = 1;
    static final double OUTER_RADIUS = 4;
    static final double TURN = -24;
    static final int CIRCLING = 23;
    static final int WIDENING = 5;
    static final double CIRCLING_RISE = 0.06;
    static final double WIDENING_RISE = 2.2;
    static final int POP = 29;
    static final int END = 34;

    @Override
    public String id() {
        return "passarinhos";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        double first = random.nextDouble() * 360;
        stage.sound("mob.bat.loop", origin.add(0, 2, 0), 1, 1);
        stage.sound("mob.bat.idle", origin.add(0, 2, 0), 1, 1);
        timeline.at(20, () -> stage.sound("mob.bat.idle", origin.add(0, 2, 0), 1, 1));
        for (int index = 0; index < BIRDS.size(); index++) {
            bird(stage, timeline, origin, BIRDS.get(index), first + index * 90, FIRST_BASE + index * BASE_STEP);
        }
        timeline.at(POP + 1, () -> stage.sound("mob.bat.death", origin.add(0, 4, 0), 1, 2));
        timeline.lasts(END + 1);
    }

    static double radius(int tick) {
        if (tick <= CIRCLING) {
            return INNER_RADIUS;
        }
        double progress = Math.min(1, (double) (tick - CIRCLING) / WIDENING);
        return INNER_RADIUS + (OUTER_RADIUS - INNER_RADIUS) * (1 - (1 - progress) * (1 - progress));
    }

    static double rise(int tick) {
        double circling = Math.min(tick, CIRCLING) * CIRCLING_RISE;
        if (tick <= CIRCLING) {
            return circling;
        }
        double progress = Math.min(1, (double) (tick - CIRCLING) / WIDENING);
        return circling + WIDENING_RISE * progress;
    }

    static Vec position(Vec origin, double start, double base, int tick) {
        double degrees = start + TURN * tick;
        return origin.add(Kit.horizontal(degrees, radius(tick))).add(0, base + rise(tick), 0);
    }

    private static void bird(Stage stage, Timeline timeline, Vec origin, Bird bird, double start, double base) {
        Actor stand = stage.armorStand(position(origin, start, base, 0), (float) (start + 180), new StandStyle(true, false, false, false, false));
        stand.equipSkull(Slot.HELMET, bird.texture());
        timeline.repeat(1, END - 1, 1, tick -> stand.teleport(position(origin, start, base, tick), (float) (start + TURN * tick + 180), 0));
        timeline.at(POP, () -> {
            Vec head = position(origin, start, base, POP).add(0, HEAD_ABOVE_BASE, 0);
            stage.sound("fireworks.launch", head, 1, 1);
            stage.particle("BLOCK_DUST", head, 50, new Vec(0, 0.25, 0), 0.1, Kit.blockId(Material.WOOL, bird.wool()));
        });
        timeline.at(END, stand::remove);
    }

    record Bird(String texture, int wool) {
    }
}
