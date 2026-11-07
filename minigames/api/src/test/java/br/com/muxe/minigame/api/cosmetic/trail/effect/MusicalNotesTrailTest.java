package br.com.muxe.minigame.api.cosmetic.trail.effect;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.trail.Flight;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileKind;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MusicalNotesTrailTest {

    private final MusicalNotesTrail trail = new MusicalNotesTrail();

    @Test
    void aNoteLeavesTheProjectileEveryOtherTickStartingOnTheFirst() {
        StageCalls captured = fly(ProjectileKind.SNOWBALL, 9, new Random(4));

        assertThat(trail.id()).isEqualTo("notas-musicais");
        assertThat(captured.particles).hasSize(5);
        assertThat(captured.particles).extracting(StageCalls.Emission::position)
            .containsExactly(at(0), at(2), at(4), at(6), at(8));
        assertThat(captured.particles).allSatisfy(particle -> {
            assertThat(particle.type()).isEqualTo("NOTE");
            assertThat(particle.count()).isEqualTo(1);
        });
        assertThat(captured.others).isEmpty();
    }

    @Test
    void theSpeedCountsTheNotesOfThatProjectile() {
        StageCalls captured = fly(ProjectileKind.ARROW, 9, new Random(5));

        assertThat(captured.particles).extracting(StageCalls.Emission::speed).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0);
    }

    @Test
    void everyNoteSpreadsByItsOwnRandomOffsetsBelowOneBlock() {
        StageCalls captured = fly(ProjectileKind.ENDER_PEARL, 40, new Random(6));
        List<Vec> spreads = captured.particles.stream().map(StageCalls.Emission::spread).toList();

        assertThat(spreads).allSatisfy(spread -> {
            assertThat(spread.x()).isBetween(0.0, 1.0).isNotEqualTo(1.0);
            assertThat(spread.y()).isBetween(0.0, 1.0).isNotEqualTo(1.0);
            assertThat(spread.z()).isBetween(0.0, 1.0).isNotEqualTo(1.0);
        });
        assertThat(spreads).doesNotHaveDuplicates();
        assertThat(fly(ProjectileKind.ENDER_PEARL, 40, new Random(6)).particles).extracting(StageCalls.Emission::spread).isEqualTo(spreads);
    }

    @Test
    void itIsTheSameForAHookAndLandingAddsNothing() {
        StageCalls captured = fly(ProjectileKind.FISHING_HOOK, 3, new Random(7));

        trail.land(new Flight(at(3), Vec.ZERO, 3, ProjectileKind.FISHING_HOOK), captured.stage, new Random(7));

        assertThat(captured.particles).hasSize(2);
        assertThat(captured.others).isEmpty();
        assertThat(trail.linger()).isZero();
    }

    private StageCalls fly(ProjectileKind kind, int ticks, Random random) {
        StageCalls captured = new StageCalls();
        for (int age = 0; age < ticks; age++) {
            trail.tick(new Flight(at(age), new Vec(0, 0.5, -1.2), age, kind), captured.stage, random);
        }
        return captured;
    }

    private static Vec at(int age) {
        return new Vec(10.6, 21.4 + 0.5 * age, 20.2 - 1.2 * age);
    }
}
