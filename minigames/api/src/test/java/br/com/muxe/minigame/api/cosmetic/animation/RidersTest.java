package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RidersTest {

    private final UUID winner = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @Test
    void everyRiderStartsOnTheGroundWhereTheyStandLookingTheirOwnWay() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        stage.eyesOf.put(other, new Vec(30.5, 27 + 1.62, -8.5));
        stage.lookOf.put(other, new Vec(-1, -0.5, 0));
        Scene scene = new Scene(new Vec(0.5, 27, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(winner, other));

        List<Riders.Rider> riders = Riders.of(scene, stage);

        assertThat(riders).hasSize(2);
        assertThat(riders.getFirst().seated()).containsExactly(winner);
        assertThat(riders.getFirst().ground()).isEqualTo(scene.origin());
        assertThat(riders.getFirst().facing()).isEqualTo(new Vec(0, 0, 1));
        assertThat(riders.get(1).seated()).containsExactly(other);
        assertThat(riders.get(1).ground()).isEqualTo(new Vec(30.5, 27, -8.5));
        assertThat(riders.get(1).facing()).isEqualTo(new Vec(-1, 0, 0));
    }

    @Test
    void aRiderInTheAirStartsOnTheFloorBelow() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        stage.eyesOf.put(other, new Vec(4.5, 33 + 1.62, 4.5));
        Scene scene = new Scene(new Vec(0.5, 27, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(winner, other));

        assertThat(Riders.of(scene, stage).get(1).ground()).isEqualTo(new Vec(4.5, 27, 4.5));
    }

    @Test
    void aSceneWithoutRidersStillHasOneRideAtItsOrigin() {
        Scene scene = new Scene(new Vec(0.5, 27, 0.5), new Vec(1, 0, 0), Optional.empty(), List.of());

        assertThat(Riders.of(scene, new RecordingStage(block -> false))).singleElement()
            .satisfies(rider -> {
                assertThat(rider.seated()).isEmpty();
                assertThat(rider.ground()).isEqualTo(scene.origin());
                assertThat(rider.yaw()).isEqualTo(Vec.ZERO.yawTowards(new Vec(1, 0, 0)));
            });
    }
}
