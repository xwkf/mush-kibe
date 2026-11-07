package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class MidasTouchFinalKill implements SceneAnimation {

    static final int DROP = 1;
    static final int IMPACT = 2;
    static final int FIRST_FLOWER = 3;
    static final double DROP_HEIGHT = 5.94;
    static final double FALL_SPEED = 3.9;
    static final double DUST_HEIGHT = 0.5;
    static final int DUST = 200;
    static final Spray.Reach DUST_REACH = new Spray.Reach(0.25, 0.5, 0.5);
    static final int FLOWERS = 15;
    static final double FLOWER_SPREAD = 0.2;
    static final double FLOWER_SLOWEST_RISE = 0.2;
    static final double FLOWER_FASTEST_RISE = 0.5;
    static final int END = 100;

    @Override
    public String id() {
        return "toque-de-midas";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Actor[] gold = new Actor[1];
        timeline.at(DROP, () -> {
            gold[0] = stage.fallingBlock(origin.add(0, DROP_HEIGHT, 0), Material.GOLD_BLOCK, 0);
            gold[0].velocity(new Vec(0, -FALL_SPEED, 0));
        });
        timeline.at(IMPACT, () -> {
            gold[0].teleport(origin, 0, 0);
            stage.sound("dig.stone", origin, 1, 1.49f);
            stage.sound("random.break", origin, 1, 0.75f);
            Spray.outwards(stage, "BLOCK_DUST", origin.add(0, DUST_HEIGHT, 0), DUST, DUST_REACH, random, Kit.blockId(Material.GOLD_BLOCK, 0));
        });
        timeline.at(FIRST_FLOWER, () -> gold[0].remove());
        List<Actor> flowers = new ArrayList<>();
        for (int flower = 0; flower < FLOWERS; flower++) {
            int index = flower;
            timeline.at(flowerTick(index), () -> {
                stage.sound("random.pop", origin, 1, pitch(index));
                Actor item = stage.item(origin, Material.DOUBLE_PLANT, 0);
                item.velocity(new Vec(Kit.between(random, -FLOWER_SPREAD, FLOWER_SPREAD), Kit.between(random, FLOWER_SLOWEST_RISE, FLOWER_FASTEST_RISE),
                    Kit.between(random, -FLOWER_SPREAD, FLOWER_SPREAD)));
                flowers.add(item);
            });
        }
        timeline.at(END, () -> flowers.forEach(Actor::remove));
    }

    static int flowerTick(int index) {
        return FIRST_FLOWER + Math.max(0, index - 1);
    }

    static float pitch(int index) {
        return 1 + index / (float) FLOWERS;
    }
}
