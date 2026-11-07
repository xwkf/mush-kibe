package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class DrownFinalKill implements SceneAnimation {

    static final List<String> WAVE = List.of(
        "..CCCCC..CCCCC..CCCCC",
        ".CBBBC..CBBBC..CBBBC.",
        ".BB.....BB.....BB....",
        "BBBB...BBBB...BBBB...",
        "BAAABBBBAAABBBBAAABB.",
        "AAAABBAAAAABBAAAAABBB",
        ".AAAAAAAAAAAAAAAAAAA."
    );
    static final Map<Character, double[]> WATER = Map.of(
        'A', new double[] {0.082, 0.314, 0.737},
        'B', new double[] {0.247, 0.498, 0.957},
        'C', new double[] {0.957, 0.969, 0.996}
    );
    static final double PIXEL = 1.0 / 7;
    static final double ROW_HEIGHT = 1.25 / 7;
    static final double CENTRE_COLUMN = 10.5;
    static final double SWAY = 1.0 / 28;
    static final double SWAY_PERIOD = 24;
    static final double SWAY_PEAK = 5.5;
    static final int ROWS_PER_TICK = 2;
    static final double[] SQUID_SINK = {1, -0.1875, -0.75, -0.96875, -1};
    static final int[] WATER_TICKS = {7, 14, 22, 27};
    static final float[] WATER_PITCHES = {1.27f, 1.06f, 0.86f, 0.67f};
    static final int END = 39;

    @Override
    public String id() {
        return "afogar";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec right = scene.right();
        float towardHitter = origin.yawTowards(origin.subtract(scene.facing()));
        stage.sound("random.splash", origin, 1, 1);
        stage.sound("random.explode", origin, 1, 1);
        stage.sound("liquid.water", origin, 1, 1.46f);
        stage.particle("DRIP_WATER", origin, 100, new Vec(1, 1.5, 1), 1);
        stage.particle("BLOCK_DUST", origin, 200, new Vec(0.25, 0.25, 0.25), 1, Kit.blockId(Material.LAPIS_BLOCK, 0));
        Actor squid = stage.mob("Squid", origin.add(0, SQUID_SINK[0], 0), towardHitter);
        timeline.repeat(1, SQUID_SINK.length - 1, 1, offset -> squid.teleport(origin.add(0, SQUID_SINK[offset + 1], 0), towardHitter, 0));
        timeline.repeat(0, END, 1, tick -> wave(stage, origin, right, tick));
        for (int index = 0; index < WATER_TICKS.length; index++) {
            float pitch = WATER_PITCHES[index];
            timeline.at(WATER_TICKS[index], () -> stage.sound("liquid.water", origin, 1, pitch));
        }
        timeline.at(END, () -> {
            stage.sound("random.splash", origin, 1, 0.49f);
            squid.remove();
        });
        timeline.lasts(END + 1);
    }

    static int rows(int tick) {
        return Math.min(WAVE.size(), ROWS_PER_TICK * (tick + 1));
    }

    static double sway(int tick) {
        return SWAY * Math.cos(2 * Math.PI * (tick - SWAY_PEAK) / SWAY_PERIOD);
    }

    static Vec pixel(Vec origin, Vec right, int level, int column, int tick) {
        double along = (column - CENTRE_COLUMN) * PIXEL + (level + 1) * sway(tick);
        return origin.add(right.multiply(along)).add(0, (level + 1) * ROW_HEIGHT, 0);
    }

    private static void wave(Stage stage, Vec origin, Vec right, int tick) {
        for (int level = 0; level < rows(tick); level++) {
            String line = WAVE.get(WAVE.size() - 1 - level);
            for (int column = 0; column < line.length(); column++) {
                double[] color = WATER.get(line.charAt(column));
                if (color != null) {
                    Kit.dust(stage, pixel(origin, right, level, column, tick), color[0], color[1], color[2]);
                }
            }
        }
    }
}
