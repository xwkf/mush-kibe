package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class DiscoPartyFinalKill implements SceneAnimation {

    static final String DISCO_BALL = "99bf4dd7dd1926968b185aa197a41e16c417dc0aa3aac6c42e39444de165f415";
    static final double BALL_ABOVE = 1.06;
    static final float SPIN = -18.28125f;
    static final double RING_RADIUS = 1.2;
    static final double RING_TURN = 11.5;
    static final int NOTE_EVERY = 2;
    static final double LOOSE_NOTE_REACH = 1;
    static final double LOOSE_NOTE_LOW = 1.2;
    static final double LOOSE_NOTE_HIGH = 3;
    static final double MUSIC_ABOVE = 1.5;
    static final float MUSIC_VOLUME = 10;
    static final int BEAT = 4;
    static final int BEATS = 22;
    static final float DRUM_PITCH = 59 / 63f;
    static final float FIRST_PITCH = 44 / 63f;
    static final float LOW_PITCH = 33 / 63f;
    static final float LAST_PITCH = 37 / 63f;
    static final int LOW_FROM_BEAT = 12;
    static final int LAST_FROM_BEAT = 15;
    static final int DISCS = 10;
    static final int FIRST_DISC = 9;
    static final int DISC_EVERY = 6;
    static final double DISC_TURN = 34;
    static final double DISC_MIN_SPREAD = 0.12;
    static final double DISC_MAX_SPREAD = 0.27;
    static final double DISC_MIN_LIFT = 0.16;
    static final double DISC_MAX_LIFT = 0.34;
    static final int CONFETTI = 50;
    static final double CONFETTI_SPREAD = 0.87;
    static final int FINALE = 77;
    static final double FINALE_ABOVE = 1.06;
    static final int[] BALL_COLORS = {0xFA3C27, 0x60FF4D, 0x4647FF};
    static final int[] BURST_COLORS = {0xFFFB42, 0xF23EFF, 0x46E6FF};
    static final int BALL_GONE = 82;
    static final int END = 98;

    @Override
    public String id() {
        return "festa-disco";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        stage.particle("REDSTONE", origin, CONFETTI, new Vec(CONFETTI_SPREAD, CONFETTI_SPREAD, CONFETTI_SPREAD), 1);
        Actor ball = stage.armorStand(origin.add(0, BALL_ABOVE, 0), 0, StandStyle.HIDDEN);
        ball.equipSkull(Slot.HELMET, DISCO_BALL);
        timeline.repeat(0, BALL_GONE - 1, 1, tick -> ball.teleport(origin.add(0, BALL_ABOVE, 0), SPIN * (tick + 1), 0));
        double ring = random.nextDouble() * 360;
        timeline.repeat(0, BALL_GONE - 1, NOTE_EVERY, tick -> {
            stage.particle("NOTE", ringNote(origin, ring, tick), 1, Vec.ZERO, 1);
            Vec loose = origin.add(Kit.between(random, -LOOSE_NOTE_REACH, LOOSE_NOTE_REACH), Kit.between(random, LOOSE_NOTE_LOW, LOOSE_NOTE_HIGH),
                Kit.between(random, -LOOSE_NOTE_REACH, LOOSE_NOTE_REACH));
            stage.particle("NOTE", loose, 1, Vec.ZERO, 1);
        });
        Vec speaker = origin.add(0, MUSIC_ABOVE, 0);
        timeline.repeat(0, (BEATS - 1) * BEAT, BEAT, tick -> {
            int beat = tick / BEAT;
            if (beat % 2 == 0) {
                stage.sound("note.bd", speaker, MUSIC_VOLUME, DRUM_PITCH);
            }
            stage.sound("note.bass", speaker, MUSIC_VOLUME, melody(beat));
            stage.sound("note.harp", speaker, MUSIC_VOLUME, melody(beat));
        });
        List<Actor> discs = new ArrayList<>();
        double firstDisc = random.nextDouble() * 360;
        for (int disc = 0; disc < DISCS; disc++) {
            Vec velocity = Kit.horizontal(firstDisc + disc * DISC_TURN, Kit.between(random, DISC_MIN_SPREAD, DISC_MAX_SPREAD))
                .add(0, Kit.between(random, DISC_MIN_LIFT, DISC_MAX_LIFT), 0);
            timeline.at(FIRST_DISC + disc * DISC_EVERY, () -> {
                Actor record = stage.item(speaker, Material.RECORD_3, 0);
                record.velocity(velocity);
                discs.add(record);
            });
        }
        Vec finale = origin.add(0, FINALE_ABOVE, 0);
        Kit.flash(stage, timeline, FINALE, finale, Kit.burst(Kit.LARGE_BALL, true, false, BALL_COLORS));
        Kit.flash(stage, timeline, FINALE, finale, Kit.burst(Kit.BURST, false, false, BURST_COLORS));
        timeline.at(FINALE, () -> stage.sound("random.explode", origin.add(0, 1, 0), 1.5f, 1));
        timeline.at(BALL_GONE, ball::remove);
        timeline.at(END, () -> discs.forEach(Actor::remove));
        timeline.lasts(END + 1);
    }

    static float melody(int beat) {
        if (beat >= LAST_FROM_BEAT) {
            return LAST_PITCH;
        }
        return beat >= LOW_FROM_BEAT ? LOW_PITCH : FIRST_PITCH;
    }

    static Vec ringNote(Vec origin, double start, int tick) {
        return origin.add(Kit.horizontal(start + RING_TURN * tick, RING_RADIUS)).add(0, BALL_ABOVE, 0);
    }
}
