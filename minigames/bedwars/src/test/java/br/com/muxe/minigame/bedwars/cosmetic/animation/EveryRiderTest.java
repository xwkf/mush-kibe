package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeMount;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class EveryRiderTest {

    private final UUID winner = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @Test
    void everyLivingPlayerGetsTheirOwnHyena() {
        assertOwnMounts(new FlyingHyenaVictory());
    }

    @Test
    void everyLivingPlayerGetsTheirOwnPig() {
        assertOwnMounts(new FlyingPigVictory());
    }

    @Test
    void everyLivingPlayerGetsTheirOwnBalloons() {
        assertOwnMounts(new BalloonsVictory());
    }

    @Test
    void everyLivingPlayerGetsTheirOwnCarpetAndOasis() {
        RecordingStage stage = assertOwnMounts(new DesertVictory());

        assertThat(stage.bulk.stream().filter(change -> change.material() == Material.GOLD_BLOCK)
            .map(change -> change.x() + "," + change.z()).collect(Collectors.toSet())).hasSize(2);
    }

    @Test
    void everyLivingPlayerGetsTheirOwnBroomAndWitches() {
        RecordingStage stage = assertOwnMounts(new WitchVictory());

        assertThat(stage.tools).containsOnlyKeys(winner, other);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Player"))).hasSize(8);
    }

    private RecordingStage assertOwnMounts(SceneAnimation animation) {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        stage.eyesOf.put(other, new Vec(60.5, 27 + Riders.EYE_HEIGHT, -40.5));
        stage.lookOf.put(other, new Vec(1, 0, 0));
        Timeline timeline = new Timeline();
        animation.script(new Scene(new Vec(0.5, 27, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(winner, other)), stage, timeline, new Random(5));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.mounts).hasSize(2);
        assertThat(stage.mounts).extracting(mount -> mount.riders).containsExactly(List.of(winner), List.of(other));
        FakeMount second = stage.mounts.get(1);
        assertThat(second.start.x()).isEqualTo(60.5);
        assertThat(second.start.z()).isEqualTo(-40.5);
        assertThat(stage.actors).allMatch(actor -> actor.removed);
        return stage;
    }
}
