package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Click;
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

class MagicalGirlVictoryTest {

    private static final int FLOOR = 27;

    private final UUID winner = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @Test
    void itIsTheGarotaMagicaVictoryAndLastsTwoHundredTicks() {
        Timeline timeline = script(stage(), scene(winner));

        assertThat(new MagicalGirlVictory().id()).isEqualTo("garota-magica");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void aDragonGrowlsAndOneTickLaterTheWinnerSitsOnABigStarThatWaitsForTheFirstRocket() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        assertThat(stage.sounds).containsExactly("mob.enderdragon.growl");
        assertThat(stage.tools).containsOnlyKeys(winner);
        assertThat(stage.mounts).isEmpty();

        timeline.run(1);
        FakeMount mount = stage.mounts.getFirst();
        FakeActor star = stars(stage).getFirst();
        assertThat(mount.riders).containsExactly(winner);
        assertThat(mount.start).isEqualTo(new Vec(0.5, FLOOR + 0.65, 0.5));
        assertThat(star.skulls).containsEntry(Slot.HELMET, MagicalGirlFinalKill.STAR);
        assertThat(star.style.invisible()).isTrue();
        assertThat(star.position.y()).isCloseTo(FLOOR + 0.65 - 1.78, offset(1e-9));

        for (int tick = 2; tick <= 40; tick++) {
            timeline.run(tick);
        }
        assertThat(mount.seat).isEqualTo(mount.start);
    }

    @Test
    void theStarTurnsAFifthOfTheWayTowardTheLookEachTick() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        timeline.run(1);
        stage.mounts.getFirst().yaw = 50;

        timeline.run(2);

