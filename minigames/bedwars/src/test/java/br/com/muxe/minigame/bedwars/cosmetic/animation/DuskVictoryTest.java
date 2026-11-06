package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeMount;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DuskVictoryTest {

    private static final double FLOOR = 27;
    private static final double OWL_START = FLOOR + 1.5625;
    private static final double FLOCK_ALTITUDE = OWL_START + 81;

    private final UUID winner = UUID.randomUUID();

    private Scene scene(List<UUID> riders) {
        return new Scene(new Vec(0.5, FLOOR, 0.5), new Vec(1, 0, 0), Optional.empty(), riders);
    }

    private Timeline script(RecordingStage stage, List<UUID> riders) {
        Timeline timeline = new Timeline();
        new DuskVictory().script(scene(riders), stage, timeline, new Random(7));
        return timeline;
    }

    private static RecordingStage floor() {
        return new RecordingStage(block -> block[1] < FLOOR);
    }

    private static List<FakeActor> owls(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> DuskAnimation.OWL.equals(actor.skulls.get(Slot.HELMET))).toList();
    }

    private static List<FakeActor> flock(RecordingStage stage) {
        return owls(stage).stream().skip(1).toList();
    }

    private static void run(Timeline timeline, int from, int until) {
        for (int tick = from; tick <= until; tick++) {
            timeline.run(tick);
        }
    }

    @Test
    void itIsTheAnoitecerEffectAndLastsTwoHundredTicks() {
        Timeline timeline = script(floor(), List.of(winner));

        assertThat(new DuskVictory().id()).isEqualTo("anoitecer");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void theOwlEasesIntoACruiseOfSeventyThreeHundredthsAndClimbsFortyOneHundredthsATick() {
        assertThat(DuskVictory.flown(0)).isZero();
        assertThat(DuskVictory.flown(1)).isCloseTo(0.73 * 0.12, offset(1e-9));
        assertThat(DuskVictory.flown(10) / 10).isCloseTo(0.34, offset(0.01));
        assertThat(DuskVictory.flown(41) - DuskVictory.flown(40)).isCloseTo(0.73, offset(0.01));
        Vec start = new Vec(0, 30, 0);
        Vec heading = new Vec(1, 0, 0);
        Vec owl = DuskVictory.owl(start, heading, 100);
        assertThat(owl.y()).isCloseTo(30 + 41, offset(1e-9));
        assertThat(owl.z()).isZero();
        assertThat(owl.x()).isCloseTo(DuskVictory.flown(100), offset(1e-9));
    }

    @Test
    void theSeatStartsUnderTheOwlAndSettlesThreeBlocksBehindAndAboveIt() {
        Vec start = new Vec(0, 30, 0);
        Vec heading = new Vec(0, 0, 1);

        assertThat(DuskVictory.seat(start, heading, 0)).isEqualTo(new Vec(0, 30 - 0.0625, 0));
        Vec owl = DuskVictory.owl(start, heading, 80);
        Vec seat = DuskVictory.seat(start, heading, 80);
        assertThat(owl.z() - seat.z()).isCloseTo(3.1, offset(1e-9));
        assertThat(seat.y() - owl.y()).isCloseTo(1.15, offset(1e-9));
    }

    @Test
    void theWinnerRidesTheOwlAlongTheirFacingWhateverTheyPress() {
        RecordingStage stage = floor();
        Timeline timeline = script(stage, List.of(winner));

        timeline.run(0);
        FakeMount mount = stage.mounts.getFirst();
        mount.pushing = true;
        mount.yaw = 180;
        mount.pitch = -60;
        run(timeline, 1, 199);

        assertThat(mount.riders).containsExactly(winner);
        assertThat(mount.start).isEqualTo(new Vec(0.5, FLOOR + 1.5, 0.5));
        Vec owl = DuskVictory.owl(new Vec(0.5, OWL_START, 0.5), new Vec(1, 0, 0), 199);
        assertThat(owls(stage).getFirst().position).isEqualTo(owl);
        assertThat(mount.seat()).isEqualTo(DuskVictory.seat(new Vec(0.5, OWL_START, 0.5), new Vec(1, 0, 0), 199));
        assertThat(mount.seat().y()).isGreaterThan(FLOCK_ALTITUDE);
        assertThat(mount.path).hasSize(199);
    }

    @Test
    void anInvisibleChickenAtTheOwlsHeadHoldsTheReinsToTheWinner() {
        RecordingStage stage = floor();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, 50);

        FakeActor chicken = stage.actors.stream().filter(actor -> actor.type.equals("Chicken")).findFirst().orElseThrow();
        assertThat(chicken.leashedTo).isEqualTo(winner);
        assertThat(chicken.watched).containsEntry(0, (byte) 32);
        assertThat(chicken.position.y() - owls(stage).getFirst().position.y()).isCloseTo(1.4375, offset(1e-9));
    }

    @Test
    void inThePreviewWithoutRidersTheOwlFliesWithoutReins() {
        RecordingStage stage = floor();
        Timeline timeline = script(stage, List.of());

        run(timeline, 0, 200);

        assertThat(stage.mounts).hasSize(1);
        assertThat(stage.mounts.getFirst().riders).isEmpty();
        assertThat(stage.actors).noneMatch(actor -> actor.type.equals("Chicken"));
        assertThat(stage.alive()).isZero();
    }

    @Test
    void nightFallsAndTheOwlArrivesWithThunderAScreechAndAPuff() {
        RecordingStage stage = floor();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, 40);

        assertThat(stage.times.getLast()).isEqualTo(Nightfall.NIGHT);
        assertThat(stage.sounds).contains("ambient.weather.thunder", "mob.bat.idle", "mob.enderdragon.growl");
        assertThat(stage.pitches.get(stage.sounds.indexOf("mob.bat.idle"))).isCloseTo(SoundPitch.of(40), offset(1e-6f));
    }

    @Test
    void aFlockOfTwelveOwlsCirclesHighAboveAndKeepsItsSizeAsOwlsPuffAway() {
        RecordingStage stage = floor();
        Timeline timeline = script(stage, List.of(winner));

        timeline.run(0);
        assertThat(flock(stage)).hasSize(OwlFlock.SIZE);
        assertThat(flock(stage)).allSatisfy(owl -> assertThat(owl.position.y()).isEqualTo(FLOCK_ALTITUDE));
        int clouds = stage.particles.get("CLOUD");
        assertThat(clouds).isEqualTo(1 + OwlFlock.SIZE);

        run(timeline, 1, 199);

        assertThat(flock(stage).stream().filter(owl -> !owl.removed)).hasSize(OwlFlock.SIZE);
        assertThat(flock(stage).size()).isGreaterThan(OwlFlock.SIZE * 2);
        assertThat(stage.particles.get("CLOUD")).isEqualTo(1 + flock(stage).size() + flock(stage).stream().filter(owl -> owl.removed).count());
        assertThat(flock(stage).stream().filter(owl -> !owl.removed)).allSatisfy(owl -> assertThat(owl.position.y()).isBetween(FLOCK_ALTITUDE - 1.5, FLOCK_ALTITUDE + OwlGlide.CEILING + 1.5));
    }

    @Test
    void newOwlsAppearAroundWhereTheRiderIs() {
        Random random = new Random(3);
        Vec around = new Vec(40, 60, -12);
        for (int attempt = 0; attempt < 300; attempt++) {
            Vec spot = OwlFlock.spot(around, 140, random);
            assertThat(Math.hypot(spot.x() - 40, spot.z() + 12)).isLessThanOrEqualTo(12.5);
            assertThat(spot.y()).isEqualTo(140);
        }
    }

    @Test
    void featherTrailsWingBeatsAndLaunchesFollowTheRecordedRhythm() {
        RecordingStage stage = floor();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, 200);

        assertThat(stage.particles.get("REDSTONE")).isEqualTo(2 * (199 - 5 + 1));
        assertThat(stage.sounds.stream().filter("mob.enderdragon.wings"::equals)).hasSize(25);
        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals)).hasSize(20);
        assertThat(stage.pitches.get(stage.sounds.indexOf("mob.enderdragon.wings"))).isCloseTo(SoundPitch.of(113), offset(1e-6f));
    }

    @Test
    void everyLivingPlayerRidesTheirOwnOwlWithTheirOwnFlockAndEverythingLeavesAtTheEnd() {
        UUID other = UUID.randomUUID();
        RecordingStage stage = floor();
        stage.eyesOf.put(other, new Vec(60.5, FLOOR + Riders.EYE_HEIGHT, -40.5));
        stage.lookOf.put(other, new Vec(0, 0, -1));
        Timeline timeline = script(stage, List.of(winner, other));

        timeline.run(0);
        assertThat(stage.mounts).extracting(mount -> mount.riders).containsExactly(List.of(winner), List.of(other));
        assertThat(stage.mounts.get(1).start).isEqualTo(new Vec(60.5, FLOOR + 1.5, -40.5));
        assertThat(owls(stage)).hasSize(2 * (1 + OwlFlock.SIZE));
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Chicken")).map(actor -> actor.leashedTo)).containsExactly(winner, other);

        run(timeline, 1, 200);

        assertThat(stage.mounts.get(1).seat().z()).isLessThan(-40.5 - 100);
        assertThat(stage.alive()).isZero();
    }
}
