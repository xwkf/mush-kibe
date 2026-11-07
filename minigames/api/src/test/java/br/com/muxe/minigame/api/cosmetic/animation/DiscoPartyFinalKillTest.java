package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DiscoPartyFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void theDiscoBallHangsOverTheVictimSpinningWhileTenRecordsFlyOut() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();

        new DiscoPartyFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(2));
        RecordingStage.FakeActor ball = stage.actors.getFirst();
        for (int tick = 0; tick <= 20; tick++) {
            timeline.run(tick);
        }

        assertThat(ball.skulls).containsEntry(Slot.HELMET, DiscoPartyFinalKill.DISCO_BALL);
        assertThat(ball.position).isEqualTo(victim.add(0, DiscoPartyFinalKill.BALL_ABOVE, 0));
        assertThat(ball.yaw).isCloseTo(DiscoPartyFinalKill.SPIN * 21, offset(1e-3f));
        for (int tick = 21; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:RECORD_3")).hasSize(DiscoPartyFinalKill.DISCS);
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Firework")).hasSize(2);
        assertThat(stage.particles).containsKeys("REDSTONE", "NOTE");
        assertThat(stage.sounds).contains("note.bd", "note.bass", "note.harp", "random.explode");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }

    @Test
    void theMelodyDropsThenSettles() {
        assertThat(DiscoPartyFinalKill.melody(0)).isEqualTo(DiscoPartyFinalKill.FIRST_PITCH);
        assertThat(DiscoPartyFinalKill.melody(11)).isEqualTo(DiscoPartyFinalKill.FIRST_PITCH);
        assertThat(DiscoPartyFinalKill.melody(12)).isEqualTo(DiscoPartyFinalKill.LOW_PITCH);
        assertThat(DiscoPartyFinalKill.melody(15)).isEqualTo(DiscoPartyFinalKill.LAST_PITCH);
        assertThat(DiscoPartyFinalKill.melody(21)).isEqualTo(DiscoPartyFinalKill.LAST_PITCH);
    }

    @Test
    void theRingNotesCircleTheVictimAtWaistHeight() {
        Vec first = DiscoPartyFinalKill.ringNote(victim, 0, 0);
        Vec later = DiscoPartyFinalKill.ringNote(victim, 0, 4);

        assertThat(Math.hypot(first.x() - victim.x(), first.z() - victim.z())).isCloseTo(1.2, offset(1e-9));
        assertThat(first.y()).isCloseTo(victim.y() + 1.06, offset(1e-9));
        assertThat(Math.toDegrees(Math.atan2(later.z() - victim.z(), later.x() - victim.x()))).isCloseTo(46, offset(1e-9));
    }
}
