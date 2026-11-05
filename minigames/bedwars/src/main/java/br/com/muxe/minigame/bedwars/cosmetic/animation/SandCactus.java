package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class SandCactus {

    static final String HEAD = "d4b3ea5cb6b6f046e326621ca11ffb7d6aec22d66c0d81e5039b19ee4400309f";
    static final double MAX_SPAWN_RADIUS = 0.75;
    static final double STAND_BELOW = 0.656;
    static final double BURIED = 0.15;
    static final double HEAD_CENTER = 0.95;
    static final double LAUNCH = 0.8;
    static final double MIN_DRIFT = 0.03;
    static final double MAX_DRIFT = 0.07;
    static final double GRAVITY = 0.08;
    static final double DRAG = 0.98;
    static final int FLIGHT_LIMIT = 40;
    static final double[] TRAIL = {0.61, 0.88, 0.36};

    private SandCactus() {
    }

    static Body launch(Vec ground, double degrees, double drift) {
        return new Body(ground, Kit.horizontal(degrees, drift).add(0, LAUNCH, 0), GRAVITY, DRAG, 1);
    }

    static void jump(Stage stage, Timeline timeline, Vec origin, int start, RandomGenerator random) {
        Vec ground = origin.add(Kit.horizontal(random.nextDouble() * 360, random.nextDouble() * MAX_SPAWN_RADIUS));
        Body body = launch(ground, random.nextDouble() * 360, Kit.between(random, MIN_DRIFT, MAX_DRIFT));
        double turn = random.nextDouble() * 360;
        Actor[] cactus = new Actor[1];
        int sand = Kit.blockId(Material.SAND, 0);
        timeline.at(start, () -> {
            cactus[0] = stage.armorStand(ground.add(0, -STAND_BELOW, 0), 0, StandStyle.HIDDEN_SMALL);
            cactus[0].pose(Pose.STANDING.withHead(new Vec(0, turn, 0)));
            cactus[0].equipSkull(Slot.HELMET, HEAD);
            stage.particle("BLOCK_DUST", ground.add(0, 0.3, 0), 8, new Vec(0.2, 0.05, 0.2), 0.08, sand);
            stage.sound("dig.cloth", ground, 0.8f, (float) Kit.between(random, 0.97, 1.16));
            stage.sound("fireworks.launch", ground, 0.5f, (float) Kit.between(random, 1.44, 1.75));
        });
        float plopPitch = (float) Kit.between(random, 1.5, 2.0);
        float clothPitch = (float) Kit.between(random, 1.2, 1.5);
        timeline.repeat(start + 1, start + FLIGHT_LIMIT, 1, offset -> {
            if (cactus[0].removed()) {
                return;
            }
            if (body.landed() || offset + 1 == FLIGHT_LIMIT) {
                Vec at = body.position();
                stage.particle("EXPLOSION_NORMAL", at.add(0, 0.15, 0), 6, Vec.ZERO, 0.02);
                stage.sound("dig.cloth", at, 0.8f, clothPitch);
                stage.sound("mob.chicken.plop", at, 0.6f, plopPitch);
                cactus[0].remove();
                return;
            }
            boolean landed = body.step(stage);
            Vec stand = body.position().add(0, landed ? -STAND_BELOW - BURIED : -STAND_BELOW, 0);
            cactus[0].teleport(stand, 0, 0);
            Kit.dust(stage, stand.add(0, HEAD_CENTER, 0), TRAIL[0], TRAIL[1], TRAIL[2]);
        });
    }
}
