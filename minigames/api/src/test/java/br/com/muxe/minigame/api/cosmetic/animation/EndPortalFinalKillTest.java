package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class EndPortalFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void theWitchRingWidensToAboutOneAndAHalfBlocks() {
        List<Vec> first = EndPortalFinalKill.ring(victim, 0);
        List<Vec> last = EndPortalFinalKill.ring(victim, EndPortalFinalKill.RINGS - 1);

        assertThat(first).hasSize(15).allSatisfy(point -> assertThat(point.distance(victim.add(0, 0.1, 0))).isCloseTo(0, offset(1e-9)));
        assertThat(last).allSatisfy(point -> assertThat(point.distance(victim.add(0, 0.1, 0))).isCloseTo(1.45, offset(1e-9)));
    }

    @Test
    void twentyEyesAppearWhileTheRingGrows() {
        List<Integer> ticks = IntStream.range(0, EndPortalFinalKill.EYES).map(EndPortalFinalKill::eyeTick).boxed().toList();

        assertThat(ticks).isSorted().first().isEqualTo(0);
        assertThat(ticks.getLast()).isLessThan(EndPortalFinalKill.RINGS);
    }

    @Test
    void eyesOfEnderFloatAroundTheVictimWithPortalShattersThenDisappear() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new EndPortalFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(6));

        for (int tick = 0; tick < EndPortalFinalKill.END; tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).hasSize(20).allSatisfy(actor -> {
            assertThat(actor.type).isEqualTo("Item:EYE_OF_ENDER");
            assertThat(Math.abs(actor.position.x() - victim.x())).isLessThanOrEqualTo(1.5);
            assertThat(actor.position.y() - victim.y()).isBetween(0.0, 1.8);
        });
        assertThat(stage.particles).containsEntry("SPELL_WITCH", 30 * 15).containsEntry("ITEM_CRACK", 4 * 8).containsEntry("PORTAL", 4 * 80);
        assertThat(stage.sounds.stream().filter(sound -> sound.equals("mob.endermen.portal"))).hasSize(20);
        assertThat(stage.sounds).contains("mob.endermen.scream", "mob.endermen.idle");
        assertThat(stage.alive()).isEqualTo(20);

        timeline.run(EndPortalFinalKill.END);

        assertThat(stage.alive()).isZero();
    }
}
