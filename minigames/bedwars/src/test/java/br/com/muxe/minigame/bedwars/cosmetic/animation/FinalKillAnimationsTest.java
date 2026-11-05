package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class FinalKillAnimationsTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void theFrozenHeadFliesAwayFromTheHitterAndUp() {
        Vec away = new Vec(0, 0, -1);

        Vec end = BrainFreezeFinalKill.flight(away, BrainFreezeFinalKill.FLIGHT);

        assertThat(end.z()).isCloseTo(-5.6, offset(1e-9));
        assertThat(end.y()).isCloseTo(7, offset(1e-9));
        assertThat(end.x()).isZero();
    }

    @Test
    void theFrozenHeadWearsTheVictimSkinAndExplodesAtTheEnd() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new BrainFreezeFinalKill().script(new Scene(victim, new Vec(1, 0, 0), Optional.of("skin")), stage, timeline, new Random(1));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).hasSize(2);
        assertThat(stage.particles).containsKeys("BLOCK_CRACK", "EXPLOSION_HUGE");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }

    @Test
    void theBirdsCircleCloseThenWidenAndRise() {
        assertThat(BirdsFinalKill.radius(0)).isEqualTo(1);
        assertThat(BirdsFinalKill.radius(BirdsFinalKill.CIRCLING)).isEqualTo(1);
        assertThat(BirdsFinalKill.radius(BirdsFinalKill.CIRCLING + BirdsFinalKill.WIDENING)).isEqualTo(4);
        assertThat(BirdsFinalKill.rise(BirdsFinalKill.POP)).isGreaterThan(BirdsFinalKill.rise(BirdsFinalKill.CIRCLING) + 2);
        Vec start = BirdsFinalKill.position(victim, 0, 0, 0);
        assertThat(start.distance(victim)).isCloseTo(1, offset(1e-9));
    }

    @Test
    void theBigGiftRisesFiveBlocksBeforeItBursts() {
        Vec start = new Vec(0, -1.25, 0);

        assertThat(GiftedFinalKill.rising(start, GiftedFinalKill.RISE_TICKS).y()).isCloseTo(3.69, offset(1e-9));
        assertThat(GiftedFinalKill.GIFTS).hasSize(8).doesNotHaveDuplicates().doesNotContain(GiftedFinalKill.BIG_GIFT);
    }

    @Test
    void everyFinalKillEffectRunsAnywhereAndCleansUp() {
        for (var animation : SceneAnimations.finalKills().all()) {
            RecordingStage stage = new RecordingStage(block -> block[1] < 27);
            Timeline timeline = new Timeline();
            animation.script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(3));
            for (int tick = 0; tick <= timeline.length(); tick++) {
                timeline.run(tick);
            }
            assertThat(stage.actors.isEmpty() && stage.particles.isEmpty()).as(animation.id() + " shows something").isFalse();
            assertThat(stage.actors).as(animation.id()).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        }
    }
}
