package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.Body;
import br.com.muxe.minigame.api.cosmetic.animation.Kit;
import br.com.muxe.minigame.api.cosmetic.animation.SoundPitch;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class VictoryBlastAnimation implements BedAnimation {

    static final String[] TROPHIES = {
        "567f2bc411dd38f3111fe1a37e179bdcab66e509c2aff2702814d5e07a4faeb6",
        "5d308ae27b58b69645497f9da86597eda947eacd10c29e3d4bbf3bc76ceb1eab"
    };
    static final StandStyle TROPHY_STAND = new StandStyle(true, false, false, false, false);
    static final double HEAD_BASE = 1.4375;
    static final int SHARDS = 200;
    static final int SUNFLOWERS = 17;
    static final double SUNFLOWER_SPEED = 0.2;
    static final double SUNFLOWER_MIN_UP = 0.2;
    static final double SUNFLOWER_MAX_UP = 0.5;
    static final int TROPHY_COUNT = 10;
    static final int TROPHY_EVERY = 2;
    static final double TROPHY_JITTER = 10;
    static final double TROPHY_MIN_SPEED = 0.25;
    static final double TROPHY_MAX_SPEED = 0.52;
    static final double TROPHY_MIN_UP = 0.94;
    static final double TROPHY_MAX_UP = 1.38;
    static final int SPIN = 16;
    static final double FIRST_ANGLE = -90;
    static final int END = 100;

    @Override
    public String id() {
        return "explosao-da-vitoria";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        List<Actor> leftovers = new ArrayList<>();
        timeline.at(0, () -> {
            stage.sound("dig.stone", middle, 1, SoundPitch.of(94));
            stage.sound("random.break", middle, 1, SoundPitch.of(47));
            int gold = Kit.blockId(Material.GOLD_BLOCK, 0);
            for (int shard = 0; shard < SHARDS; shard++) {
                Vec push = new Vec(Kit.between(random, -0.5, 0.5), Kit.between(random, 0, 0.5), Kit.between(random, -0.5, 0.5));
                stage.particle("BLOCK_DUST", middle.add(0, 0.5, 0), 0, push, 1, gold);
            }
        });
        for (int index = 0; index < SUNFLOWERS; index++) {
            int sunflower = index;
            timeline.at(sunflower, () -> {
                Vec velocity = direction(bed, FIRST_ANGLE + 360.0 * sunflower / SUNFLOWERS).multiply(SUNFLOWER_SPEED)
                    .add(0, Kit.between(random, SUNFLOWER_MIN_UP, SUNFLOWER_MAX_UP), 0);
                Actor item = stage.item(middle, Material.DOUBLE_PLANT, 0);
                item.velocity(velocity);
                leftovers.add(item);
                stage.sound("random.pop", middle, 1, popPitch(sunflower));
            });
        }
        for (int index = 0; index < TROPHY_COUNT; index++) {
            int trophy = index;
            int launch = trophy * TROPHY_EVERY;
            Vec velocity = direction(bed, FIRST_ANGLE + 360.0 * trophy / TROPHY_COUNT + Kit.between(random, -TROPHY_JITTER, TROPHY_JITTER))
                .multiply(Kit.between(random, TROPHY_MIN_SPEED, TROPHY_MAX_SPEED))
                .add(0, Kit.between(random, TROPHY_MIN_UP, TROPHY_MAX_UP), 0);
            Body body = Body.living(middle, velocity);
            Actor[] stand = new Actor[1];
            timeline.at(launch, () -> {
                stand[0] = stage.armorStand(middle.add(0, -HEAD_BASE, 0), bed.yaw(), TROPHY_STAND);
                stand[0].equipSkull(Slot.HELMET, TROPHIES[trophy % TROPHIES.length]);
                leftovers.add(stand[0]);
            });
            timeline.repeat(launch + 1, END - 1, 1, offset -> {
                if (body.landed()) {
                    return;
                }
                body.step(stage);
                stand[0].teleport(body.position().add(0, -HEAD_BASE, 0), bed.yaw() + spin(offset + 1), 0);
            });
        }
        timeline.at(END, () -> leftovers.forEach(Actor::remove));
    }

    static float popPitch(int sunflower) {
        return 1 + sunflower / (float) SUNFLOWERS;
    }

    static float spin(int step) {
        double left = 1 - Math.min(step, SPIN) / (double) SPIN;
        return (float) (-360 * (1 - left * left * left));
    }

    private static Vec direction(BedFrame bed, double degrees) {
        double radians = Math.toRadians(degrees);
        return bed.forward().multiply(Math.cos(radians)).add(bed.right().multiply(Math.sin(radians)));
    }
}
