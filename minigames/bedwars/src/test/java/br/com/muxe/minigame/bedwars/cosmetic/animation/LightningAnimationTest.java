package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import org.junit.jupiter.api.Test;

class LightningAnimationTest {

    private static final BedFrame BED = BedFrame.of(8, 33, -6, 8, -5);
    private static final Vec BREAKER = new Vec(8.5, 33, -9.5);

    private final TracedStage stage = new TracedStage(block -> block[1] <= 32).play(new LightningAnimation(), BED, BREAKER, 7);

    @Test
    void threeBoltsStrikeTheHeadOfTheBedOnTheRecordedTicks() {
        List<TracedStage.Call> bolts = stage.calls("lightning");

        assertThat(bolts).extracting(TracedStage.Call::tick).containsExactly(0, 21, 31);
        assertThat(bolts).extracting(call -> (Vec) call.args().getFirst()).containsOnly(new Vec(8.5, 33, -4.5));
        assertThat(stage.sounds("random.explode")).extracting(TracedStage.Call::pitchByte).containsExactly(32, 39, 35);
        assertThat(stage.timeline.length()).isEqualTo(31);
    }

    @Test
    void theBreakBlowsAHugeExplosionAndARingOfFlamesOutOfTheBed() {
        List<TracedStage.Call> flames = stage.particles("FLAME");

        assertThat(stage.particles("EXPLOSION_HUGE")).singleElement().satisfies(call -> {
            assertThat(call.tick()).isZero();
            assertThat(call.at()).isEqualTo(BED.middle());
        });
        assertThat(flames).hasSize(LightningAnimation.FLAMES).allSatisfy(flame -> {
            assertThat(flame.tick()).isZero();
            assertThat(flame.count()).isZero();
            assertThat(flame.at()).isEqualTo(BED.middle().add(0, 0.5, 0));
            assertThat(Math.hypot(flame.spread().x(), flame.spread().z())).isBetween(0.24, 0.5);
            assertThat(flame.spread().y()).isBetween(0.0, 0.05);
        });
    }

    @Test
    void nothingIsLeftBehind() {
        assertThat(stage.recording.actors).isEmpty();
        assertThat(stage.recording.blocks).isEmpty();
    }
}
