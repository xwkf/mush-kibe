package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class TakeOffFinalKill implements SceneAnimation {

    static final double BACK_PER_TICK = 6;
    static final double UP_PER_TICK = 2.5;
    static final int FLIGHT = 19;
    static final int BLAST = FLIGHT;
    static final int END = 33;
    static final Vec SPARK_SPREAD = new Vec(0.25, 0.25, 0.25);
    static final int SPARKS = 3;

    @Override
    public String id() {
        return "decolando";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec back = scene.facing();
        float yaw = VictimBody.facingKiller(scene);
        Actor body = VictimBody.spawn(stage, scene, origin, yaw);
        stage.sound("fireworks.launch", origin, 1, 1);
        timeline.repeat(0, FLIGHT, 1, tick -> {
            stage.particle("FIREWORKS_SPARK", origin.add(flight(back, tick)), SPARKS, SPARK_SPREAD, 0);
            body.teleport(origin.add(flight(back, tick + 1)), yaw, 0);
        });
        timeline.at(BLAST, () -> stage.sound("fireworks.blast", origin, 1, 1));
        timeline.at(END, body::remove);
    }

    static Vec flight(Vec back, int tick) {
        return back.multiply(BACK_PER_TICK * tick).add(0, UP_PER_TICK * tick, 0);
    }
}
