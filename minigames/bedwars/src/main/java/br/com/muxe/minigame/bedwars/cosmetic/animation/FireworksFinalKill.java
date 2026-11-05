package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class FireworksFinalKill implements SceneAnimation {

    static final double ROCKET_ABOVE = 1;
    static final int[] COLORS = {0x00CC81, 0x408068};
    static final int END = 3;

    @Override
    public String id() {
        return "fogos-de-artificio";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec rocket = scene.origin().add(0, ROCKET_ABOVE, 0);
        Kit.flash(stage, timeline, 0, rocket, Kit.burst(Kit.BALL, false, true, COLORS));
        timeline.lasts(END);
    }
}
