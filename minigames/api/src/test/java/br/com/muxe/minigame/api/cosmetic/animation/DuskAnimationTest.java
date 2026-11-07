package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DuskAnimationTest {

    private static final BedFrame BED = BedFrame.of(0, 24, 2, 1, 2);
    private static final Vec BREAKER = new Vec(-3.5, 24, 2.5);

    private final RecordingStage stage = new RecordingStage(block -> block[1] <= 23);
    private final Timeline timeline = new Timeline();
    private final List<String> schedule = new ArrayList<>();

    @Test
    void theSkyFlashesToNightWhileTheMoonShines() {
        runUntil(DuskAnimation.SET - 1);

        assertThat(stage.times).containsExactly(Nightfall.NIGHT);
    }

    @Test
    void theDayComesBackWhenTheMoonScatters() {
        runUntil(DuskAnimation.SET);

        assertThat(stage.times).containsExactly(Nightfall.NIGHT, Nightfall.MIDDAY);
    }

    @Test
    void theMoonSetsAboutTwoSecondsAfterTheBedBreaksAndNothingIsLeft() {
        runUntil(Integer.MAX_VALUE);

        assertThat(timeline.length()).isEqualTo(DuskAnimation.SET).isEqualTo(37);
        assertThat(stage.alive()).isZero();
    }

    @Test
    void theSoundsFollowTheRecordedSchedule() {
        runUntil(Integer.MAX_VALUE);

        assertThat(schedule).containsExactly(
            "0 ambient.weather.thunder", "0 fireworks.twinkle", "0 mob.bat.idle", "0 mob.enderdragon.wings",
            "2 mob.enderdragon.wings", "5 mob.enderdragon.wings",
            "8 mob.enderdragon.wings", "8 mob.bat.takeoff", "8 mob.bat.idle",
            "11 mob.enderdragon.wings", "15 mob.enderdragon.wings", "16 mob.enderdragon.wings",
            "19 mob.bat.idle", "19 mob.enderdragon.wings", "20 mob.enderdragon.wings",
            "25 mob.chicken.plop", "25 random.fizz",
            "37 fireworks.twinkle", "37 random.orb");
        assertThat(stage.pitches).extracting(pitch -> (int) (pitch * 63)).containsExactly(
            63, 75, 37, 113, 113, 113, 113, 50, 37, 113, 113, 113, 31, 75, 113, 50, 94, 94, 126);
    }

    @Test
    void theOwlSwoopsInFromTheLeftAndPerchesAboveTheBed() {
        runUntil(DuskAnimation.CARRY_FROM - 1);

        RecordingStage.FakeActor owl = owl();
        assertThat(owl.style).isEqualTo(DuskAnimation.OWL_STAND);
        assertThat(owl.position.distance(BED.at(0.5, -0.906, 0))).isLessThan(1.0E-9);
        assertThat(DuskAnimation.owl(BED, 0).z()).isLessThan(BED.foot().z() - 5);
        assertThat(stage.actors.getFirst().position).isEqualTo(BED.foot());
    }

    @Test
    void theOwlCarriesTheBedAndTheLanternAwayUpAndToTheRight() {
        runUntil(DuskAnimation.LEAVE - 1);

        Vec bedFoot = stage.actors.getFirst().position;
        assertThat(bedFoot.y() - BED.foot().y()).isCloseTo(10.844, offset(1.0E-9));
        assertThat(bedFoot.subtract(BED.foot()).z()).isGreaterThan(20);
        assertThat(owl().position.subtract(bedFoot).distance(new Vec(0.5, -0.906, 0))).isLessThan(1.0E-9);
        assertThat(stage.actors.get(4).position.distance(HeadLantern.at(BED, bedFoot.subtract(BED.foot())))).isLessThan(1.0E-9);
    }

    @Test
    void theMoonIsACrescentAboveTheBedThatScattersWhenItSets() {
        Vec moon = DuskAnimation.moonCenter(BED);
        Random random = new Random(3);
        for (int dot = 0; dot < 500; dot++) {
            Vec point = DuskAnimation.crescent(BED, moon, random);
            double right = point.subtract(moon).z();
            double up = point.subtract(moon).y();
            assertThat(Math.hypot(right, up)).isLessThanOrEqualTo(DuskAnimation.MOON_RADIUS);
            assertThat(Math.hypot(right - DuskAnimation.SHADOW_SHIFT, up)).isGreaterThanOrEqualTo(DuskAnimation.SHADOW_RADIUS);
            assertThat(point.x()).isEqualTo(BED.middle().x());
        }
        assertThat(DuskAnimation.lit(-1.2, 0)).isTrue();
        assertThat(DuskAnimation.lit(0, 0)).isFalse();

        runUntil(Integer.MAX_VALUE);
        int dust = DuskAnimation.SET * DuskAnimation.MOON_DOTS + DuskAnimation.MOON_FADE;
        int feathers = (DuskAnimation.LEAVE - DuskAnimation.FEATHER_FROM + 1) * DuskAnimation.FEATHERS_PER_TICK;
        assertThat(stage.particles.get("REDSTONE")).isEqualTo(dust + feathers);
        assertThat(stage.particles.get("FIREWORKS_SPARK")).isEqualTo(DuskAnimation.SET * DuskAnimation.MOON_SPARKS + 1);
        assertThat(stage.particles).containsEntry("CLOUD", 1).containsEntry("EXPLOSION_NORMAL", 1);
    }

    private RecordingStage.FakeActor owl() {
        return stage.actors.stream().filter(actor -> DuskAnimation.OWL.equals(actor.skulls.get(Slot.HELMET))).findFirst().orElseThrow();
    }

    private void runUntil(int last) {
        new DuskAnimation().script(BED, BREAKER, stage, timeline, new Random(11));
        for (int tick = 0; tick <= Math.min(last, timeline.length()); tick++) {
            int before = stage.sounds.size();
            timeline.run(tick);
            for (String sound : stage.sounds.subList(before, stage.sounds.size())) {
                schedule.add(tick + " " + sound);
            }
        }
    }
}
