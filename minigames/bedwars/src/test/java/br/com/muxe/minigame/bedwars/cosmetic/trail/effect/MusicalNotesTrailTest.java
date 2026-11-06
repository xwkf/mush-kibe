package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.trail.Flight;
import br.com.muxe.minigame.bedwars.cosmetic.trail.ProjectileKind;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MusicalNotesTrailTest {

    private final MusicalNotesTrail trail = new MusicalNotesTrail();

    @Test
    void aNoteLeavesTheProjectileEveryOtherTickStartingOnTheFirst() {
        CapturedParticles captured = fly(ProjectileKind.SNOWBALL, 9, new Random(4));

        assertThat(trail.id()).isEqualTo("notas-musicais");
        assertThat(captured.particles).hasSize(5);
        assertThat(captured.particles).extracting(CapturedParticles.Particle::position)
            .containsExactly(at(0), at(2), at(4), at(6), at(8));
        assertThat(captured.particles).allSatisfy(particle -> {
            assertThat(particle.type()).isEqualTo("NOTE");
            assertThat(particle.count()).isEqualTo(1);
        });
        assertThat(captured.otherCalls).isEmpty();
    }

    @Test
    void theSpeedCountsTheNotesOfThatProjectile() {
        CapturedParticles captured = fly(ProjectileKind.ARROW, 9, new Random(5));

        assertThat(captured.particles).extracting(CapturedParticles.Particle::speed).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0);
    }

    @Test
    void everyNoteSpreadsByItsOwnRandomOffsetsBelowOneBlock() {
        CapturedParticles captured = fly(ProjectileKind.ENDER_PEARL, 40, new Random(6));
        List<Vec> spreads = captured.particles.stream().map(CapturedParticles.Particle::spread).toList();

        assertThat(spreads).allSatisfy(spread -> {
            assertThat(spread.x()).isBetween(0.0, 1.0).isNotEqualTo(1.0);
            assertThat(spread.y()).isBetween(0.0, 1.0).isNotEqualTo(1.0);
            assertThat(spread.z()).isBetween(0.0, 1.0).isNotEqualTo(1.0);
        });
        assertThat(spreads).doesNotHaveDuplicates();
        assertThat(fly(ProjectileKind.ENDER_PEARL, 40, new Random(6)).particles).extracting(CapturedParticles.Particle::spread).isEqualTo(spreads);
    }

    @Test
    void itIsTheSameForAHookAndLandingAddsNothing() {
        CapturedParticles captured = fly(ProjectileKind.FISHING_HOOK, 3, new Random(7));

        trail.land(new Flight(at(3), Vec.ZERO, 3, ProjectileKind.FISHING_HOOK), captured.stage, new Random(7));

        assertThat(captured.particles).hasSize(2);
        assertThat(captured.otherCalls).isEmpty();
        assertThat(trail.linger()).isZero();
    }

    private CapturedParticles fly(ProjectileKind kind, int ticks, Random random) {
        CapturedParticles captured = new CapturedParticles();
        for (int age = 0; age < ticks; age++) {
            trail.tick(new Flight(at(age), new Vec(0, 0.5, -1.2), age, kind), captured.stage, random);
        }
        return captured;
    }

    private static Vec at(int age) {
        return new Vec(10.6, 21.4 + 0.5 * age, 20.2 - 1.2 * age);
    }
}
