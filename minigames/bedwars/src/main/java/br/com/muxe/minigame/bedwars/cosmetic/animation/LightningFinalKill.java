package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class LightningFinalKill implements SceneAnimation {

    static final int FLAMES = 50;
    static final double FLAME_HEIGHT = 0.5;
    static final Spray.Reach FLAME_REACH = new Spray.Reach(0.25, 0.5, 0.05);

    @Override
    public String id() {
        return "raio";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Spray.outwards(stage, "FLAME", origin.add(0, FLAME_HEIGHT, 0), FLAMES, FLAME_REACH, random);
        Vec strike = strike(origin);
        stage.lightning(strike);
        stage.sound("random.explode", strike, 2, (float) Kit.between(random, 0.5, 0.7));
        timeline.lasts(1);
    }

    static Vec strike(Vec origin) {
        return new Vec(Math.floor(origin.x()), origin.y(), Math.floor(origin.z()));
    }
}
