package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class ExplodingSquidFinalKill implements SceneAnimation {

    static final double START_HEIGHT = 0.5;
    static final double FIRST_STEP = 0.28;
    static final double STEP_GROWTH = 0.03;
    static final double TRAIL_BELOW = 0.5;
    static final int POP = 17;
    static final float FIRST_PITCH = 1.05f;
    static final float PITCH_STEP = 0.05f;
    static final int INK_SACS = 12;
    static final double SIDEWAYS = 0.19;
    static final double MIN_UP = 0.2;
    static final double MAX_UP = 0.5;
    static final int INK_LIFE = 58;

    @Override
    public String id() {
        return "lula-explosiva";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Actor squid = stage.mob("Squid", origin.add(0, height(0), 0), 0);
        timeline.repeat(0, POP - 1, 1, tick -> {
            Vec trail = origin.add(0, height(tick) - TRAIL_BELOW, 0);
            stage.particle("DRIP_WATER", trail, 5, new Vec(0.3, 0.1, 0.3), 0.05);
            stage.particle("WATER_SPLASH", trail, 3, new Vec(0.2, 0.1, 0.2), 0.1);
            stage.sound("note.pling", trail, 1, FIRST_PITCH + PITCH_STEP * tick);
            squid.teleport(origin.add(0, height(tick + 1), 0), 0, 0);
        });
        timeline.at(POP, () -> {
            Vec top = origin.add(0, height(POP), 0);
            squid.remove();
            stage.particle("SMOKE_LARGE", top, 25, new Vec(0.5, 0.5, 0.5), 0.1);
            stage.sound("random.explode", top, 1, 1);
            for (int sac = 0; sac < INK_SACS; sac++) {
                Spill.drop(stage, timeline, top, Material.INK_SACK, 0, Spill.scattered(random, SIDEWAYS, MIN_UP, MAX_UP), POP + INK_LIFE);
            }
        });
        timeline.lasts(POP + INK_LIFE);
    }

    static double height(int tick) {
        return START_HEIGHT + FIRST_STEP * tick + STEP_GROWTH * tick * (tick - 1) / 2;
    }
}
