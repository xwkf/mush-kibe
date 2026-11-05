package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public final class IgniteFinalKill implements SceneAnimation {

    static final List<String> FLAME = List.of(
        "...A...",
        "...A...",
        "..AA...",
        "..ACA..",
        "..ACAA.",
        ".ACCCAA",
        "ACCCCCA",
        "ACDDCCA",
        "ACBBBCA",
        "ACBBBCA",
        ".ABBBA.",
        "..AAA.."
    );
    static final Map<Character, double[]> PALETTE = Map.of(
        'A', new double[] {0.88, 0.29, 0.23},
        'B', new double[] {1, 1, 1},
        'C', new double[] {0.88, 0.41, 0.23},
        'D', new double[] {0.88, 0.54, 0.23}
    );
    static final double PIXEL = 1 / 6.0;
    static final double SWAY = 0.25;
    static final int SWAY_PERIOD = 16;
    static final double[] SINK = {-1.5, -2.19, -2.44, -2.5};
    static final int BURST = 70;
    static final double BURST_ABOVE = 0.5;
    static final double BURST_MIN_SPEED = 0.1;
    static final double BURST_MAX_SPEED = 0.18;
    static final double BURST_MIN_LIFT = 0.01;
    static final double BURST_MAX_LIFT = 0.02;
    static final int SMOKE = 10;
    static final double SMOKE_SPREAD = 0.25;
    static final int RATTLES = 5;
    static final double RATTLE_EVERY = 6.25;
    static final float FIRST_RATTLE = 92 / 63f;
    static final float RATTLE_STEP = 12.5f / 63;
    static final float FIREBALL_PITCH = 31 / 63f;
    static final int END = 36;

    @Override
    public String id() {
        return "incendiar";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        stage.sound("ambient.weather.thunder", origin, 1, 1);
        stage.sound("random.explode", origin, 1, 1);
        Vec burst = origin.add(0, BURST_ABOVE, 0);
        for (int flame = 0; flame < BURST; flame++) {
            Vec direction = Kit.horizontal(random.nextDouble() * 360, Kit.between(random, BURST_MIN_SPEED, BURST_MAX_SPEED))
                .add(0, Kit.between(random, BURST_MIN_LIFT, BURST_MAX_LIFT), 0);
            stage.particle("FLAME", burst, 0, direction, 1);
        }
        float towardsKiller = origin.yawTowards(origin.subtract(scene.facing()));
        Actor skeleton = stage.mob("Skeleton", origin, towardsKiller);
        timeline.repeat(0, SINK.length - 1, 1, tick -> skeleton.teleport(origin.add(0, SINK[tick], 0), towardsKiller, 0));
        for (int rattle = 0; rattle < RATTLES; rattle++) {
            float pitch = FIRST_RATTLE - rattle * RATTLE_STEP;
            timeline.at((int) Math.round(rattle * RATTLE_EVERY), () -> stage.sound("mob.skeleton.death", origin, 1, pitch));
        }
        Vec left = scene.right().multiply(-1);
        timeline.repeat(0, END - 1, 1, tick -> {
            stage.particle("SMOKE_NORMAL", origin, SMOKE, new Vec(SMOKE_SPREAD, SMOKE_SPREAD, SMOKE_SPREAD), 0);
            for (Pixel pixel : pixels(origin, left, tick)) {
                Kit.dust(stage, pixel.position(), pixel.color()[0], pixel.color()[1], pixel.color()[2]);
            }
        });
        timeline.at(END, () -> {
            stage.sound("mob.ghast.fireball", origin, 1, FIREBALL_PITCH);
            skeleton.remove();
        });
        timeline.lasts(END + 1);
    }

    static List<Pixel> pixels(Vec origin, Vec left, int tick) {
        int rows = Math.min(FLAME.size(), tick + 1);
        double middle = (FLAME.getFirst().length() - 1) / 2.0;
        double sway = SWAY * Math.sin(2 * Math.PI * tick / SWAY_PERIOD);
        List<Pixel> pixels = new ArrayList<>();
        for (int row = 1; row <= rows; row++) {
            String line = FLAME.get(FLAME.size() - row);
            double shift = sway * row / FLAME.size();
            for (int column = 0; column < line.length(); column++) {
                double[] color = PALETTE.get(line.charAt(column));
                if (color != null) {
                    Vec at = origin.add(left.multiply((column - middle) * PIXEL + shift)).add(0, row * PIXEL, 0);
                    pixels.add(new Pixel(at, color));
                }
            }
        }
        return pixels;
    }

    record Pixel(Vec position, double[] color) {
    }
}