        assertThat((double) stars(stage).getFirst().yaw).isCloseTo(10, offset(1e-4));
    }

    @Test
    void aRocketThrowsTheStarWhereTheRiderLooksWithSparksAndItFallsBackToWhereItStarted() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        timeline.run(1);
        FakeMount mount = stage.mounts.getFirst();
        mount.yaw = -90;
        int sparks = stage.particles.getOrDefault("FIREWORKS_SPARK", 0);

        stage.tools.get(winner).accept(Click.RIGHT);
        timeline.run(2);

        assertThat(stage.sounds).contains("fireworks.launch");
        assertThat(stage.particles.get("FIREWORKS_SPARK")).isGreaterThan(sparks);
        Vec first = mount.seat.subtract(mount.start);
        assertThat(first.x()).isCloseTo(1.5, offset(1e-9));
        assertThat(first.y()).isCloseTo(0.8, offset(1e-9));
        assertThat(first.z()).isCloseTo(0, offset(1e-9));
        assertThat(stars(stage).getFirst().position.y()).isCloseTo(mount.seat.y() - 1.78, offset(1e-9));

        double highest = 0;
        for (int tick = 3; tick <= 60; tick++) {
            timeline.run(tick);
            highest = Math.max(highest, mount.seat.y());
        }
        assertThat(highest - mount.start.y()).isBetween(3.0, 5.0);
        assertThat(mount.seat.y()).isEqualTo(mount.start.y());
        assertThat(mount.seat.x()).isGreaterThan(mount.start.x() + 10);
    }

    @Test
    void aRocketWorksAtMostOnceEveryFiveTicksAndIgnoresClicksBeforeTheStarAppears() {
        assertThat(MagicalGirlVictory.ready(Optional.empty(), 0)).isTrue();
        assertThat(MagicalGirlVictory.ready(Optional.of(10), 14)).isFalse();
        assertThat(MagicalGirlVictory.ready(Optional.of(10), 15)).isTrue();

        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        stage.tools.get(winner).accept(Click.RIGHT);
        assertThat(launches(stage)).isZero();
        timeline.run(1);
        stage.tools.get(winner).accept(Click.RIGHT);
        stage.tools.get(winner).accept(Click.LEFT);
        assertThat(launches(stage)).isEqualTo(1);
        for (int tick = 2; tick <= 5; tick++) {
            timeline.run(tick);
            stage.tools.get(winner).accept(Click.RIGHT);
        }
        assertThat(launches(stage)).isEqualTo(1);
        timeline.run(6);
        stage.tools.get(winner).accept(Click.RIGHT);
        assertThat(launches(stage)).isEqualTo(2);
    }

    @Test
    void theFlightKeepsItsSpeedAndClimbFallsLikeABodyAndStopsOnTheFirstBlockOrAtTheStartingHeight() {
        RocketFlight flight = new RocketFlight(new Vec(0, 30, 0), 0.65);
        flight.step((x, y, z) -> false);
        assertThat(flight.position()).isEqualTo(new Vec(0, 30, 0));

        flight.launch(0);
        flight.step((x, y, z) -> false);
        assertThat(flight.position().z()).isCloseTo(1.5, offset(1e-9));
        assertThat(flight.velocity().z()).isCloseTo(1.5 * 0.91, offset(1e-9));
        assertThat(flight.velocity().y()).isCloseTo((0.8 - 0.08) * 0.98, offset(1e-9));

        RocketFlight roof = new RocketFlight(new Vec(0.5, 30, 0.5), 0.65);
        roof.launch(0);
        for (int tick = 0; tick < 20; tick++) {
            roof.step((x, y, z) -> y == 31 && z >= 5);
        }
        assertThat(roof.flying()).isFalse();
        assertThat(roof.position().y()).isCloseTo(32.65, offset(1e-9));
    }

    @Test
    void everyEightTicksAStarChimesAndThenFallsSpinningOverTheMapWithARainbowTrail() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        timeline.run(1);
        assertThat(stage.sounds).contains("note.pling");
        assertThat(stars(stage)).hasSize(1);

        timeline.run(2);
        assertThat(stars(stage)).hasSize(2);
        FakeActor star = stars(stage).get(1);
        assertThat(star.skulls).containsEntry(Slot.HELMET, MagicalGirlFinalKill.STAR);
        assertThat(star.yaw).isEqualTo(-18);
        assertThat(star.position.y()).isBetween(FLOOR + 0.65 + 3.7 - 0.38 - 1e-9, FLOOR + 0.65 + 9.5 - 0.38 + 1e-9);
        assertThat(Math.abs(star.position.x() - 0.5)).isLessThanOrEqualTo(16.4);
        assertThat(Math.abs(star.position.z() - 0.5)).isLessThanOrEqualTo(16.4);
        assertThat(stage.particles.get("REDSTONE")).isPositive().isEqualTo(stage.particles.get("REDSTONE") / 6 * 6);

        timeline.run(3);
        assertThat(star.yaw).isEqualTo(-36);
        for (int tick = 4; tick <= 9; tick++) {
            timeline.run(tick);
        }
        assertThat(stars(stage)).hasSize(2);
        assertThat(stage.sounds.stream().filter("note.pling"::equals).count()).isEqualTo(2);
        timeline.run(10);
        assertThat(stars(stage)).hasSize(3);
    }

    @Test
    void aStarThatLandsBurstsWhereItAppearedAndFizzlesOutFourTicksLater() {
        RecordingStage stage = stage();
        ShootingStar star = ShootingStar.fall(stage, new Vec(0.5, FLOOR + 0.2, 0.5), 0);

        assertThat(star.step(stage, new Random(1))).isFalse();
        assertThat(star.landed()).isTrue();
        assertThat(star.position().y()).isEqualTo(FLOOR);
        assertThat(stage.sounds).containsExactly("mob.chicken.plop", "fireworks.twinkle");
        assertThat(stage.pitches).containsExactly(126.5f / 63, 94.5f / 63);
        for (int tick = 0; tick < 2; tick++) {
            assertThat(star.step(stage, new Random(1))).isFalse();
        }
        assertThat(star.position().y()).isCloseTo(FLOOR + 0.21, offset(1e-9));
        assertThat(star.step(stage, new Random(1))).isFalse();
        assertThat(star.step(stage, new Random(1))).isTrue();
        assertThat(stage.sounds).endsWith("random.fizz");
        assertThat(stage.alive()).isZero();
    }

    @Test
    void aStarFallsFasterAndFasterAndIsDroppedOnceItLeavesTheWorld() {
        assertThat(ShootingStar.fallStep(new Vec(0.35, -0.38, 0)).y()).isCloseTo((-0.38 - 0.05) * 0.985, offset(1e-9));
        assertThat(ShootingStar.fallStep(new Vec(0.35, -0.38, 0)).x()).isEqualTo(0.35);

        RecordingStage stage = new RecordingStage(block -> false);
        ShootingStar star = ShootingStar.fall(stage, new Vec(0.5, 20, 0.5), 90);
        int ticks = 0;
        while (!star.step(stage, new Random(1))) {
            ticks++;
        }
        assertThat(ticks).isBetween(15, 30);
        assertThat(stage.alive()).isZero();
        assertThat(stage.sounds).isEmpty();
    }

    @Test
    void theRainbowHasSixStrandsATenthOfABlockApartFromPurpleAtTheBottomToRedAtTheTop() {
        List<Vec> strands = ShootingStar.strands(Vec.ZERO, new Vec(0.3, -0.5, 0), new Vec(1, 0, 0));

        assertThat(strands).hasSize(6);
        assertThat(strands.getFirst().y()).isLessThan(strands.getLast().y());
        assertThat(strands.get(0).distance(strands.get(1))).isCloseTo(0.1, offset(1e-9));
        assertThat(strands.get(2).add(strands.get(3)).multiply(0.5).length()).isCloseTo(0, offset(1e-9));
    }

    @Test
    void everyRiderGetsTheirOwnStarAndRocketAndEverythingIsGoneAtTheEnd() {
        RecordingStage stage = stage();
        stage.eyesOf.put(other, new Vec(60.5, FLOOR + Riders.EYE_HEIGHT, -40.5));
        stage.lookOf.put(other, new Vec(1, 0, 0));
        Timeline timeline = script(stage, scene(winner, other));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
            if (tick == 100) {
                stage.tools.get(other).accept(Click.RIGHT);
            }
        }
        stage.tools.get(winner).accept(Click.RIGHT);

        assertThat(stage.tools).containsOnlyKeys(winner, other);
        assertThat(stage.mounts).extracting(mount -> mount.riders).containsExactly(List.of(winner), List.of(other));
        assertThat(stage.mounts.get(1).start).isEqualTo(new Vec(60.5, FLOOR + 0.65, -40.5));
        assertThat(stage.mounts.get(1).seat).isNotEqualTo(stage.mounts.get(1).start);
        assertThat(stage.mounts.get(0).seat).isEqualTo(stage.mounts.get(0).start);
        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals).count()).isEqualTo(21);
        assertThat(stage.sounds.stream().filter("note.pling"::equals).count()).isEqualTo(25);
        assertThat(stage.alive()).isZero();
    }

    @Test
    void thePitchOfTheChimeIsDrawnBetweenAThirdAndAnOctaveUp() {
        assertThat(MagicalGirlVictory.chime(MagicalGirlVictory.CHIME_LOWEST)).isCloseTo(1.532f, offset(1e-3f));
        assertThat(MagicalGirlVictory.chime(MagicalGirlVictory.CHIME_HIGHEST)).isCloseTo(1.992f, offset(1e-3f));
    }

    private static long launches(RecordingStage stage) {
        return stage.sounds.stream().filter("fireworks.launch"::equals).count() - (stage.mounts.isEmpty() ? 0 : 1);
    }

    private static List<FakeActor> stars(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> actor.skulls.containsValue(MagicalGirlFinalKill.STAR)).toList();
    }

    private static RecordingStage stage() {
        return new RecordingStage(block -> block[1] < FLOOR);
    }

    private static Timeline script(RecordingStage stage, Scene scene) {
        Timeline timeline = new Timeline();
        new MagicalGirlVictory().script(scene, stage, timeline, new Random(7));
        return timeline;
    }

    private static Scene scene(UUID... riders) {
        return new Scene(new Vec(0.5, FLOOR, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(riders));
    }
}
