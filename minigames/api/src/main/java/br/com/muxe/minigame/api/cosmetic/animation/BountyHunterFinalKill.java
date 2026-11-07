package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.random.RandomGenerator;

public final class BountyHunterFinalKill implements SceneAnimation {

    static final List<String> REWARDS = List.of(
        "1b7a1c5da1d362fa357db9889fa6a06a6032e24508481daca914e762660b948",
        "9fd108383dfa5b02e86635609541520e4e158952d68c1c8f8f200ec7e88642d"
    );
    static final StandStyle SMALL_STAND = new StandStyle(true, true, false, false, false);
    static final int GREEN = 0x70CA55;
    static final int HEADS = 6;
    static final int HEADS_PER_TICK = 2;
    static final double BASE_BELOW = 0.78125;
    static final double SPEED = 0.19;
    static final double DRAG = 0.9;
    static final double LIFT = 0.875;
    static final double GRAVITY = 0.2;
    static final int LANDING = 10;
    static final int CHIME = 3;
    static final int END = 56;

    @Override
    public String id() {
        return "cacador-de-recompensas";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Kit.flash(stage, timeline, 0, origin, Kit.burst(Kit.BALL, false, true, GREEN));
        stage.sound("random.orb", origin, 1, 0.49f);
        timeline.at(CHIME, () -> stage.sound("random.orb", origin, 1, 2));
        double first = random.nextDouble() * 360;
        Vec base = origin.add(0, -BASE_BELOW, 0);
        for (int index = 0; index < HEADS; index++) {
            reward(stage, timeline, base, index / HEADS_PER_TICK, first + index * 360.0 / HEADS, (float) (random.nextDouble() * 360), REWARDS.get(index % REWARDS.size()));
        }
        timeline.lasts(END);
    }

    static Vec position(double degrees, int tick) {
        double out = 0;
        double up = 0;
        double speed = SPEED;
        double lift = LIFT;
        for (int step = 0; step < Math.min(tick, LANDING); step++) {
            out += speed;
            up = Math.max(0, up + lift);
            speed *= DRAG;
            lift -= GRAVITY;
        }
        return Kit.horizontal(degrees, out).add(0, up, 0);
    }

    private static void reward(Stage stage, Timeline timeline, Vec base, int start, double degrees, float yaw, String texture) {
        Actor[] head = new Actor[1];
        timeline.at(start, () -> {
            head[0] = stage.armorStand(base, yaw, SMALL_STAND);
            head[0].equipSkull(Slot.HELMET, texture);
        });
        timeline.repeat(start + 1, start + LANDING, 1, step -> head[0].teleport(base.add(position(degrees, step + 1)), yaw, 0));
        timeline.at(END, () -> head[0].remove());
    }
}
