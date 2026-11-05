package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MagicalGirlAnimationTest {

    private static final BedFrame BED = BedFrame.of(8, 33, -6, 8, -5);
    private static final Vec BREAKER = new Vec(8.5, 33, -9.5);
    private static final int FLOOR = 32;

    private TracedStage play(int last) {
        return new TracedStage(block -> block[1] <= FLOOR).play(new MagicalGirlAnimation(), BED, BREAKER, 11, last);
    }

    @Test
    void aPinkHeartIsTracedFromItsTipEveryOtherTickUntilItIsWhole() {
        TracedStage stage = play(MagicalGirlAnimation.LAUNCH);
        List<TracedStage.Call> heart = stage.particles("REDSTONE");

        assertThat(heart).allSatisfy(dot -> assertThat(dot.spread()).isEqualTo(new Vec(1, 0.71, 0.77)));
        assertThat(heart.stream().map(TracedStage.Call::tick).distinct()).containsExactly(0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30);
        assertThat(heart.stream().filter(dot -> dot.tick() == 0)).hasSize(9);
        assertThat(heart.stream().filter(dot -> dot.tick() == 2)).hasSize(17);
        assertThat(heart.stream().filter(dot -> dot.tick() == 28)).hasSize(120);
        assertThat(heart.stream().filter(dot -> dot.tick() == 30)).hasSize(120);
        assertThat(stage.particles("FIREWORKS_SPARK")).isNotEmpty().allMatch(spark -> spark.tick() <= 30);
        assertThat(stage.schedule()).startsWith("0 random.orb", "9 random.orb", "20 random.orb");
    }

    @Test
    void theHeartStandsOverTheBedFacingTheBreaker() {
        Vec across = MagicalGirlAnimation.across(BED, BREAKER);
        Vec tip = MagicalGirlAnimation.heartPoint(BED.middle(), across, Math.PI);

        assertThat(across).isEqualTo(new Vec(1, 0, 0));
        assertThat(tip.y() - BED.middle().y()).isCloseTo(0.78, offset(1.0E-9));
        assertThat(tip.x()).isCloseTo(BED.middle().x(), offset(1.0E-9));
        double widest = 0;
        double highest = 0;
        for (int step = 0; step < 120; step++) {
            Vec point = MagicalGirlAnimation.heartPoint(BED.middle(), across, step * Math.PI / 60);
            widest = Math.max(widest, Math.abs(point.x() - BED.middle().x()));
            highest = Math.max(highest, point.y() - BED.middle().y());
            assertThat(point.z()).isCloseTo(BED.middle().z(), offset(1.0E-9));
        }
        assertThat(widest).isCloseTo(1.6, offset(0.01));
        assertThat(highest).isCloseTo(3.66, offset(0.05));
    }

    @Test
    void aPinkRocketRisesFromTheHeartAndBurstsBeforeTheStarsFall() {
        TracedStage stage = play(MagicalGirlAnimation.BURST + 2);

        assertThat(stage.recording.actors).filteredOn(actor -> actor.type.equals("Firework")).singleElement().satisfies(rocket -> {
            assertThat(rocket.position).isEqualTo(BED.middle().add(0, 2.5, 0));
            assertThat(rocket.removed).isTrue();
        });
        assertThat(stage.calls("firework")).singleElement().satisfies(call -> assertThat(call.tick()).isEqualTo(MagicalGirlAnimation.LAUNCH));
        assertThat(stage.sounds("fireworks.launch")).extracting(TracedStage.Call::tick).containsExactly(31, 31);
    }

    @Test
    void eightRainbowStarsFallSpinningOntoTheBedAndFizzleOut() {
        TracedStage stage = play(Integer.MAX_VALUE);
        List<RecordingStage.FakeActor> stars = stage.recording.actors.stream()
            .filter(actor -> MagicalGirlAnimation.STAR.equals(actor.skulls.get(Slot.HELMET))).toList();

        assertThat(stars).hasSize(8).allMatch(star -> star.removed);
        assertThat(stage.sounds("note.pling")).extracting(TracedStage.Call::tick).containsExactly(48, 50, 51, 53, 56, 57, 58, 61);
        assertThat(stage.sounds("note.pling")).extracting(TracedStage.Call::pitchByte).containsExactly(126, 119, 113, 107, 100, 94, 88, 81);
        assertThat(stage.sounds("mob.chicken.plop")).extracting(TracedStage.Call::tick).containsExactly(61, 63, 64, 66, 69, 70, 71, 74);
        assertThat(stage.sounds("random.fizz")).extracting(TracedStage.Call::tick).containsExactly(65, 67, 68, 70, 73, 74, 75, 78);
        assertThat(stage.particles("REDSTONE").stream().filter(dot -> dot.tick() >= 48).map(TracedStage.Call::spread).distinct())
            .containsExactly(new Vec(1, 0, 0), new Vec(1, 0.5, 0), new Vec(1, 1, 0), new Vec(0.02, 1, 0), new Vec(0.02, 0, 1),
                new Vec(0.29, 0, 0.51), new Vec(0.58, 0, 0.83));
        assertThat(stage.timeline.length()).isEqualTo(78);
        assertThat(stage.recording.alive()).isZero();
    }

    @Test
    void aStarStartsHighAroundTheBedAndLandsWithItsHeadOnTheFloorNearTheMiddle() {
        MagicalGirlAnimation.FallingStar star = new MagicalGirlAnimation.FallingStar(BED, new RecordingStage(block -> false), new Random(5));
        Vec middle = BED.middle();

        double reach = Math.hypot(star.start().x() - middle.x(), star.start().z() - middle.z());
        assertThat(reach).isBetween(4.5, 6.8);
        assertThat(star.start().y() - middle.y()).isBetween(5.8, 7.5);
        assertThat(Math.hypot(star.landing().x() - middle.x(), star.landing().z() - middle.z())).isLessThanOrEqualTo(1.5);
        assertThat(star.position(0)).isEqualTo(star.start());
        assertThat(star.position(13).y() - middle.y()).isCloseTo(-1.44, offset(1.0E-9));
        assertThat(star.position(15).y() - middle.y()).isCloseTo(-1.22, offset(1.0E-9));
        assertThat(star.position(13).x()).isCloseTo(star.landing().x(), offset(1.0E-9));
        assertThat(star.position(12).y()).isGreaterThan(star.position(13).y());
    }
}
