package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SixSevenFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void theDancerEasesIntoEachPoseAndSwapsArmsEverySixTicks() {
        assertThat(SixSevenFinalKill.pose(0).rightArm().x()).isCloseTo(-60 * 0.5904, offset(1e-9));
        assertThat(SixSevenFinalKill.pose(4)).isEqualTo(SixSevenFinalKill.SIX_UP);
        assertThat(SixSevenFinalKill.pose(10)).isEqualTo(SixSevenFinalKill.SEVEN_UP);
        assertThat(SixSevenFinalKill.pose(16)).isEqualTo(SixSevenFinalKill.SIX_UP);
        assertThat(SixSevenFinalKill.pose(34)).isEqualTo(SixSevenFinalKill.SEVEN_UP);
    }

    @Test
    void eachNumberRidesTheHandThatHoldsIt() {
        Vec toward = new Vec(0, 0, -1);

        Vec sixUp = SixSevenFinalKill.hand(victim, toward, 4, true);
        Vec sevenDown = SixSevenFinalKill.hand(victim, toward, 4, false);
        Vec sixDown = SixSevenFinalKill.hand(victim, toward, 10, true);

        assertThat(sixUp.y() - victim.y()).isCloseTo(SixSevenFinalKill.RAISED.up(), offset(1e-9));
        assertThat(sevenDown.y() - victim.y()).isCloseTo(SixSevenFinalKill.LOWERED.up(), offset(1e-9));
        assertThat(sixDown.y()).isLessThan(sixUp.y());
        assertThat(sixUp.z()).isLessThan(victim.z());
        assertThat(sixUp.x()).isGreaterThan(victim.x());
        assertThat(sevenDown.x()).isLessThan(victim.x());
    }

    @Test
    void theDancerShrinksTwiceBeforeItExplodes() {
        assertThat(SixSevenFinalKill.shrunk(20)).isFalse();
        assertThat(SixSevenFinalKill.shrunk(SixSevenFinalKill.FIRST_SHRINK)).isTrue();
        assertThat(SixSevenFinalKill.shrunk(SixSevenFinalKill.REGROW)).isFalse();
        assertThat(SixSevenFinalKill.shrunk(SixSevenFinalKill.SECOND_SHRINK)).isTrue();
    }

    @Test
    void theDancerWearsTheHeadOfThePlayerWhoDied() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new SixSevenFinalKill().script(new Scene(victim, new Vec(0, 0, 1), Optional.of("victim-skin"), List.of()), stage, timeline, new Random(3));
        timeline.run(0);

        assertThat(stage.actors.getFirst().skulls).containsEntry(Slot.HELMET, "victim-skin");
    }

    @Test
    void theDancerFacesTheKillerExplodesAndSprinklesNumbersThatAreGoneAtTheEnd() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new SixSevenFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(3));
        RecordingStage.FakeActor dancer = stage.actors.getFirst();

        for (int tick = 0; tick < SixSevenFinalKill.BLAST; tick++) {
            timeline.run(tick);
        }

        assertThat(dancer.skulls).containsEntry(Slot.HELMET, SixSevenFinalKill.DANCER);
        assertThat(dancer.style).isEqualTo(SixSevenFinalKill.DANCER_STYLE);
        assertThat(dancer.watched).containsEntry(SixSevenFinalKill.STAND_FLAGS, SixSevenFinalKill.SHRUNK);
        assertThat(stage.sounds).filteredOn("random.fizz"::equals).hasSize(3);

        for (int tick = SixSevenFinalKill.BLAST; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.sounds).contains("random.explode");
        assertThat(stage.actors).hasSize(3 + SixSevenFinalKill.FOUNTAIN).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(stage.actors.subList(3, stage.actors.size())).allSatisfy(label -> assertThat(label.position.y()).isCloseTo(victim.y(), offset(1e-9)));
    }
}
