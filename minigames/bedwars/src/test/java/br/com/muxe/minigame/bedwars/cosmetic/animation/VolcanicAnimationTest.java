package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class VolcanicAnimationTest {

    private static final BedFrame BED = BedFrame.of(6, 11, 17, 6, 16);
    private static final Vec BREAKER = new Vec(6.5, 11, 21.5);
    private static final double EPSILON = 1.0E-9;

    private TracedStage play(int last) {
        return new TracedStage(block -> block[1] <= 10).play(new VolcanicAnimation(), BED, BREAKER, 8, last);
    }

    @Test
    void theEruptionThrowsTheBedFourAndAHalfBlocksUpAndKeepsItBobbingOnTheLava() {
        double[] heights = VolcanicAnimation.heights(new Random(3));

        assertThat(Arrays.copyOf(heights, 5)).containsExactly(1.91, 3.34, 4.19, 4.47, 4.50);
        assertThat(heights).hasSize(VolcanicAnimation.END);
        assertThat(Arrays.stream(heights).skip(5)).allSatisfy(height -> assertThat(height).isBetween(4.1, 5.8));
        assertThat(heights[8]).isBetween(4.1, 5.8);
    }

    @Test
    void theBedAndItsLanternRideTheEruption() {
        TracedStage stage = play(4);
        RecordingStage.FakeActor footStand = stage.recording.actors.getFirst();
        RecordingStage.FakeActor lantern = stage.recording.actors.stream()
            .filter(actor -> actor.equipment.get(Slot.HELMET) == Material.JACK_O_LANTERN).findFirst().orElseThrow();

        assertThat(footStand.position.y() - BED.foot().y()).isCloseTo(4.5, offset(EPSILON));
        assertThat(lantern.position.y() - BED.head().y()).isCloseTo(4.5 - 0.16, offset(EPSILON));
        assertThat(stage.particles("EXPLOSION_HUGE")).hasSize(1);
        assertThat(stage.sounds("random.explode")).singleElement().satisfies(blast -> assertThat(blast.pitchByte()).isEqualTo(126));
    }

    @Test
    void flamesJetUpwardsFromTheBedEveryTickUntilTheLavaCoolsDown() {
        TracedStage stage = play(Integer.MAX_VALUE);
        List<TracedStage.Call> flames = stage.particles("FLAME");

        assertThat(flames).hasSize(VolcanicAnimation.FLAMES * (VolcanicAnimation.END + 1)).allSatisfy(flame -> {
            assertThat(flame.count()).isZero();
            assertThat(flame.spread().y()).isBetween(0.26, 0.73);
            assertThat(Math.abs(flame.at().x() - BED.middle().x())).isLessThanOrEqualTo(0.1);
            assertThat(flame.at().y()).isEqualTo(BED.middle().y());
        });
        assertThat(stage.particles("LAVA")).hasSize(VolcanicAnimation.END + 1);
        assertThat(stage.sounds("liquid.lavapop").size()).isBetween(VolcanicAnimation.END + 1, 2 * (VolcanicAnimation.END + 1));
        assertThat(stage.sounds("random.fizz")).extracting(TracedStage.Call::tick).containsExactly(77, 79, 82);
        assertThat(stage.sounds("random.fizz")).extracting(TracedStage.Call::pitchByte).containsExactly(70, 118, 124);
        assertThat(stage.recording.alive()).isZero();
        assertThat(stage.timeline.length()).isEqualTo(VolcanicAnimation.END);
    }
}
