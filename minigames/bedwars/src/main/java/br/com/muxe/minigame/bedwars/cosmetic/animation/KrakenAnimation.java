package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class KrakenAnimation implements BedAnimation {

    static final int FILL = 20;
    static final int DRAIN = 8;
    static final List<Hole> HOLES = List.of(
        new Hole(20, 1.28, 1.47, true, 0),
        new Hole(24, -1.31, -1.44, false, 2.4),
        new Hole(30, -1.12, 1.5, false, 4.1),
        new Hole(34, 1.09, -1.5, true, 1.3));
    static final int STRIKE = 57;
    static final int SHAKE = 63;
    static final int SLAP = 65;
    static final int ROAR = 72;
    static final int[] SQUEEZES = {81, 89, 97};
    static final int PLUNGE = 103;
    static final int[] RIPPLES = {105, 110};
    static final int SWALLOW = 106;
    static final int GONE = 115;
    static final int END = 118;
    static final double DRIP_CHANCE = 0.3;
    static final double VOICE = 0.75;
    static final double SPRAY = 0.8;
    static final double CRUSH = 0.3;
    @SuppressWarnings("deprecation")
    static final int INK = Material.INK_SACK.getId();

    @Override
    public String id() {
        return "kraken";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        Vec voice = middle.add(0, VOICE, 0);
        KrakenSea sea = new KrakenSea(bed, stage);
        KrakenBed floating = KrakenBed.afloat(stage, bed);
        List<KrakenTentacle> tentacles = new ArrayList<>();
        timeline.at(0, () -> {
            stage.sound("liquid.water", middle, 1.5f, SoundPitch.of(37));
            stage.sound("random.splash", middle, 1, SoundPitch.of(31));
        });
        timeline.repeat(0, SWALLOW - 1, 1, tick -> floating.move(tick, random));
        timeline.repeat(0, END - 1, 2, offset -> sea.mist(offset));
        timeline.repeat(2, END - 2, 3, offset -> sea.wake());
        timeline.repeat(3, GONE, 4, offset -> sea.swim(3 + offset, random));
        timeline.repeat(8, END - 1, 9, offset -> sea.splash(8 + offset));
        timeline.repeat(7, GONE - 1, 1, offset -> sea.lap(random));
        for (Hole hole : HOLES) {
            Vec base = middle.add(bed.forward().multiply(hole.ahead())).add(bed.right().multiply(hole.right()));
            Vec aim = hole.head() ? bed.head() : bed.foot();
            timeline.at(hole.rise(), () -> {
                tentacles.add(KrakenTentacle.spawn(stage, base, aim, hole.rise(), hole.phase()));
                burst(stage, base);
            });
        }
        timeline.repeat(HOLES.getFirst().rise(), GONE - 1, 1, offset -> {
            int tick = HOLES.getFirst().rise() + offset;
            tentacles.forEach(tentacle -> tentacle.move(tick));
            if (tick < PLUNGE) {
                tentacles.stream().filter(tentacle -> random.nextDouble() < DRIP_CHANCE).forEach(tentacle -> tentacle.drip(random)
                    .ifPresent(at -> stage.particle("DRIP_WATER", at, 2, new Vec(0.12, 0.12, 0.12), 0)));
            }
        });
        timeline.at(SLAP, () -> {
            stage.sound("mob.slime.attack", voice, 1.5f, SoundPitch.of(31));
            stage.sound("random.splash", voice, 1, SoundPitch.of(81));
            for (Vec half : List.of(floating.head(SLAP), floating.foot(SLAP))) {
                stage.particle("BLOCK_CRACK", half.add(0, CRUSH, 0), 12, new Vec(0.3, 0.3, 0.3), 0.1, Kit.blockId(Material.BED_BLOCK, 0));
            }
        });
        timeline.at(ROAR, () -> {
            stage.sound("mob.enderdragon.growl", voice, 3, SoundPitch.of(31));
            stage.sound("note.bass", voice, 2, SoundPitch.of(31));
            spray(stage, middle, 60);
        });
        for (int squeeze : SQUEEZES) {
            timeline.at(squeeze, () -> squeeze(stage, voice, floating.head(squeeze), floating.foot(squeeze)));
        }
        timeline.at(PLUNGE, () -> {
            stage.sound("mob.enderdragon.growl", voice, 3, SoundPitch.of(40));
            stage.sound("random.splash", voice, 3, SoundPitch.of(31));
            stage.sound("random.explode", voice, 1.5f, SoundPitch.of(88));
            spray(stage, middle, 100);
            stage.particle("WATER_SPLASH", middle.add(0, KrakenSea.SURFACE, 0), 60, KrakenSea.spread(bed, 0.8, 0.2, 0.5), 0.6);
        });
        for (int ripple : RIPPLES) {
            timeline.repeat(ripple, ripple + KrakenSea.RIPPLE_STEPS - 1, 1, sea::ripple);
        }
        timeline.at(SWALLOW, floating::remove);
        timeline.at(GONE, () -> {
            tentacles.forEach(KrakenTentacle::remove);
            stage.sound("game.neutral.swim", middle, 1.2f, SoundPitch.of(31));
        });
        timeline.lasts(END);
    }

    static double level(int tick) {
        double filled = Math.min(1, (tick + 1) / (double) FILL);
        return filled * Math.clamp((END - tick) / (double) DRAIN, 0, 1);
    }

    private static void burst(Stage stage, Vec base) {
        stage.breakEffect((int) Math.floor(base.x()), (int) Math.floor(base.y()), (int) Math.floor(base.z()), Material.BEDROCK, 0);
        stage.sound("random.splash", base.add(0, 0.125, 0), 1.6f, SoundPitch.of(44));
        Vec hole = base.add(0, 0.2, 0);
        stage.particle("WATER_SPLASH", hole, 40, new Vec(0.3, 0.1, 0.3), 0.35);
        stage.particle("BLOCK_CRACK", hole, 20, new Vec(0.3, 0.3, 0.3), 0.15, Kit.blockId(Material.BEDROCK, 0));
    }

    private static void spray(Stage stage, Vec middle, int splashes) {
        Vec spray = middle.add(0, SPRAY, 0);
        stage.particle("ITEM_CRACK", spray, 40, new Vec(0.6, 0.6, 0.6), 0.3, INK, 0);
        stage.particle("SPELL_MOB", spray, 30, new Vec(1.5, 0.6, 1.5), 0);
        stage.particle("WATER_SPLASH", spray, splashes, new Vec(1.5, 0.2, 1.5), 0.4);
    }

    private static void squeeze(Stage stage, Vec voice, Vec head, Vec foot) {
        stage.sound("random.splash", voice, 2, SoundPitch.of(37));
        stage.sound("mob.zombie.woodbreak", voice, 0.8f, SoundPitch.of(44));
        for (Vec half : List.of(head, foot)) {
            Vec at = half.add(0, CRUSH, 0);
            stage.particle("WATER_SPLASH", at, 30, new Vec(0.4, 0.1, 0.4), 0.3);
            stage.particle("BLOCK_CRACK", at, 12, new Vec(0.35, 0.35, 0.35), 0.12, Kit.blockId(Material.BEDROCK, 0));
            stage.particle("ITEM_CRACK", at, 10, new Vec(0.3, 0.3, 0.3), 0.2, INK, 0);
        }
    }

    record Hole(int rise, double ahead, double right, boolean head, double phase) {
    }
}
