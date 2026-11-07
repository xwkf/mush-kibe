package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class ChineseDragonsFinalKill implements SceneAnimation {

    static final String DRAGON = "f6dc0314b923a5a9962a09909597dd4cbc231f24952c4ab999479ca5cae0ac1c";
    static final int DRAGONS = 5;
    static final double SPACING = 360.0 / DRAGONS;
    static final double YAW_JITTER = 18;
    static final double MIN_CLIMB = 5;
    static final double MAX_CLIMB = 25;
    static final double STAND_ABOVE = 0.78125;
    static final double ROCKET_ABOVE = 1;
    static final double GROWL_ABOVE = 1;
    static final float GROWL_PITCH = SoundPitch.of(126);
    static final int[] COLORS = {0xE84034, 0xDECD34};
    static final Vec TRAIL_SPREAD = new Vec(0.1, 0.1, 0.1);
    static final Vec EMBER_SPREAD = new Vec(0.5, 0.5, 0.5);
    static final int END = 39;

    @Override
    public String id() {
        return "dragoes-chineses";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        timeline.at(0, () -> stage.sound("mob.enderdragon.growl", origin.add(0, GROWL_ABOVE, 0), 1, GROWL_PITCH));
        Kit.flash(stage, timeline, 0, origin.add(0, ROCKET_ABOVE, 0), Kit.burst(Kit.BALL, false, true, COLORS));
        float facing = Vec.ZERO.yawTowards(scene.facing());
        Vec spawn = origin.add(0, STAND_ABOVE, 0);
        for (int index = 0; index < DRAGONS; index++) {
            float yaw = (float) (facing + SPACING * index + Kit.between(random, -YAW_JITTER, YAW_JITTER));
            float climb = (float) Kit.between(random, MIN_CLIMB, MAX_CLIMB);
            dragon(stage, timeline, new DragonFlight(spawn, yaw, climb));
        }
        timeline.lasts(END);
    }

    private static void dragon(Stage stage, Timeline timeline, DragonFlight flight) {
        Actor[] dragon = new Actor[1];
        timeline.at(0, () -> {
            dragon[0] = stage.armorStand(flight.spawn(), flight.yaw(), StandStyle.HIDDEN);
            dragon[0].pose(Pose.STANDING.withHead(new Vec(-flight.climb(), 0, 0)));
            dragon[0].equipSkull(Slot.HELMET, DRAGON);
        });
        timeline.repeat(0, DragonFlight.STEPS - 1, 1, tick -> dragon[0].teleport(flight.stand(tick), flight.yaw(), -flight.climb()));
        timeline.repeat(0, END, 1, tick -> stage.particle("REDSTONE", flight.trail(tick), 5, TRAIL_SPREAD, 0));
        timeline.at(END, () -> {
            stage.particle("LAVA", flight.trail(END), 3, EMBER_SPREAD, 0.1);
            dragon[0].remove();
        });
    }
}
