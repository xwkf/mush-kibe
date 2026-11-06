package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.trail.Flight;
import br.com.muxe.minigame.bedwars.cosmetic.trail.ProjectileKind;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SteadyParticleTrailTest {

    static Stream<Arguments> recorded() {
        return Stream.of(
            Arguments.of(SteadyParticleTrail.FUMACA, "fumaca", "SMOKE_LARGE", 0, Vec.ZERO),
            Arguments.of(SteadyParticleTrail.LAVA, "lava", "LAVA", 0, Vec.ZERO),
            Arguments.of(SteadyParticleTrail.MAGIA, "magia", "SPELL_WITCH", 0, Vec.ZERO),
            Arguments.of(SteadyParticleTrail.VAPOR, "vapor", "CLOUD", 0, Vec.ZERO),
            Arguments.of(SteadyParticleTrail.FOGOS_DE_ARTIFICIO, "fogos-de-artificio", "FIREWORKS_SPARK", 0, Vec.ZERO),
            Arguments.of(SteadyParticleTrail.SANGRENTO, "sangrento", "REDSTONE", 0, new Vec(1, 0, 0)),
            Arguments.of(SteadyParticleTrail.FAISCA_COSMICA, "faisca-cosmica", "PORTAL", 5, new Vec(0.05, 0.05, 0.05)));
    }

    @ParameterizedTest
    @MethodSource("recorded")
    void everyTickDrawsTheRecordedParticleExactlyOnTheProjectile(SteadyParticleTrail trail, String id, String type, int count, Vec spread) {
        CapturedParticles captured = new CapturedParticles();
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
        assertThat(captured.otherCalls).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("recorded")
    void itIsTheSameForEveryKindOfProjectileAndKeepsGoingOnAHookLyingStill(SteadyParticleTrail trail, String id, String type, int count, Vec spread) {
        CapturedParticles captured = new CapturedParticles();
        Vec resting = new Vec(10.5, 21, 5.1);

        for (ProjectileKind kind : ProjectileKind.values()) {
            trail.tick(new Flight(resting, Vec.ZERO, 40, kind), captured.stage, new Random(2));
        }

        assertThat(captured.particles).hasSize(ProjectileKind.values().length);
        assertThat(captured.particles).allSatisfy(particle -> assertThat(particle.position()).isEqualTo(resting));
    }

    @ParameterizedTest
    @MethodSource("recorded")
    void landingAddsNothingAndTheStageClosesAtOnce(SteadyParticleTrail trail, String id, String type, int count, Vec spread) {
        CapturedParticles captured = new CapturedParticles();

        trail.land(new Flight(new Vec(10, 19, 16), Vec.ZERO, 12, ProjectileKind.POTION), captured.stage, new Random(3));

        assertThat(captured.particles).isEmpty();
        assertThat(captured.otherCalls).isEmpty();
        assertThat(trail.linger()).isZero();
    }

    @Test
    void stillTrailsSendOneMotionlessParticleAndTintedOnesUseTheOffsetsAsTheColour() {
        assertThat(SteadyParticleTrail.still("x", "FLAME")).isEqualTo(new SteadyParticleTrail("x", "FLAME", 0, Vec.ZERO, 1));
        assertThat(SteadyParticleTrail.tinted("y", 0.2, 0.4, 0.6)).isEqualTo(new SteadyParticleTrail("y", "REDSTONE", 0, new Vec(0.2, 0.4, 0.6), 1));
    }

    @Test
    void aTrailCannotSendANegativeCount() {
        assertThatThrownBy(() -> new SteadyParticleTrail("x", "FLAME", -1, Vec.ZERO, 1)).isInstanceOf(IllegalArgumentException.class);
    }
}
