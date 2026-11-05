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

public final class VanillaFinalKill implements SceneAnimation {

    static final int HEALTH = 6;
    static final float DEAD = 0;
    static final double KNOCKBACK = 0.41;
    static final double LIFT = 0.2;
    static final int SLIDE_TICKS = 6;
    static final int END = 29;

    @Override
    public String id() {
        return "vanilla";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        float yaw = VictimBody.facingKiller(scene);
        Actor body = VictimBody.spawn(stage, scene, origin, yaw);
        body.watch(HEALTH, DEAD);
        List<Vec> slide = slide(stage, origin, scene.facing());
        timeline.repeat(0, slide.size() - 1, 1, tick -> body.teleport(slide.get(tick), yaw, 0));
        timeline.at(END, body::remove);
    }

    static List<Vec> slide(Stage stage, Vec origin, Vec away) {
        Body body = Body.living(origin, away.multiply(KNOCKBACK).add(0, LIFT, 0));
        List<Vec> path = new ArrayList<>();
        while (path.size() < END && !body.landed()) {
            body.step(stage);
            path.add(body.position());
        }
        return List.copyOf(path);
    }
}
