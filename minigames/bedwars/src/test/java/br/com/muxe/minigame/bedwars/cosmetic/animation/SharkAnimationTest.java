package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage;
import br.com.muxe.minigame.api.cosmetic.animation.TracedStage;
import java.util.List;
import org.junit.jupiter.api.Test;

class SharkAnimationTest {

    private static final BedFrame BED = BedFrame.of(0, 24, 2, 1, 2);
    private static final Vec BREAKER = new Vec(-3.5, 24, 2.5);
    private static final int FLOOR = 23;
    private static final double EPSILON = 1.0E-9;

    private TracedStage play(int last) {
        return new TracedStage(block -> block[1] <= FLOOR).play(new SharkAnimation(), BED, BREAKER, 6, last);
    }

    private static List<RecordingStage.FakeActor> sharks(TracedStage stage) {
        return stage.recording.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).toList();
    }

    @Test
    void fourSharksWaitInAFanUnderTheBrokenBed() {
        TracedStage stage = new TracedStage(block -> block[1] <= FLOOR);
        stage.play(new SharkAnimation(), BED, BREAKER, 6, -1);
        List<RecordingStage.FakeActor> sharks = sharks(stage);

        assertThat(sharks).extracting(shark -> shark.skulls.get(Slot.HELMET))
            .containsExactly(SharkAnimation.SHARK, SharkAnimation.SHARK, SharkAnimation.SHARK, SharkAnimation.PALE_SHARK);
        assertThat(sharks).extracting(shark -> Math.round((shark.position.z() - BED.middle().z()) * 10) / 10.0).containsExactly(-1.2, -0.4, 0.4, 1.2);
        assertThat(sharks).allSatisfy(shark -> {
            assertThat(shark.position.x()).isEqualTo(BED.middle().x());
            assertThat(shark.position.y() - BED.middle().y()).isCloseTo(-0.94, offset(EPSILON));
        });
    }

    @Test
    void theyLeapTowardsTheBreakerAndDiveBackIntoTheFloor() {
        TracedStage stage = play(SharkAnimation.SWIM - 1);
        List<RecordingStage.FakeActor> sharks = sharks(stage);

        assertThat(sharks).allSatisfy(shark -> {
            assertThat(shark.position.y() - BED.middle().y()).isCloseTo(-0.69, offset(EPSILON));
            assertThat(BED.middle().x() - shark.position.x()).isBetween(2.4, 2.8);
            assertThat(shark.pose.head().x()).isEqualTo(10);
        });
        double peak = stage.calls("particle").stream().filter(call -> call.name().equals("WATER_SPLASH"))
            .mapToDouble(call -> call.at().y() - BED.middle().y()).max().orElseThrow();
        assertThat(peak).isBetween(6.5, 7.5);
        assertThat(SharkAnimation.pitch(new Vec(0.25, 1.19, 0))).isCloseTo(-78.1f, offset(0.1f));
    }

    @Test
    void thenTheySwimApartOnTheFloorAndVanishInPuffs() {
        TracedStage stage = play(Integer.MAX_VALUE);
        List<RecordingStage.FakeActor> sharks = sharks(stage);

        assertThat(sharks).allMatch(shark -> shark.removed);
        assertThat(sharks.getFirst().position.z() - BED.middle().z()).isLessThan(-3);
        assertThat(sharks.getLast().position.z() - BED.middle().z()).isGreaterThan(3);
        assertThat(sharks).allSatisfy(shark -> assertThat(BED.middle().x() - shark.position.x()).isBetween(7.0, 9.5));
        assertThat(stage.particles("CLOUD")).extracting(TracedStage.Call::tick).containsExactly(56, 56, 58, 58);
        assertThat(stage.sounds("mob.bat.hurt")).extracting(TracedStage.Call::tick).containsExactly(56, 56, 58, 58);
        assertThat(stage.sounds("fire.ignite")).hasSize(2 * (58 - 28 + 1));
        assertThat(stage.sounds("random.splash").getFirst().pitchByte()).isEqualTo(114);
        assertThat(stage.sounds("random.splash")).allSatisfy(splash -> assertThat(splash.tick()).isBetween(28, 55));
        assertThat(stage.recording.alive()).isZero();
        assertThat(stage.timeline.length()).isEqualTo(58);
    }

    @Test
    void sharksFollowTheGroundDownWhenTheIslandEnds() {
        TracedStage stage = new TracedStage(block -> block[1] <= FLOOR && block[0] > -4 || block[1] <= FLOOR - 3)
            .play(new SharkAnimation(), BED, BREAKER, 6);

        assertThat(sharks(stage)).allSatisfy(shark -> assertThat(shark.position.y() - BED.middle().y()).isCloseTo(-3.69, offset(EPSILON)));
    }

    @Test
    void aBlueFireworkPopsAsTheSharksBreakOut() {
        TracedStage stage = play(2);

        assertThat(stage.calls("firework")).singleElement().satisfies(rocket -> {
            assertThat(rocket.tick()).isZero();
            assertThat(rocket.args().getFirst()).isEqualTo(BED.middle());
        });
        assertThat(SharkAnimation.away(BED, BED.middle()).distance(new Vec(-1, 0, 0))).isZero();
    }
}
