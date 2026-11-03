package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ZombieApocalypseVictoryTest {

    private final Vec origin = new Vec(10.5, 27, -3.5);

    private Scene scene() {
        return new Scene(origin, new Vec(0, 0, 1), Optional.empty(), List.of(UUID.randomUUID()));
    }

    private RecordingStage stage() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        stage.eyes = origin.add(0, 1.62, 0);
        return stage;
    }

    private Timeline script(RecordingStage stage, long seed) {
        Timeline timeline = new Timeline();
        new ZombieApocalypseVictory().script(scene(), stage, timeline, new Random(seed));
        return timeline;
    }

    @Test
    void theSwirlTurnsThreeWitchSparksAroundTheWinnerOneHundredAndTwentyDegreesApart() {
        Vec winner = new Vec(5, 30, 5);

        for (int tick = 1; tick < 100; tick += 9) {
            Vec first = ZombieApocalypseVictory.swirl(winner, tick, 0);
            Vec second = ZombieApocalypseVictory.swirl(winner, tick, 1);
            Vec third = ZombieApocalypseVictory.swirl(winner, tick, 2);

            assertThat(Math.hypot(first.x() - winner.x(), first.z() - winner.z())).isCloseTo(1.1, offset(1e-9));
            assertThat(first.y() - winner.y()).isBetween(0.1 - 1e-9, 1.9 + 1e-9);
            assertThat(first.distance(second)).isCloseTo(second.distance(third), offset(1e-9));
        }
    }

    @Test
    void zombiesWalkStraightAtTheWinnerFasterWhenTheyAreBabies() {
        Vec zombie = new Vec(0, 27, 0);
        Vec winner = new Vec(10, 27, 0);

        Vec adult = ZombieApocalypseVictory.advance(zombie, winner, ZombieApocalypseVictory.speed(false));
        Vec baby = ZombieApocalypseVictory.advance(zombie, winner, ZombieApocalypseVictory.speed(true));

        assertThat(adult.x()).isCloseTo(0.2, offset(1e-9));
        assertThat(baby.x()).isCloseTo(0.3, offset(1e-9));
        assertThat(adult.z()).isZero();
        assertThat(ZombieApocalypseVictory.advance(new Vec(9.9, 27, 0), winner, 0.3)).isEqualTo(new Vec(10, 27, 0));
    }

    @Test
    void zombiesRiseFromTwoBlocksUnderTheGroundInEightSteps() {
        assertThat(ZombieApocalypseVictory.rising(27, 0)).isEqualTo(25);
        assertThat(ZombieApocalypseVictory.rising(27, 4)).isEqualTo(26);
        assertThat(ZombieApocalypseVictory.rising(27, 8)).isEqualTo(27);
        assertThat(ZombieApocalypseVictory.rising(27, 20)).isEqualTo(27);
    }

    @Test
    void zombiesEmergeBetweenFiveAndElevenBlocksFromTheWinnerOnBlockCentres() {
        Random random = new Random(3);
        Vec winner = new Vec(0.5, 27, 0.5);
        for (int attempt = 0; attempt < 200; attempt++) {
            Vec spot = ZombieApocalypseVictory.emergence(winner, random.nextDouble() * 360, 5 + random.nextDouble() * 6.3);

            assertThat(spot.x() - Math.floor(spot.x())).isCloseTo(0.5, offset(1e-9));
            assertThat(spot.z() - Math.floor(spot.z())).isCloseTo(0.5, offset(1e-9));
            assertThat(ZombieApocalypseVictory.flat(spot, winner)).isBetween(4.0, 12.5);
        }
    }

    @Test
    void zombiesSpawnEveryTwoTicksAndFiveBoltsAreScheduledOnSpawnTicks() {
        assertThat(ZombieApocalypseVictory.spawnTick(2)).isTrue();
        assertThat(ZombieApocalypseVictory.spawnTick(3)).isFalse();
        assertThat(ZombieApocalypseVictory.spawnTick(ZombieApocalypseVictory.END)).isFalse();

        Set<Integer> bolts = ZombieApocalypseVictory.boltTicks(new Random(9));

        assertThat(bolts).hasSize(5).allSatisfy(tick -> assertThat(ZombieApocalypseVictory.spawnTick(tick)).isTrue());
    }

    @Test
    void theNightFallsAtTheStartAndAtMostTwentyZombiesStandAtOnce() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, 5);
        long most = 0;

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
            most = Math.max(most, stage.alive());
            assertThat(stage.alive()).isLessThanOrEqualTo(20);
        }

        assertThat(stage.times).containsExactly(ZombieApocalypseVictory.NIGHT);
        assertThat(most).isBetween(15L, 20L);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Zombie")).count()).isGreaterThan(40);
    }

    @Test
    void someZombiesAreBabiesSomeVanishInSmokeAndSomeExplodeAfterCharging() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, 5);
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        long babies = stage.actors.stream().filter(actor -> actor.watched.get(ZombieApocalypseVictory.BABY_FLAG) != null).count();
        Map<String, Long> sounds = stage.sounds.stream().collect(Collectors.groupingBy(sound -> sound, Collectors.counting()));
        assertThat(babies).isBetween(1L, 30L);
        assertThat(sounds).containsKeys("dig.gravel", "random.fizz", "mob.creeper.say", "random.explode", "ambient.weather.thunder");
        assertThat(sounds.get("lightning")).isEqualTo(8L);
        assertThat(stage.particles).containsKeys("SPELL_WITCH", "SMOKE_NORMAL", "BLOCK_CRACK", "EXPLOSION_LARGE", "SMOKE_LARGE");
    }

    @Test
    void zombiesAlwaysHeadForTheWinnersCurrentPosition() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, 11);
        for (int tick = 0; tick <= 40; tick++) {
            timeline.run(tick);
        }
        Vec moved = new Vec(40.5, 28.62, 30.5);
        stage.eyes = moved;
        Vec winner = moved.add(0, -1.62, 0);
        Map<FakeActor, Vec> before = stage.actors.stream()
            .filter(actor -> !actor.removed())
            .collect(Collectors.toMap(actor -> actor, actor -> actor.position));

        for (int tick = 41; tick <= 45; tick++) {
            timeline.run(tick);
        }

        List<FakeActor> still = before.keySet().stream().filter(actor -> !actor.removed()).toList();
        assertThat(still).isNotEmpty().allSatisfy(actor ->
            assertThat(ZombieApocalypseVictory.flat(actor.position, winner)).isLessThanOrEqualTo(ZombieApocalypseVictory.flat(before.get(actor), winner) + 1e-9));
        assertThat(still).anySatisfy(actor ->
            assertThat(ZombieApocalypseVictory.flat(actor.position, winner)).isLessThan(ZombieApocalypseVictory.flat(before.get(actor), winner) - 0.1));
    }

    @Test
    void withoutAnyFloorNoZombieEverRises() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = script(stage, 1);
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).isEmpty();
    }
}
