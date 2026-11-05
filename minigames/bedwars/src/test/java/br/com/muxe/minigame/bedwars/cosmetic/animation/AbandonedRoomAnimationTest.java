package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class AbandonedRoomAnimationTest {

    private static final BedFrame BED = BedFrame.of(0, 24, 2, 1, 2);
    private static final Vec BREAKER = new Vec(-3.5, 24, 2.5);

    @Test
    void sixteenBatsBurstOutOfTheBedInACloudOfSmoke() {
        TracedStage stage = new TracedStage(block -> block[1] < 24).play(new AbandonedRoomAnimation(), BED, BREAKER, 4, 0);

        assertThat(stage.recording.actors).hasSize(AbandonedRoomAnimation.BATS).allSatisfy(bat -> {
            assertThat(bat.type).isEqualTo("Bat");
            assertThat(bat.position).isEqualTo(BED.middle());
        });
        assertThat(stage.particles("SMOKE_LARGE")).singleElement().satisfies(smoke -> {
            assertThat(smoke.count()).isEqualTo(60);
            assertThat(smoke.spread()).isEqualTo(new Vec(1, 1, 1));
        });
        assertThat(stage.sounds("mob.bat.takeoff")).hasSize(AbandonedRoomAnimation.BATS);
    }

    @Test
    void theBatsFlutterUpAndAwayUntilTheyAllVanishTogether() {
        TracedStage stage = new TracedStage(block -> block[1] < 24).play(new AbandonedRoomAnimation(), BED, BREAKER, 4, AbandonedRoomAnimation.LEAVE - 1);
        List<RecordingStage.FakeActor> bats = stage.recording.actors;

        double averageHeight = bats.stream().mapToDouble(bat -> bat.position.y() - BED.middle().y()).average().orElseThrow();
        assertThat(averageHeight).isBetween(3.0, 14.0);
        assertThat(bats).allMatch(bat -> !bat.removed);
        assertThat(stage.sounds("mob.bat.idle")).isNotEmpty().allSatisfy(squeak -> assertThat(squeak.pitch()).isBetween(0.78f, 0.97f));

        stage.timeline.run(AbandonedRoomAnimation.LEAVE);

        assertThat(bats).allMatch(bat -> bat.removed);
        assertThat(stage.timeline.length()).isEqualTo(AbandonedRoomAnimation.LEAVE);
    }

    @Test
    void aBatTakesOffUpwardsAndStaysAroundTheBed() {
        Vec home = new Vec(0, 0, 0);
        BatFlight flight = new BatFlight(home, 90);
        Random random = new Random(9);

        Vec first = flight.step(random);
        assertThat(first.y()).isGreaterThan(0.3);
        for (int tick = 0; tick < 200; tick++) {
            Vec at = flight.step(random);
            assertThat(Math.hypot(at.x(), at.z())).isLessThan(BatFlight.MAX_DISTANCE + 2);
            assertThat(at.y()).isBetween(0.0, BatFlight.MAX_ALTITUDE + 2);
        }
    }
}
