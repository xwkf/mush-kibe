package br.com.muxe.minigame.bedwars.cosmetic.animation;

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

class BalloonsAnimationTest {

    private static final BedFrame BED = BedFrame.of(18, 15, 8, 17, 8);
    private static final Vec BREAKER = new Vec(22.5, 15, 8.5);

    private final RecordingStage stage = new RecordingStage(block -> block[1] <= 14);
    private final Timeline timeline = new Timeline();
    private final List<String> schedule = new ArrayList<>();

    @Test
    void theBedFloatsAwayForAboutThreeSecondsWithOneLaunchSound() {
        runUntil(Integer.MAX_VALUE);

        assertThat(timeline.length()).isEqualTo(BalloonsAnimation.END);
        assertThat(stage.alive()).isZero();
        assertThat(schedule).containsExactly("0 fireworks.launch");
        assertThat(stage.particles).isEmpty();
    }

    @Test
    void twentyFiveBalloonsAreTiedToAnInvisibleKnotNextToTheBed() {
        runUntil(0);

        RecordingStage.FakeActor anchor = stage.actors.stream().filter(actor -> actor.type.equals("Chicken")).findFirst().orElseThrow();
        List<RecordingStage.FakeActor> chickens = stage.actors.stream().filter(actor -> actor.type.equals("Chicken") && actor != anchor).toList();
        List<RecordingStage.FakeActor> balloons = stage.actors.stream().filter(actor -> actor.skulls.containsKey(Slot.HELMET)).toList();
        assertThat(chickens).hasSize(BalloonsAnimation.BALLOONS).allMatch(chicken -> chicken.watched.get(-1) == anchor)
            .allMatch(chicken -> chicken.watched.get(0).equals(Hex.INVISIBLE));
        assertThat(anchor.watched.get(0)).isEqualTo(Hex.INVISIBLE);
        assertThat(balloons).hasSize(BalloonsAnimation.BALLOONS).allMatch(balloon -> balloon.style.equals(BalloonsVictory.BALLOON_STAND));
        assertThat(balloons).extracting(balloon -> balloon.skulls.get(Slot.HELMET)).containsAll(List.of(BalloonsVictory.SKINS));
        for (int index = 0; index < chickens.size(); index++) {
            Vec knot = chickens.get(index).position.subtract(anchor.position);
            assertThat(knot.x()).isBetween(BalloonsAnimation.MIN_X - BalloonsAnimation.SWAY, BalloonsAnimation.MAX_X + BalloonsAnimation.SWAY);
            assertThat(knot.y()).isBetween(BalloonsAnimation.MIN_Y - BalloonsAnimation.SWAY, BalloonsAnimation.MAX_Y + BalloonsAnimation.SWAY);
            assertThat(balloons.get(index).position.add(BalloonsVictory.CHICKEN_OFFSET).distance(chickens.get(index).position)).isLessThan(1.0E-9);
        }
    }

    @Test
    void theBedRisesFasterAndFasterUntilOneBlockATick() {
        assertThat(BalloonsAnimation.lift(1)).isCloseTo(0.15, offset(1.0E-9));
        assertThat(BalloonsAnimation.lift(18)).isCloseTo(1, offset(1.0E-9));
        assertThat(BalloonsAnimation.lift(30)).isEqualTo(1);
        assertThat(BalloonsAnimation.rise(16)).isCloseTo(8.4, offset(1.0E-9));

        runUntil(BalloonsAnimation.END - 1);
        assertThat(stage.actors.getFirst().position.y() - BED.foot().y()).isCloseTo(BalloonsAnimation.rise(BalloonsAnimation.END), offset(1.0E-9));
        assertThat(stage.actors.getFirst().position.y() - BED.foot().y()).isGreaterThan(45);
    }

    @Test
    void eachBalloonCirclesGentlyAroundItsSpot() {
        for (int tick = 0; tick < 70; tick++) {
            Vec sway = BalloonsAnimation.sway(tick, 1.3);
            assertThat(Math.hypot(sway.x(), sway.z())).isCloseTo(BalloonsAnimation.SWAY, offset(1.0E-9));
            assertThat(Math.abs(sway.y())).isLessThanOrEqualTo(BalloonsAnimation.SWAY);
        }
        Vec start = BalloonsAnimation.sway(0, 0);
        Vec turned = BalloonsAnimation.sway((int) Math.round(Math.PI / BalloonsAnimation.SWAY_SPEED), 0);
        assertThat(start.distance(turned)).isLessThan(0.05);
    }

    private void runUntil(int last) {
        new BalloonsAnimation().script(BED, BREAKER, stage, timeline, new Random(23));
        for (int tick = 0; tick <= Math.min(last, timeline.length()); tick++) {
            int before = stage.sounds.size();
            timeline.run(tick);
            for (String sound : stage.sounds.subList(before, stage.sounds.size())) {
                schedule.add(tick + " " + sound);
            }
        }
    }
}
