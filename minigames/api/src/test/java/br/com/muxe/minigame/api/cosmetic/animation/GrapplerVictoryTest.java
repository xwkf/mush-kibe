package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Click;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GrapplerVictoryTest {

    private final UUID winner = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @Test
    void theHookLandsOnTheFloorUnderTheLookDirection() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);

        Vec hit = GrapplerVictory.trace(stage, new Vec(0.5, 40, 0.5), new Vec(0, -1, 0)).orElseThrow();

        assertThat(hit.y()).isCloseTo(27, offset(0.01));
        assertThat(hit.x()).isCloseTo(0.5, offset(1e-9));
        assertThat(stage.solid((int) Math.floor(hit.x()), (int) Math.floor(hit.y()), (int) Math.floor(hit.z()))).isTrue();
    }

    @Test
    void theHookLandsOnTheFirstBlockOfAWallAndNotOnTheOnesBehindIt() {
        RecordingStage stage = new RecordingStage(block -> block[0] == 20 || block[0] >= 30);

        Vec hit = GrapplerVictory.trace(stage, new Vec(0.5, 30, 0.5), new Vec(1, 0, 0)).orElseThrow();

        assertThat(hit.x()).isCloseTo(20, offset(0.01));
    }

    @Test
    void aSlantedLookStopsAtTheFloorBeforeReachingTheWall() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27 || block[0] >= 10);

        Vec hit = GrapplerVictory.trace(stage, new Vec(0.5, 30.5, 0.5), new Vec(1, -1, 0)).orElseThrow();

        assertThat(hit.y()).isCloseTo(27, offset(0.01));
        assertThat(hit.x()).isCloseTo(4, offset(0.02));
    }

    @Test
    void theHookReachesFarAcrossTheMapButNotPastEightyBlocks() {
        assertThat(GrapplerVictory.trace(new RecordingStage(block -> block[2] >= 75), new Vec(0.5, 30, 0.5), new Vec(0, 0, 1))).isPresent();
        assertThat(GrapplerVictory.trace(new RecordingStage(block -> block[2] >= 90), new Vec(0.5, 30, 0.5), new Vec(0, 0, 1))).isEmpty();
        assertThat(GrapplerVictory.trace(new RecordingStage(block -> false), new Vec(0.5, 30, 0.5), new Vec(0, 1, 0))).isEmpty();
    }

    @Test
    void thePullPointsAtTheHookAndGrowsWithTheDistance() {
        Vec near = GrapplerVictory.pull(Vec.ZERO, new Vec(10, 0, 0));
        Vec far = GrapplerVictory.pull(Vec.ZERO, new Vec(0, 0, 15));

        assertThat(near.x()).isCloseTo(1.3 + 0.15 * 10, offset(1e-9));
        assertThat(near.z()).isZero();
        assertThat(far.z()).isCloseTo(1.3 + 0.15 * 15, offset(1e-9));
        assertThat(far.z()).isGreaterThan(near.x());
    }

    @Test
    void thePullNeverExceedsTheVelocityPacketLimitAndKeepsItsDirection() {
        Random random = new Random(4);
        for (int index = 0; index < 300; index++) {
            Vec hook = new Vec(random.nextDouble() * 400 - 200, random.nextDouble() * 400 - 200, random.nextDouble() * 400 - 200);
            Vec pull = GrapplerVictory.pull(Vec.ZERO, hook);

            assertThat(Math.abs(pull.x())).isLessThanOrEqualTo(3.9 + 1e-9);
            assertThat(Math.abs(pull.y())).isLessThanOrEqualTo(3.9 + 1e-9);
            assertThat(Math.abs(pull.z())).isLessThanOrEqualTo(3.9 + 1e-9);
            assertThat(Math.signum(pull.x())).isEqualTo(Math.signum(hook.x()));
            assertThat(Math.signum(pull.z())).isEqualTo(Math.signum(hook.z()));
            assertThat(pull.y()).isGreaterThanOrEqualTo(0.1);
        }
    }

    @Test
    void aHookBelowOnlyHopsAndAHookAboveLiftsExtra() {
        Vec below = GrapplerVictory.pull(Vec.ZERO, new Vec(0, -10, 0));
        Vec above = GrapplerVictory.pull(Vec.ZERO, new Vec(0, 10, 0));
        Vec farAbove = GrapplerVictory.pull(Vec.ZERO, new Vec(0, 300, 0));

        assertThat(below.y()).isEqualTo(0.1);
        assertThat(below.x()).isZero();
        assertThat(above.y()).isCloseTo(1.3 + 1.5 + 0.3, offset(1e-9));
        assertThat(farAbove.y()).isEqualTo(3.9);
    }

    @Test
    void everyRiderGetsAGrapplerAndNobodyElseDoes() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);

        new GrapplerVictory().script(scene(), stage, new Timeline(), new Random(1));

        assertThat(stage.tools).containsOnlyKeys(winner, other);
    }

    @Test
    void leftClickThrowsAHookAndRightClickPullsTowardsIt() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27 || block[2] >= 20);
        stage.eyes = new Vec(0.5, 28.62, 0.5);
        stage.look = new Vec(0, 0, 1);
        Timeline timeline = new Timeline();
        new GrapplerVictory().script(scene(), stage, timeline, new Random(1));

        stage.tools.get(winner).accept(Click.RIGHT);
        assertThat(stage.pushes).isEmpty();
        assertThat(stage.actors).isEmpty();

        stage.tools.get(winner).accept(Click.LEFT);
        assertThat(stage.alive()).isEqualTo(1);
        assertThat(stage.actors.getFirst().type).isEqualTo("FishingHook");
        assertThat(stage.actors.getFirst().position.z()).isCloseTo(20, offset(0.01));

        stage.tools.get(winner).accept(Click.RIGHT);
        assertThat(stage.pushes).hasSize(1);
        assertThat(stage.pushes.getFirst().z()).isCloseTo(3.9, offset(0.01));
        assertThat(stage.pushes.getFirst().x()).isZero();
        assertThat(stage.pushes.getFirst().y()).isGreaterThan(0.1);
        assertThat(stage.sounds).contains("step.gravel");

        stage.tools.get(winner).accept(Click.RIGHT);
        assertThat(stage.pushes).hasSize(2);
        assertThat(stage.alive()).isEqualTo(1);
    }

    @Test
    void aNewThrowReplacesThePreviousHook() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        stage.eyes = new Vec(0.5, 40, 0.5);
        stage.look = new Vec(0, -1, 0);
        new GrapplerVictory().script(scene(), stage, new Timeline(), new Random(1));

        stage.tools.get(winner).accept(Click.LEFT);
        stage.tools.get(winner).accept(Click.LEFT);

        assertThat(stage.actors).hasSize(2);
        assertThat(stage.actors.getFirst().removed()).isTrue();
        assertThat(stage.actors.getLast().removed()).isFalse();
        assertThat(stage.alive()).isEqualTo(1);
    }

    @Test
    void aThrowThatFindsNoBlockCreatesNoHookAndNoPull() {
        RecordingStage stage = new RecordingStage(block -> false);
        new GrapplerVictory().script(scene(), stage, new Timeline(), new Random(1));

        stage.tools.get(winner).accept(Click.LEFT);
        stage.tools.get(winner).accept(Click.RIGHT);

        assertThat(stage.actors).isEmpty();
        assertThat(stage.pushes).isEmpty();
    }

    @Test
    void theWholeEffectStartsWithABurstLaunchesEveryTenTicksAndCleansUp() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        stage.eyes = new Vec(0.5, 40, 0.5);
        stage.look = new Vec(0, -1, 0);
        Timeline timeline = new Timeline();
        new GrapplerVictory().script(scene(), stage, timeline, new Random(1));

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
            if (tick == 5) {
                stage.tools.get(winner).accept(Click.LEFT);
                stage.tools.get(other).accept(Click.LEFT);
                assertThat(stage.alive()).isEqualTo(2);
            }
        }
        stage.tools.get(winner).accept(Click.LEFT);

        assertThat(timeline.length()).isEqualTo(200);
        assertThat(stage.particles).containsEntry("VILLAGER_HAPPY", 1);
        assertThat(stage.sounds).contains("random.pop", "mob.horse.armor");
        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals).count()).isEqualTo(20);
        assertThat(stage.actors).hasSize(2).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(stage.alive()).isZero();
    }

    private Scene scene() {
        return new Scene(new Vec(0.5, 27, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(winner, other));
    }
}
