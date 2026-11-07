package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class KrakenBed {

    static final double BOB = 0.05;
    static final double BOB_PERIOD = 18;
    static final int BOB_LOW = 13;
    static final int CALM_TICKS = 8;
    static final double SHAKE = 0.05;
    static final double SQUEEZE_SINK = 0.2;
    static final int SQUEEZE_TICKS = 3;
    static final double PLUNGE_SINK = 0.06;

    private final BedFrame bed;
    private final Kit.BedDisplay display;

    private KrakenBed(BedFrame bed, Kit.BedDisplay display) {
        this.bed = bed;
        this.display = display;
    }

    static KrakenBed afloat(Stage stage, BedFrame bed) {
        return new KrakenBed(bed, Kit.bed(stage, bed));
    }

    void move(int tick, RandomGenerator random) {
        double bob = bob(tick);
        double sink = sink(tick);
        display.headStand().teleport(bed.head().add(shake(tick, random)).add(0, bob - sink, 0), 0, 0);
        display.footStand().teleport(bed.foot().add(shake(tick, random)).add(0, -bob - sink, 0), 0, 0);
    }

    Vec head(int tick) {
        return bed.head().add(0, -sink(tick), 0);
    }

    Vec foot(int tick) {
        return bed.foot().add(0, -sink(tick), 0);
    }

    void remove() {
        display.remove();
    }

    static double bob(int tick) {
        double calm = Math.clamp((KrakenAnimation.STRIKE - tick) / (double) CALM_TICKS, 0, 1);
        return -BOB * KrakenAnimation.level(tick) * calm * Math.cos(2 * Math.PI * (tick - BOB_LOW) / BOB_PERIOD);
    }

    static double sink(int tick) {
        double sink = 0;
        for (int squeeze : KrakenAnimation.SQUEEZES) {
            sink += SQUEEZE_SINK * Math.clamp((tick - squeeze) / (double) SQUEEZE_TICKS, 0, 1);
        }
        return tick > KrakenAnimation.PLUNGE ? sink + PLUNGE_SINK * (tick - KrakenAnimation.PLUNGE) : sink;
    }

    private static Vec shake(int tick, RandomGenerator random) {
        if (tick < KrakenAnimation.SHAKE) {
            return Vec.ZERO;
        }
        return new Vec(Kit.between(random, -SHAKE, SHAKE), 0, Kit.between(random, -SHAKE, SHAKE));
    }
}
