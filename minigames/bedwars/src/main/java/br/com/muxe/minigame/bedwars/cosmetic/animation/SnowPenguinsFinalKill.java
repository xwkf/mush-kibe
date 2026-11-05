package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class SnowPenguinsFinalKill implements SceneAnimation {

    static final List<String> PENGUINS = List.of(
        "b3a7b820e0ef209eec3be5e0dfe6f277d61531adc31bf55107116c8e4e44af3",
        "b6b7aa1d3aff5b9ef6c18a912983d69b2e1ffe62387a44d1f45a724bf635e035"
    );
    static final double THROW_HEIGHT = 1;
    static final int FISH = 11;
    static final int HEADS = 15;
    static final double TOSS_SPREAD = 0.2;
    static final double MIN_TOSS_LIFT = 0.2;
    static final double MAX_TOSS_LIFT = 0.5;
    static final float STEP_VOLUME = 1.5f;
    static final float STEP_PITCH = 0.794f;
    static final int SNOWBALLS_PER_TICK = 2;
    static final int TICKS = 30;
    static final double SPIRAL_BASE = 1.1;
    static final double SPIRAL_RISE = 0.1;
    static final double SPIRAL_GROWTH = 0.015;
    static final double SPIRAL_TURN = 0.5;
    static final double MAX_PUSH = 0.3;
    static final double MIN_SHOT = 0.6;
    static final double MAX_SHOT = 0.9;
    static final int[] CLUCKS = {8, 27};
    static final float CLUCK_VOLUME = 0.8f;
    static final float CLUCK_PITCH = 0.587f;
    static final int SNOWBALLS_GONE = 96;
    static final int TOSSED_GONE = 97;

    @Override
    public String id() {
        return "pinguins-na-neve";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec hand = origin.add(0, THROW_HEIGHT, 0);
        stage.sound("step.snow", hand, STEP_VOLUME, STEP_PITCH);
        List<Actor> tossed = new ArrayList<>();
        for (int fish = 0; fish < FISH; fish++) {
            tossed.add(toss(stage.item(hand, Material.RAW_FISH, 0), random));
        }
        for (int head = 0; head < HEADS; head++) {
            tossed.add(toss(stage.skullItem(hand, PENGUINS.get(random.nextInt(PENGUINS.size()))), random));
        }
        double start = random.nextDouble() * 360;
        List<Actor> snowballs = new ArrayList<>();
        timeline.repeat(0, TICKS - 1, 1, tick -> {
            for (int slot = 0; slot < SNOWBALLS_PER_TICK; slot++) {
                int index = tick * SNOWBALLS_PER_TICK + slot;
                Vec at = spiral(origin, start, index);
                Vec out = Kit.horizontal(angle(start, index), Kit.between(random, 0, MAX_PUSH));
                stage.particle("CLOUD", at, 2, new Vec(0.1, 0.1, 0.1), 0.05);
                stage.sound("mob.chicken.plop", at, 1, 1);
                snowballs.add(stage.snowball(at, out.add(0, Kit.between(random, MIN_SHOT, MAX_SHOT), 0)));
            }
        });
        for (int cluck : CLUCKS) {
            timeline.at(cluck, () -> stage.sound("mob.chicken.say", hand, CLUCK_VOLUME, CLUCK_PITCH));
        }
        timeline.at(SNOWBALLS_GONE, () -> snowballs.forEach(Actor::remove));
        timeline.at(TOSSED_GONE, () -> tossed.forEach(Actor::remove));
    }

    static Vec spiral(Vec origin, double start, int index) {
        return origin.add(Kit.horizontal(angle(start, index), SPIRAL_GROWTH * index)).add(0, SPIRAL_BASE + SPIRAL_RISE * index, 0);
    }

    static double angle(double start, int index) {
        return start + Math.toDegrees(SPIRAL_TURN * index);
    }

    private static Actor toss(Actor item, RandomGenerator random) {
        item.velocity(new Vec(Kit.between(random, -TOSS_SPREAD, TOSS_SPREAD), Kit.between(random, MIN_TOSS_LIFT, MAX_TOSS_LIFT), Kit.between(random, -TOSS_SPREAD, TOSS_SPREAD)));
        return item;
    }
}
