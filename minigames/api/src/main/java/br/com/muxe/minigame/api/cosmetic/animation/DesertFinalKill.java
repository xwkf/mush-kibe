package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class DesertFinalKill implements SceneAnimation {

    static final double SUN_ABOVE = 3.8;
    static final double SUN_TOWARD_KILLER = 0.1;
    static final double SOUND_ABOVE = 3.75;
    static final int SUN_FROM = 1;
    static final int SUN_UNTIL = 73;
    static final int SUN_EVERY = 2;
    static final double TURN_PER_TICK = -3;
    static final int SPARKS_PER_TICK = 2;
    static final double SPARK_REACH = 1.3;
    static final List<Integer> TWINKLES = List.of(12, 26, 36, 48, 59);
    static final double DROPS_BELOW_CENTER = 0.2;
    static final StandStyle VICTIM = new StandStyle(false, false, true, false, true);
    static final Pose REACHING_UP = new Pose(new Vec(335, 0, 0), Vec.ZERO, new Vec(190, 0, 330), new Vec(210, 0, 15), Vec.ZERO, Vec.ZERO);
    static final int SINK_FROM = 3;
    static final int SINK_UNTIL = 31;
    static final double SINK_DEPTH = 2.125;
    static final double WOBBLE = 7;
    static final double WOBBLE_PERIOD = 12;
    static final int SWALLOW = 32;
    static final int SAND_FROM = 6;
    static final int SAND_EVERY = 2;
    static final int GRIND_FROM = 8;
    static final int GRIND_EVERY = 4;
    static final List<Integer> CACTI = List.of(10, 20, 23, 26, 26, 27, 28);
    static final int END = SUN_UNTIL + SunDrops.fallTicks(SUN_ABOVE - DROPS_BELOW_CENTER) + 1;

    @Override
    public String id() {
        return "deserto";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec right = scene.right();
        Vec center = origin.add(0, SUN_ABOVE, 0).subtract(scene.facing().multiply(SUN_TOWARD_KILLER));
        timeline.at(SUN_FROM, () -> {
            stage.sound("fireworks.twinkle", origin.add(0, SOUND_ABOVE, 0), 1.2f, 88 / 63f);
            stage.sound("random.levelup", origin.add(0, SOUND_ABOVE, 0), 0.6f, 2);
            Quicksand.spread(stage, origin, random);
        });
        sinking(scene, stage, timeline, random);
        timeline.repeat(SUN_FROM, SUN_UNTIL, SUN_EVERY, offset -> DesertSun.draw(stage, center, right, turn(SUN_FROM + offset)));
        timeline.repeat(SUN_FROM, SUN_UNTIL, 1, offset -> {
            for (int spark = 0; spark < SPARKS_PER_TICK; spark++) {
                Vec at = center.add(right.multiply(Kit.between(random, -SPARK_REACH, SPARK_REACH))).add(0, Kit.between(random, -SPARK_REACH, SPARK_REACH), 0);
                stage.particle("FIREWORKS_SPARK", at, 1, Vec.ZERO, 0);
            }
        });
        TWINKLES.forEach(tick -> timeline.at(tick, () -> stage.sound("fireworks.twinkle", origin.add(0, SOUND_ABOVE, 0), 0.4f, (float) Kit.between(random, 1.68, 1.9))));
        SunDrops drops = new SunDrops(center.add(0, -DROPS_BELOW_CENTER, 0), right, origin.y());
        timeline.repeat(SUN_FROM, END, 1, offset -> drops.tick(stage, random, SUN_FROM + offset < SUN_UNTIL));
        CACTI.forEach(tick -> SandCactus.jump(stage, timeline, origin, tick, random));
        timeline.lasts(END + 1);
    }

    static double turn(int tick) {
        return TURN_PER_TICK * tick;
    }

    static double sunk(int tick) {
        return SINK_DEPTH * Math.clamp((double) (tick - SINK_FROM) / (SINK_UNTIL - SINK_FROM), 0, 1);
    }

    static float wobble(float yaw, int tick) {
        return (float) (yaw - WOBBLE * Math.sin(2 * Math.PI * tick / WOBBLE_PERIOD));
    }

    private static void sinking(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        float yaw = VictimBody.facingKiller(scene);
        int sand = Kit.blockId(Material.SAND, 0);
        Actor[] victim = new Actor[1];
        timeline.at(SUN_FROM, () -> {
            victim[0] = stage.armorStand(origin, yaw, VICTIM);
            victim[0].pose(REACHING_UP);
            VictimBody.wearHead(scene, victim[0]);
        });
        timeline.repeat(SUN_FROM + 1, SWALLOW - 1, 1, offset -> {
            int tick = SUN_FROM + 1 + offset;
            victim[0].teleport(origin.add(0, -sunk(tick), 0), wobble(yaw, tick), 0);
        });
        timeline.at(4, () -> stage.sound("dig.sand", origin, 1, 44 / 63f));
        timeline.repeat(SAND_FROM, SWALLOW - 1, SAND_EVERY,
            offset -> stage.particle("BLOCK_DUST", origin.add(0, 0.1, 0), 4, new Vec(0.4, 0.05, 0.4), 0.05, sand));
        timeline.repeat(GRIND_FROM, SWALLOW - 1, GRIND_EVERY, offset -> stage.sound("dig.sand", origin, 0.7f, (float) Kit.between(random, 0.58, 0.86)));
        timeline.at(SWALLOW, () -> {
            victim[0].remove();
            stage.particle("BLOCK_DUST", origin.add(0, 0.1, 0), 25, new Vec(0.5, 0.1, 0.5), 0.1, sand);
            stage.sound("dig.sand", origin, 1.2f, 37 / 63f);
            stage.sound("random.burp", origin, 1, 44 / 63f);
        });
    }
}
