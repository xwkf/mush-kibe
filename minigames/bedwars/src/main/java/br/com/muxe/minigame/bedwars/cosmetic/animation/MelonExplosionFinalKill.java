package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class MelonExplosionFinalKill implements SceneAnimation {

    static final int[] FIREWORK_COLORS = {0x799E2A, 0x4E7715, 0xC03023};
    static final double BURST_HEIGHT = 1;
    static final int PIECES = 8;
    static final double MIN_SPEED = 0.155;
    static final double MAX_SPEED = 0.2;
    static final double UP = 0.4;
    static final float EXPLOSION_PITCH = 1.2f;
    static final int END = 99;

    @Override
    public String id() {
        return "explosao-de-melancia";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec center = scene.origin().add(0, BURST_HEIGHT, 0);
        Kit.flash(stage, timeline, 0, center, Kit.burst(Kit.BURST, false, true, FIREWORK_COLORS));
        stage.sound("random.explode", center, 1, EXPLOSION_PITCH);
        for (int piece = 0; piece < PIECES; piece++) {
            Material material = piece % 2 == 0 ? Material.MELON_BLOCK : Material.MELON;
            Spill.drop(stage, timeline, center, material, 0, launch(piece, Kit.between(random, MIN_SPEED, MAX_SPEED)), END);
        }
        stage.sound("mob.chicken.plop", center, 1.2f, 1);
        timeline.lasts(END);
    }

    static Vec launch(int piece, double speed) {
        return Kit.horizontal(piece * 360.0 / PIECES, speed).add(0, UP, 0);
    }
}
