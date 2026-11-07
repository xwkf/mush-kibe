package br.com.muxe.minigame.api.cosmetic.trail.effect;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.trail.Flight;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileKind;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RecordedParticleTrailsTest {

    static Stream<Arguments> recorded() {
        return Stream.of(
            Arguments.of(ParticleTrail.FUMACA, "fumaca", "SMOKE_LARGE", 0, Vec.ZERO),
            Arguments.of(ParticleTrail.LAVA, "lava", "LAVA", 0, Vec.ZERO),
            Arguments.of(ParticleTrail.MAGIA, "magia", "SPELL_WITCH", 0, Vec.ZERO),
            Arguments.of(ParticleTrail.VAPOR, "vapor", "CLOUD", 0, Vec.ZERO),
            Arguments.of(ParticleTrail.FOGOS_DE_ARTIFICIO, "fogos-de-artificio", "FIREWORKS_SPARK", 0, Vec.ZERO),
            Arguments.of(ParticleTrail.SANGRENTO, "sangrento", "REDSTONE", 0, new Vec(1, 0, 0)),
            Arguments.of(ParticleTrail.FAISCA_COSMICA, "faisca-cosmica", "PORTAL", 5, new Vec(0.05, 0.05, 0.05)));
    }

    @ParameterizedTest
    @MethodSource("recorded")
    void everyTickDrawsTheRecordedParticleExactlyOnTheProjectile(ParticleTrail trail, String id, String type, int count, Vec spread) {
        StageCalls captured = new StageCalls();
        Vec position = new Vec(10.6, 20.5, 19.9);

        for (int age = 0; age < 5; age++) {
            trail.tick(new Flight(position.add(0, 0, -1.5 * age), new Vec(0, 0, -1.5), age, ProjectileKind.SNOWBALL), captured.stage, new Random(1));
        }

        assertThat(trail.id()).isEqualTo(id);
        assertThat(captured.particles).hasSize(5);
        assertThat(captured.particles).allSatisfy(particle -> {
            assertThat(particle.type()).isEqualTo(type);
            assertThat(particle.count()).isEqualTo(count);
            assertThat(particle.spread()).isEqualTo(spread);
            assertThat(particle.speed()).isEqualTo(1);
        });
        assertThat(captured.particles.get(3).position()).isEqualTo(position.add(0, 0, -4.5));
        assertThat(captured.others).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("recorded")
    void itIsTheSameForEveryKindOfProjectileAndKeepsGoingOnAHookLyingStill(ParticleTrail trail, String id, String type, int count, Vec spread) {
        StageCalls captured = new StageCalls();
        Vec resting = new Vec(10.5, 21, 5.1);

        for (ProjectileKind kind : ProjectileKind.values()) {
            trail.tick(new Flight(resting, Vec.ZERO, 40, kind), captured.stage, new Random(2));
        }

        assertThat(captured.particles).hasSize(ProjectileKind.values().length);
        assertThat(captured.particles).allSatisfy(particle -> assertThat(particle.position()).isEqualTo(resting));
    }

    @ParameterizedTest
    @MethodSource("recorded")
    void landingAddsNothingAndTheStageClosesAtOnce(ParticleTrail trail, String id, String type, int count, Vec spread) {
        StageCalls captured = new StageCalls();

        trail.land(new Flight(new Vec(10, 19, 16), Vec.ZERO, 12, ProjectileKind.POTION), captured.stage, new Random(3));

        assertThat(captured.particles).isEmpty();
        assertThat(captured.others).isEmpty();
        assertThat(trail.linger()).isZero();
    }

    @Test
    void tintedTrailsSendOneRedstoneDustWithTheOffsetsAsTheColour() {
        StageCalls calls = new StageCalls();
        Vec position = new Vec(1, 2, 3);

        ParticleTrail.tinted("y", 0.2, 0.4, 0.6).tick(new Flight(position, Vec.ZERO, 0, ProjectileKind.ARROW), calls.stage, new Random(1));

        assertThat(calls.particles).containsExactly(new StageCalls.Emission("REDSTONE", position, 0, new Vec(0.2, 0.4, 0.6), 1, List.of()));
    }
}
