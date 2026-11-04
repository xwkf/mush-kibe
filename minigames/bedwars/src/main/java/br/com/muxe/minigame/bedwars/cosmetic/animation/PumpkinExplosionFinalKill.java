package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class PumpkinExplosionFinalKill implements SceneAnimation {

    static final int[] FIREWORK_COLORS = {0xDF935A, 0xD37143};
    static final double BURST_HEIGHT = 1;
    static final int PIES = 5;
    static final int PUMPKINS = 3;
    static final double SIDEWAYS = 0.14;
    static final double MIN_UP = 0.15;
    static final double MAX_UP = 0.36;
    static final int END = 100;

    @Override
    public String id() {
        return "explosao-de-abobora";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec center = scene.origin().add(0, BURST_HEIGHT, 0);
        Kit.flash(stage, timeline, 0, center, Kit.burst(Kit.BALL, false, true, FIREWORK_COLORS));
        for (int index = 0; index < PIES + PUMPKINS; index++) {
            Material material = index < PIES ? Material.PUMPKIN_PIE : Material.PUMPKIN;
            Spill.drop(stage, timeline, center, material, 0, Spill.scattered(random, SIDEWAYS, MIN_UP, MAX_UP), END);
        }
        timeline.lasts(END);
    }
}
