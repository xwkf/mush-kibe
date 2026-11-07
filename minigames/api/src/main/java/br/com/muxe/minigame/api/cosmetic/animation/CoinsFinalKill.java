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

public final class CoinsFinalKill implements SceneAnimation {

    static final int COINS_PER_TICK = 2;
    static final int TICKS = 30;
    static final double RADIUS = 0.5;
    static final double TURN = 0.3;
    static final double RISE = 0.05;
    static final double SPEED = 0.5;
    static final double MIN_LIFT = 0.2;
    static final double MAX_LIFT = 0.5;
    static final int SPARKS = 2;
    static final int END = 100;

    @Override
    public String id() {
        return "moedas";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        List<Actor> coins = new ArrayList<>();
        timeline.repeat(0, TICKS - 1, 1, tick -> {
            stage.sound("random.orb", origin, 1, 1);
            for (int slot = 0; slot < COINS_PER_TICK; slot++) {
                int coin = tick * COINS_PER_TICK + slot + 1;
                Vec at = spiral(origin, coin);
                for (int spark = 0; spark < SPARKS; spark++) {
                    Kit.dust(stage, at, 1, 0.85, 0);
                }
                Actor item = stage.item(at, Material.DOUBLE_PLANT, 0);
                item.velocity(outward(coin).multiply(SPEED).add(0, Kit.between(random, MIN_LIFT, MAX_LIFT), 0));
                coins.add(item);
            }
        });
        timeline.at(END, () -> coins.forEach(Actor::remove));
    }

    static Vec spiral(Vec origin, int coin) {
        return origin.add(outward(coin).multiply(RADIUS)).add(0, RISE * coin, 0);
    }

    static Vec outward(int coin) {
        return Kit.horizontal(Math.toDegrees(TURN * coin), 1);
    }
}
