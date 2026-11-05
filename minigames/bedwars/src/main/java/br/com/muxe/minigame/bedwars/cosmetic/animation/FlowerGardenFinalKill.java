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

public final class FlowerGardenFinalKill implements SceneAnimation {

    static final int PINK = 0xF3D4F3;
    static final double CENTER_HEIGHT = 1;
    static final int BLOOM = 4;
    static final int FLOWERS = 13;
    static final int PINK_TULIP = 7;
    static final double MIN_SPEED = 0.25;
    static final double MAX_SPEED = 0.35;
    static final double LIFT = 0.1;
    static final int END = 98;

    @Override
    public String id() {
        return "jardim-florido";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec center = scene.origin().add(0, CENTER_HEIGHT, 0);
        Kit.flash(stage, timeline, 0, center, Kit.burst(Kit.BALL, false, true, PINK));
        List<Actor> flowers = new ArrayList<>();
        timeline.at(BLOOM, () -> {
            for (int flower = 0; flower < FLOWERS; flower++) {
                Actor tulip = stage.item(center, Material.RED_ROSE, PINK_TULIP);
                tulip.velocity(launch(flower, Kit.between(random, MIN_SPEED, MAX_SPEED)));
                flowers.add(tulip);
            }
        });
        timeline.at(END, () -> flowers.forEach(Actor::remove));
    }

    static Vec launch(int flower, double speed) {
        return Kit.horizontal(flower * 360.0 / FLOWERS, speed).add(0, LIFT, 0);
    }
}
