package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PaintFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void theBlobsLeaveGreenYellowBlueThenRed() {
        assertThat(PaintFinalKill.ORDER).extracting(PaintAnimation.Paint::wool).containsExactly(5, 4, 11, 14);
    }

    @Test
    void theFirstBlobArcsAndLandsBackOnTheGroundSixteenTicksLater() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);

        List<Vec> path = PaintFinalKill.flight(stage, victim, PaintFinalKill.launchVelocity(0, 0));

        assertThat(path).hasSize(16);
        assertThat(path.getLast().y()).isCloseTo(27, offset(1e-9));
        assertThat(path.stream().mapToDouble(Vec::y).max().orElseThrow()).isBetween(28.5, 30.0);
    }

    @Test
    void aBlobFallingIntoTheVoidStopsAtTheFlightLimit() {
        RecordingStage stage = new RecordingStage(block -> false);

        assertThat(PaintFinalKill.flight(stage, victim, PaintFinalKill.launchVelocity(3, 0))).hasSize(PaintFinalKill.FLIGHT_LIMIT);
    }

    @Test
    void thePaintSplashesAroundTheVictimWithoutPaintingTheFloorAndCleansUp() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();

        new PaintFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(3));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.blocks).isEmpty();
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("ArmorStand")).hasSize(4);
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Firework")).hasSize(5);
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:STICK")).hasSize(4);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(stage.sounds).contains("random.splash", "fireworks.twinkle");
        assertThat(timeline.length()).isEqualTo(PaintFinalKill.END);
    }
}
