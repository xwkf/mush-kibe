package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class LightningAnimation implements BedAnimation {

    static final int[] STRIKES = {0, 21, 31};
    static final float[] BLAST_PITCH = {0.51f, 0.62f, 0.56f};
    static final int FLAMES = 150;
    static final double FLAME_LIFT = 0.5;
    static final double MIN_FLAME_SPEED = 0.24;
    static final double MAX_FLAME_SPEED = 0.5;
    static final double MAX_FLAME_RISE = 0.05;

    @Override
    public String id() {
        return "raio";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        Vec head = bed.head();
        timeline.at(0, () -> {
            stage.particle("EXPLOSION_HUGE", middle, 1, Vec.ZERO, 1);
            for (int flame = 0; flame < FLAMES; flame++) {
                Vec direction = Kit.horizontal(random.nextDouble() * 360, Kit.between(random, MIN_FLAME_SPEED, MAX_FLAME_SPEED))
                    .add(0, Kit.between(random, 0, MAX_FLAME_RISE), 0);
                stage.particle("FLAME", middle.add(0, FLAME_LIFT, 0), 0, direction, 1);
            }
        });
        for (int strike = 0; strike < STRIKES.length; strike++) {
            float pitch = BLAST_PITCH[strike];
            timeline.at(STRIKES[strike], () -> {
                stage.lightning(head);
                stage.sound("random.explode", head, 2, pitch);
            });
        }
    }
}
