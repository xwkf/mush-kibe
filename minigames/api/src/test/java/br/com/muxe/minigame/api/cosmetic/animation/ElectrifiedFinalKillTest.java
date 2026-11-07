package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage.FakeActor;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ElectrifiedFinalKillTest {

    private static final Vec VICTIM = new Vec(6.5, 11, 17.5);
    private static final SignedSkin SKIN = new SignedSkin("value", "signature");

    @Test
    void theVictimBodyWearsItsSignedSkinWithAHiddenSkeletonInside() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 11);

        new ElectrifiedFinalKill().script(scene().withBody(Optional.of(SKIN)), stage, new Timeline(), new Random(1));

        assertThat(stage.skins).containsExactly(SKIN);
        assertThat(stage.actors).extracting(actor -> actor.type).containsExactly("Player", "Skeleton");
        assertThat(skeleton(stage).watched).containsEntry(VictimBody.FLAGS, VictimBody.INVISIBLE);
    }

    @Test
    void theShockFlickersBetweenBodyAndSkeletonAndEndsOnTheBones() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 11);
        Timeline timeline = new Timeline();
        new ElectrifiedFinalKill().script(scene(), stage, timeline, new Random(1));

        run(timeline, 0, ElectrifiedFinalKill.FIRST_FLICKER);
        assertThat(body(stage).watched).containsEntry(VictimBody.FLAGS, VictimBody.INVISIBLE);
        assertThat(skeleton(stage).watched).containsEntry(VictimBody.FLAGS, VictimBody.VISIBLE);
        run(timeline, ElectrifiedFinalKill.FIRST_FLICKER + 1, ElectrifiedFinalKill.FIRST_FLICKER + 1);
        assertThat(body(stage).watched).containsEntry(VictimBody.FLAGS, VictimBody.VISIBLE);
        run(timeline, ElectrifiedFinalKill.FIRST_FLICKER + 2, ElectrifiedFinalKill.DEATH);

        assertThat(skeleton(stage).watched).containsEntry(VictimBody.FLAGS, VictimBody.VISIBLE);
        assertThat(body(stage).watched).containsEntry(VictimBody.FLAGS, VictimBody.INVISIBLE);
        assertThat(stage.sounds.stream().filter("game.neutral.hurt"::equals)).hasSize(ElectrifiedFinalKill.LAST_FLICKER - ElectrifiedFinalKill.FIRST_FLICKER + 1);
        assertThat(stage.sounds).contains("mob.skeleton.death", "random.explode").doesNotContain("lightning");
    }

    @Test
    void silentBoltsStrikeAboveTheVictimAndEverythingIsCleanedUp() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 11);
        Timeline timeline = new Timeline();
        new ElectrifiedFinalKill().script(scene(), stage, timeline, new Random(1));

        run(timeline, 0, timeline.length());

        assertThat(stage.bolts).hasSize(ElectrifiedFinalKill.STRIKES.size()).allSatisfy(bolt -> assertThat(bolt.y()).isEqualTo(VICTIM.y() + ElectrifiedFinalKill.BOLT_ABOVE));
        assertThat(stage.particles).containsKeys("CRIT", "CRIT_MAGIC", "EXPLOSION_HUGE");
        assertThat(stage.skins).isEmpty();
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }

    private static Scene scene() {
        return Scene.at(VICTIM, new Vec(0, 0, -1));
    }

    private static FakeActor body(RecordingStage stage) {
        return stage.actors.getFirst();
    }

    private static FakeActor skeleton(RecordingStage stage) {
        return stage.actors.get(1);
    }

    private static void run(Timeline timeline, int from, int until) {
        for (int tick = from; tick <= until; tick++) {
            timeline.run(tick);
        }
    }
}
