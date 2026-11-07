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

public final class GalacticFinalKill implements SceneAnimation {

    static final double LAUNCH_ABOVE = 1;
    static final int[] COLORS = {0x3C3A81, 0xFBBC4D};
    static final int STARS = 12;
    static final int FIRST_STAR = 10;
    static final int STAR_EVERY = 2;
    static final double STAR_TURN = 30;
    static final double STAR_SPREAD = 0.18;
    static final double STAR_LIFT = 0.5;
    static final float CHIME_VOLUME = 0.8f;
    static final float FIRST_PITCH = 94 / 63f;
    static final float PITCH_STEP = 2.5f / 63;
    static final int LEVEL_UP = 37;
    static final int END = 98;

    @Override
    public String id() {
        return "galatico";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec launch = scene.origin().add(0, LAUNCH_ABOVE, 0);
        Kit.flash(stage, timeline, 0, launch, Kit.burst(Kit.BALL, true, true, COLORS));
        double first = random.nextDouble() * 360;
        List<Actor> stars = new ArrayList<>();
        for (int star = 0; star < STARS; star++) {
            int index = star;
            timeline.at(FIRST_STAR + star * STAR_EVERY, () -> {
                Actor item = stage.item(launch, Material.NETHER_STAR, 0);
                item.velocity(throwing(first, index));
                stars.add(item);
                stage.sound("note.pling", launch, CHIME_VOLUME, pitch(index));
            });
        }
        timeline.at(LEVEL_UP, () -> stage.sound("random.levelup", launch, 1, FIRST_PITCH));
        timeline.at(END, () -> stars.forEach(Actor::remove));
        timeline.lasts(END + 1);
    }

    static Vec throwing(double first, int star) {
        return Kit.horizontal(first + star * STAR_TURN, STAR_SPREAD).add(0, STAR_LIFT, 0);
    }

    static float pitch(int star) {
        return FIRST_PITCH + star * PITCH_STEP;
    }
}
