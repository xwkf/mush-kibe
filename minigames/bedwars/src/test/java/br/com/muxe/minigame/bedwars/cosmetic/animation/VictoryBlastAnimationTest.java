package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class VictoryBlastAnimationTest {

    private static final BedFrame BED = BedFrame.of(8, 27, -2, 8, -1);
    private static final Vec BREAKER = new Vec(8.5, 27, -5.5);
    private static final int FLOOR = 26;

    private final RecordingStage stage = new RecordingStage(block -> block[1] <= FLOOR);
    private final Timeline timeline = new Timeline();
    private final List<String> schedule = new ArrayList<>();

    @Test
    void theTrophiesAndSunflowersStayFiveSecondsThenEverythingIsGone() {
        runUntil(Integer.MAX_VALUE);

        assertThat(timeline.length()).isEqualTo(VictoryBlastAnimation.END);
        assertThat(stage.alive()).isZero();
        assertThat(stage.actors).noneMatch(actor -> actor.type.startsWith("FallingBlock"));
    }

    @Test
    void theBedShattersLikeGoldAndEverySunflowerPopsHigherThanTheLast() {
        runUntil(Integer.MAX_VALUE);

        List<String> expected = new ArrayList<>(List.of("0 dig.stone", "0 random.break"));
        for (int sunflower = 0; sunflower < VictoryBlastAnimation.SUNFLOWERS; sunflower++) {
            expected.add(sunflower + " random.pop");
        }
        assertThat(schedule).containsExactlyElementsOf(expected);
        assertThat(stage.pitches.subList(0, 2)).extracting(pitch -> (int) (pitch * 63)).containsExactly(94, 47);
        assertThat(stage.pitches.subList(2, stage.pitches.size())).extracting(pitch -> (int) (pitch * 63))
            .containsExactly(63, 66, 70, 74, 77, 81, 85, 88, 92, 96, 100, 103, 107, 111, 114, 118, 122);
        assertThat(stage.particles).containsEntry("BLOCK_DUST", VictoryBlastAnimation.SHARDS);
        assertThat(stage.actors).filteredOn(actor -> actor.type.startsWith("Item:")).hasSize(VictoryBlastAnimation.SUNFLOWERS)
            .allMatch(actor -> actor.type.equals("Item:DOUBLE_PLANT"));
    }

    @Test
    void tenTrophiesAreThrownAllAroundSpinningOnceAndLandOnTheFloor() {
        runUntil(VictoryBlastAnimation.END - 1);

        List<RecordingStage.FakeActor> trophies = stage.actors.stream().filter(actor -> actor.skulls.containsKey(Slot.HELMET)).toList();
        assertThat(trophies).hasSize(VictoryBlastAnimation.TROPHY_COUNT).allMatch(trophy -> trophy.style.equals(VictoryBlastAnimation.TROPHY_STAND));
        assertThat(trophies).extracting(trophy -> trophy.skulls.get(Slot.HELMET)).containsOnly(VictoryBlastAnimation.TROPHIES);
        for (int index = 0; index < trophies.size(); index++) {
            RecordingStage.FakeActor trophy = trophies.get(index);
            assertThat(trophy.position.y()).isCloseTo(FLOOR + 1 - VictoryBlastAnimation.HEAD_BASE, offset(1.0E-9));
            Vec away = trophy.position.subtract(BED.middle());
            assertThat(Math.hypot(away.x(), away.z())).isBetween(2.0, 7.0);
            double angle = Math.toDegrees(Math.atan2(dot(away, BED.right()), dot(away, BED.forward())));
            double expected = VictoryBlastAnimation.FIRST_ANGLE + 36.0 * index;
            double difference = ((angle - expected) % 360 + 540) % 360 - 180;
            assertThat(Math.abs(difference)).isLessThanOrEqualTo(VictoryBlastAnimation.TROPHY_JITTER + 1.0E-6);
            assertThat(trophy.yaw).isCloseTo(BED.yaw() - 360, offset(1.0E-3f));
        }
    }

    @Test
    void aTrophySpinsOneTurnSlowingDown() {
        assertThat(VictoryBlastAnimation.spin(0)).isZero();
        assertThat(VictoryBlastAnimation.spin(1)).isCloseTo(-63.37f, offset(0.01f));
        assertThat(VictoryBlastAnimation.spin(8)).isCloseTo(-315, offset(0.01f));
        assertThat(VictoryBlastAnimation.spin(VictoryBlastAnimation.SPIN)).isEqualTo(-360);
        assertThat(VictoryBlastAnimation.spin(40)).isEqualTo(-360);
    }

    private static double dot(Vec first, Vec second) {
        return first.x() * second.x() + first.y() * second.y() + first.z() * second.z();
    }

    private void runUntil(int last) {
        new VictoryBlastAnimation().script(BED, BREAKER, stage, timeline, new Random(37));
        for (int tick = 0; tick <= Math.min(last, timeline.length()); tick++) {
            int before = stage.sounds.size();
            timeline.run(tick);
            for (String sound : stage.sounds.subList(before, stage.sounds.size())) {
                schedule.add(tick + " " + sound);
            }
        }
    }
}
