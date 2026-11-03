package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class HauntedVictoryTest {

    private final Vec origin = new Vec(10.5, 27, -3.5);

    private RecordingStage play(Predicate<int[]> solid, long seed) {
        RecordingStage stage = new RecordingStage(solid);
        Timeline timeline = new Timeline();
        new HauntedVictory().script(Scene.at(origin, new Vec(0, 0, 1)), stage, timeline, new Random(seed));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }
        return stage;
    }

    @Test
    void theTwoGhastsOrbitTenBlocksAroundTheOriginOnOppositeSidesAndBob() {
        for (int age = 0; age < 200; age += 7) {
            Vec first = HauntedVictory.ghastPosition(origin, age, 0);
            Vec second = HauntedVictory.ghastPosition(origin, age, 1);

            assertThat(Math.hypot(first.x() - origin.x(), first.z() - origin.z())).isCloseTo(10, offset(1e-9));
            assertThat(Math.hypot(second.x() - origin.x(), second.z() - origin.z())).isCloseTo(10, offset(1e-9));
            assertThat(first.x() - origin.x()).isCloseTo(-(second.x() - origin.x()), offset(1e-9));
            assertThat(first.y() - origin.y()).isBetween(8.2 - 1e-9, 9.8 + 1e-9);
            assertThat(first.y() + second.y() - 2 * origin.y()).isCloseTo(18, offset(1e-9));
        }
        assertThat(HauntedVictory.ghastPosition(origin, 0, 0).x()).isCloseTo(origin.x() + 10, offset(1e-9));
        assertThat(HauntedVictory.ghastPosition(origin, 0, 1).x()).isCloseTo(origin.x() - 10, offset(1e-9));
    }

    @Test
    void aCandyHeadFallsFasterAndFasterWithoutDrifting() {
        assertThat(HauntedVictory.drop(0)).isZero();
        assertThat(HauntedVictory.drop(1)).isGreaterThan(0.5);
        assertThat(HauntedVictory.drop(10) - HauntedVictory.drop(9)).isGreaterThan(HauntedVictory.drop(2) - HauntedVictory.drop(1));
        assertThat(HauntedVictory.drop(16)).isBetween(12.0, 20.0);
    }

    @Test
    void aNewHeadFallsEveryEightTicksStartingAtTheSecondTick() {
        Set<Integer> ticks = new HashSet<>();
        for (int tick = 0; tick <= HauntedVictory.END; tick++) {
            if (HauntedVictory.headTick(tick)) {
                ticks.add(tick);
            }
        }

        assertThat(ticks).hasSize(24).contains(2, 10, 18).doesNotContain(0, 1, 3);
    }

    @Test
    void theGhostHeadsRiseAboutFiveBlocksAndFanOutAlongOneAndAHalfBlocks() {
        Vec start = new Vec(0, 0, 0);

        Vec end = HauntedVictory.ghostPosition(start, 90, HauntedVictory.GHOST_LIFE);

        assertThat(end.y()).isBetween(4.0, 5.5);
        assertThat(end.z()).isCloseTo(HauntedVictory.GHOST_SPREAD, offset(1e-9));
        assertThat(HauntedVictory.ghostWobble(0)).isZero();
        assertThat(HauntedVictory.ghostWobble(4)).isCloseTo(20, offset(1e-9));
    }

    @Test
    void theSkyTurnsToNightAtTheStartAndEveryActorIsGoneAtTheEnd() {
        RecordingStage stage = play(block -> block[1] < 27, 4);

        assertThat(stage.times).hasSize(Nightfall.TICKS + 1).startsWith(Nightfall.MIDDAY).endsWith(Nightfall.NIGHT);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Ghast"))).hasSize(2);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals).count()).isGreaterThanOrEqualTo(19);
    }

    @Test
    void headsLandOnTheFloorAndBurstIntoTreatsOrIntoAGhostFuse() {
        Set<String> sounds = new HashSet<>();
        Set<String> particles = new HashSet<>();
        Set<String> actors = new HashSet<>();
        for (long seed = 1; seed <= 8; seed++) {
            RecordingStage stage = play(block -> block[1] < 27, seed);
            sounds.addAll(stage.sounds);
            particles.addAll(stage.particles.keySet());
            stage.actors.forEach(actor -> actors.add(actor.type));
            assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        }

        assertThat(sounds).contains("mob.chicken.plop", "creeper.primed", "random.explode", "mob.ghast.moan");
        assertThat(particles).contains("REDSTONE", "FIREWORKS_SPARK", "SMOKE_LARGE", "SPELL_WITCH");
        assertThat(actors).contains("Item:CAKE", "Firework", "ArmorStand");
    }

    @Test
    void headsThatMissTheIslandNeverBurstButStillVanish() {
        RecordingStage stage = play(block -> false, 2);

        assertThat(stage.sounds).doesNotContain("mob.chicken.plop", "creeper.primed");
        assertThat(stage.actors.stream().filter(actor -> actor.type.startsWith("Item"))).isEmpty();
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
