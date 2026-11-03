package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.FlyingPigVictory.Track;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeMount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FlyingPigVictoryTest {

    private static final int LEADER = 6;

    private static Scene scene() {
        return new Scene(new Vec(0, 27, 0), new Vec(0, 0, 1), Optional.empty(), List.of(UUID.randomUUID()));
    }

    private static Timeline script(RecordingStage stage) {
        Timeline timeline = new Timeline();
        new FlyingPigVictory().script(scene(), stage, timeline, new Random(9));
        return timeline;
    }

    @Test
    void itIsTheComoTreinarSeuPorcoEffectAndLastsTwoHundredTicks() {
        Timeline timeline = script(new RecordingStage(block -> block[1] < 27));

        assertThat(new FlyingPigVictory().id()).isEqualTo("como-treinar-seu-porco");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void followersLagTheLeaderBySixTicksMoreEach() {
        for (int follower = 0; follower < 6; follower++) {
            assertThat(FlyingPigVictory.delay(follower)).isEqualTo(5 + 6 * follower);
        }
    }

    @Test
    void aFollowerStaysPutUntilTheLeaderHasLeftADelayAgo() {
        List<Track> history = new ArrayList<>();
        for (int tick = 0; tick < 4; tick++) {
            history.add(new Track(new Vec(tick, 0, 0), tick));
        }

        assertThat(FlyingPigVictory.replay(history, 5)).isEmpty();
        assertThat(FlyingPigVictory.replay(history, 4)).isEmpty();
        assertThat(FlyingPigVictory.replay(history, 3)).contains(history.getFirst());
        assertThat(FlyingPigVictory.replay(history, 1)).contains(history.get(2));
    }

    @Test
    void sevenPigsAppearTheLeaderSaddledAndTheFourthFollowerABaby() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);

        timeline.run(0);

        assertThat(stage.actors).hasSize(7).allSatisfy(actor -> assertThat(actor.type).isEqualTo("Pig"));
        assertThat(stage.actors.get(LEADER).watched.get(16)).isEqualTo((byte) 1);
        for (int follower = 0; follower < 6; follower++) {
            FakeActor pig = stage.actors.get(follower);
            assertThat(pig.watched.get(16)).isNull();
            assertThat(pig.watched.get(12)).isEqualTo(follower == 3 ? (Object) (byte) -1 : null);
            assertThat(pig.position).isEqualTo(new Vec(0, 28.0, 0));
        }
        assertThat(stage.mounts.getFirst().seat()).isEqualTo(new Vec(0, 28.78125, 0));
    }

    @Test
    void everyFollowerReplaysTheLeadersTrackWithItsOwnDelay() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);
        List<Vec> leader = new ArrayList<>();

        for (int tick = 0; tick <= timeline.length(); tick++) {
            if (tick > 0) {
                FakeMount mount = stage.mounts.getFirst();
                mount.pushing = tick % 40 < 30;
                mount.yaw = tick * 2.5f;
                mount.pitch = (tick % 60) - 30;
            }
            timeline.run(tick);
            leader.add(stage.actors.get(LEADER).position);
            for (int follower = 0; tick < timeline.length() && follower < 6; follower++) {
                int index = tick - FlyingPigVictory.delay(follower);
                Vec expected = index >= 0 ? leader.get(index) : leader.getFirst();
                assertThat(stage.actors.get(follower).position).as("tick %d follower %d", tick, follower).isEqualTo(expected);
            }
        }

        double travelled = 0;
        for (int index = 1; index < leader.size(); index++) {
            travelled += leader.get(index).distance(leader.get(index - 1));
        }
        assertThat(travelled).isGreaterThan(50);
    }

    @Test
    void theLeaderCarriesTheSeatInsteadOfItselfAndFollowsTheLook() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);

        timeline.run(0);
        FakeMount mount = stage.mounts.getFirst();
        mount.pushing = true;
        mount.yaw = 180;
        for (int tick = 1; tick <= 20; tick++) {
            timeline.run(tick);
        }

        assertThat(mount.seat().z()).isLessThan(-10);
        Vec leader = stage.actors.get(LEADER).position;
        assertThat(leader.x()).isEqualTo(mount.seat().x());
        assertThat(leader.y()).isEqualTo(mount.seat().y() - 0.78125);
        assertThat(leader.z()).isEqualTo(mount.seat().z());
    }

    @Test
    void theWholeTimelinePlaysTheSoundsAndParticlesAndCleansUp() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals).count()).isEqualTo(20);
        assertThat(stage.sounds.stream().filter("mob.enderdragon.wings"::equals).count()).isEqualTo(6);
        assertThat(stage.sounds.stream().filter("mob.pig.say"::equals).count()).isBetween(2L, 20L);
        assertThat(stage.particles.get("CLOUD")).isEqualTo(200);
        assertThat(stage.particles.get("HEART")).isBetween(30, 100);
        assertThat(stage.mounts).hasSize(1);
        assertThat(stage.actors).hasSize(7);
        assertThat(stage.alive()).isZero();
    }
}
