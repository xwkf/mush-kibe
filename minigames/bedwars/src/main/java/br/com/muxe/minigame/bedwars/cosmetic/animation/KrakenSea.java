package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class KrakenSea {

    static final double SURFACE = 0.1;
    static final int MIST_STEPS = 10;
    static final double MIST_WIDTH = 0.5;
    static final double MIST_GROWTH = 0.11;
    static final double MIST_LENGTH = 0.7;
    static final double MIST_DENSITY = 0.9;
    static final double MIST_HEIGHT = 0.03;
    static final double WAKE = 1.5;
    static final double POOL = 2.6;
    static final double RIPPLE_START = 0.6;
    static final double RIPPLE_GROWTH = 0.3;
    static final int RIPPLE_STEPS = 9;
    static final int RIPPLE_POINTS = 18;
    static final int SWIM_LOW = 38;
    static final int SWIM_HIGH = 81;
    static final float SWIM_VOLUME = 0.8f;
    static final int LAP_PITCH = 94;
    static final float LAP_VOLUME = 0.7f;

    private final BedFrame bed;
    private final Stage stage;
    private final Vec surface;

    KrakenSea(BedFrame bed, Stage stage) {
        this.bed = bed;
        this.stage = stage;
        this.surface = bed.middle().add(0, SURFACE, 0);
    }

    void mist(int tick) {
        int step = (int) Math.round(KrakenAnimation.level(tick) * MIST_STEPS);
        int count = (int) Math.round(step * MIST_DENSITY);
        if (count > 0) {
            double width = MIST_WIDTH + MIST_GROWTH * step;
            stage.particle("SPELL_MOB", surface, count, spread(bed, width, MIST_HEIGHT, width * MIST_LENGTH), 0);
        }
    }

    void wake() {
        stage.particle("WATER_WAKE", surface, 4, new Vec(WAKE, 0, WAKE), 0.02);
    }

    void lap(RandomGenerator random) {
        double distance = POOL * Math.sqrt(random.nextDouble());
        Vec at = bed.around(surface, distance, random.nextDouble() * 360, 0);
        stage.particle("WATER_SPLASH", at, 10, new Vec(0.12, 0.02, 0.12), 0.12);
    }

    void swim(int tick, RandomGenerator random) {
        float volume = (float) (SWIM_VOLUME * KrakenAnimation.level(tick));
        stage.sound("game.neutral.swim", bed.middle(), volume, SoundPitch.of(SWIM_LOW + random.nextInt(SWIM_HIGH - SWIM_LOW + 1)));
    }

    void splash(int tick) {
        stage.sound("random.splash", bed.middle(), (float) (LAP_VOLUME * KrakenAnimation.level(tick)), SoundPitch.of(LAP_PITCH));
    }

    void ripple(int step) {
        double radius = RIPPLE_START + RIPPLE_GROWTH * step;
        for (int point = 0; point < RIPPLE_POINTS; point++) {
            Vec at = bed.around(surface, radius, 360.0 * point / RIPPLE_POINTS, 0);
            stage.particle("WATER_SPLASH", at, 2, new Vec(0.03, 0.03, 0.03), 0.02);
        }
    }

    static Vec spread(BedFrame bed, double across, double up, double along) {
        Vec right = bed.right();
        Vec forward = bed.forward();
        return new Vec(Math.abs(right.x()) * across + Math.abs(forward.x()) * along, up, Math.abs(right.z()) * across + Math.abs(forward.z()) * along);
    }
}
