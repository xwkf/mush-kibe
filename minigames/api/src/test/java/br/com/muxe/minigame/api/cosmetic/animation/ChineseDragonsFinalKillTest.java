package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ChineseDragonsFinalKillTest {

    private final Vec victim = new Vec(0.5, 24, 2.5);

    @Test
    void aFlightClimbsAlongItsYawAndStopsAfterItsSteps() {
        DragonFlight flight = new DragonFlight(Vec.ZERO, -90, 30);

        Vec step = flight.step();

        assertThat(step.length()).isCloseTo(DragonFlight.SPEED, offset(1e-9));
        assertThat(step.x()).isPositive();
        assertThat(step.z()).isCloseTo(0, offset(1e-9));
        assertThat(step.y()).isCloseTo(DragonFlight.SPEED / 2, offset(1e-9));
        assertThat(flight.stand(0)).isEqualTo(step);
        assertThat(flight.stand(DragonFlight.STEPS + 10)).isEqualTo(flight.stand(DragonFlight.STEPS - 1));
    }

    @Test
    void theTrailPuffsUpwardFirstThenFollowsBehindTheHead() {
        DragonFlight flight = new DragonFlight(Vec.ZERO, 0, 10);
        Vec head = new Vec(0, DragonFlight.HEAD_ABOVE_STAND, 0);

        assertThat(flight.trail(0)).isEqualTo(flight.stand(0).add(head));
        assertThat(flight.trail(3).y()).isCloseTo(flight.stand(0).y() + 4 * DragonFlight.HEAD_ABOVE_STAND, offset(1e-9));
        assertThat(flight.trail(4)).isEqualTo(flight.stand(1).add(head));
        assertThat(flight.trail(ChineseDragonsFinalKill.END)).isEqualTo(flight.stand(DragonFlight.STEPS).add(head));
    }

    @Test
    void fiveDragonHeadsFlyOutOfTheVictimAndBurstIntoEmbers() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();
        new ChineseDragonsFinalKill().script(Scene.at(victim, new Vec(1, 0, 0)), stage, timeline, new Random(5));

        timeline.run(0);
        assertThat(stage.sounds).containsExactly("mob.enderdragon.growl", "fireworks.launch");
        assertThat(stage.pitches.getFirst()).isCloseTo(2, offset(0.01f));
        List<RecordingStage.FakeActor> dragons = stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).toList();
        assertThat(dragons).hasSize(ChineseDragonsFinalKill.DRAGONS).allSatisfy(dragon -> {
            assertThat(dragon.style).isEqualTo(StandStyle.HIDDEN);
            assertThat(dragon.skulls).containsEntry(Slot.HELMET, ChineseDragonsFinalKill.DRAGON);
            assertThat(dragon.pose.head().x()).isBetween(-ChineseDragonsFinalKill.MAX_CLIMB, -ChineseDragonsFinalKill.MIN_CLIMB);
        });
        assertThat(dragons.stream().map(dragon -> dragon.yaw).distinct()).hasSize(ChineseDragonsFinalKill.DRAGONS);

        for (int tick = 1; tick < ChineseDragonsFinalKill.END; tick++) {
            timeline.run(tick);
        }
        assertThat(stage.alive()).isEqualTo(ChineseDragonsFinalKill.DRAGONS);
        assertThat(dragons).allSatisfy(dragon -> {
            double flown = Math.hypot(dragon.position.x() - victim.x(), dragon.position.z() - victim.z());
            assertThat(flown).isBetween(3.0, 4.0);
            assertThat(dragon.position.y()).isGreaterThan(victim.y() + ChineseDragonsFinalKill.STAND_ABOVE);
        });
        assertThat(stage.particles).containsEntry("REDSTONE", ChineseDragonsFinalKill.DRAGONS * ChineseDragonsFinalKill.END);

        timeline.run(ChineseDragonsFinalKill.END);
        assertThat(timeline.length()).isEqualTo(ChineseDragonsFinalKill.END);
        assertThat(stage.alive()).isZero();
        assertThat(stage.particles).containsEntry("LAVA", ChineseDragonsFinalKill.DRAGONS);
        assertThat(stage.particles).containsEntry("REDSTONE", ChineseDragonsFinalKill.DRAGONS * (ChineseDragonsFinalKill.END + 1));
    }
}
