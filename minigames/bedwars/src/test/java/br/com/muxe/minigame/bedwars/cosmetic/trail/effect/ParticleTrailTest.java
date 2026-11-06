package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.trail.Flight;
import br.com.muxe.minigame.bedwars.cosmetic.trail.ProjectileKind;
import br.com.muxe.minigame.bedwars.cosmetic.trail.ProjectileTrail;
import java.util.List;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ParticleTrailTest {

    private static final Vec HERE = new Vec(10.6052, 20.8188, 20.0306);

    private final StageCalls calls = new StageCalls();

    static Stream<Arguments> singleParticleTrails() {
        return Stream.of(
            Arguments.of(ParticleTrail.AGUA, "agua", "WATER_SPLASH"),
            Arguments.of(ParticleTrail.ALDEAO_FURIOSO, "aldeao-furioso", "VILLAGER_ANGRY"),
            Arguments.of(ParticleTrail.CHAMAS, "chamas", "FLAME"),
            Arguments.of(ParticleTrail.CORACOES, "coracoes", "HEART"),
            Arguments.of(ParticleTrail.CRITICO, "critico", "CRIT"),
            Arguments.of(ParticleTrail.CRITICO_ENCANTADO, "critico-encantado", "CRIT_MAGIC"),
            Arguments.of(ParticleTrail.ESPECTRO_VERDE, "espectro-verde", "VILLAGER_HAPPY"));
    }

    @ParameterizedTest
    @MethodSource("singleParticleTrails")
    void aSingleParticleTrailDrawsOneStillParticleWhereTheProjectileIsEveryTick(ProjectileTrail trail, String id, String particle) {
        trail.tick(flight(HERE, 0, ProjectileKind.SNOWBALL), calls.stage, new SplittableRandom(1));
        Vec next = HERE.add(-0.0541, 0.2658, -1.4497);
        trail.tick(flight(next, 1, ProjectileKind.SNOWBALL), calls.stage, new SplittableRandom(1));

        assertThat(trail.id()).isEqualTo(id);
        assertThat(calls.particles).containsExactly(
            new StageCalls.Emission(particle, HERE, 0, Vec.ZERO, 1, List.of()),
            new StageCalls.Emission(particle, next, 0, Vec.ZERO, 1, List.of()));
        assertThat(calls.others).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(ProjectileKind.class)
    void theTrailIsTheSameForEveryKindOfProjectile(ProjectileKind kind) {
        ParticleTrail.CRITICO.tick(flight(HERE, 3, kind), calls.stage, new SplittableRandom(1));

        assertThat(calls.particles).containsExactly(new StageCalls.Emission("CRIT", HERE, 0, Vec.ZERO, 1, List.of()));
    }

    @Test
    void aProjectileThatStoppedMovingKeepsGettingTheParticleWhereItRests() {
        for (int age = 20; age < 23; age++) {
            ParticleTrail.ESPECTRO_VERDE.tick(flight(HERE, age, ProjectileKind.FISHING_HOOK), calls.stage, new SplittableRandom(1));
        }

        assertThat(calls.particles).hasSize(3).allSatisfy(emission -> assertThat(emission.position()).isEqualTo(HERE));
    }

    @Test
    void chromaDrawsFiveSpellParticlesSpreadByThreeRandomFractionsEveryTick() {
        RandomGenerator random = new SplittableRandom(42);
        RandomGenerator same = new SplittableRandom(42);

        ParticleTrail.CHROMA.tick(flight(HERE, 0, ProjectileKind.ARROW), calls.stage, random);
        ParticleTrail.CHROMA.tick(flight(HERE, 1, ProjectileKind.ARROW), calls.stage, random);

        Vec first = new Vec(same.nextFloat(), same.nextFloat(), same.nextFloat());
        Vec second = new Vec(same.nextFloat(), same.nextFloat(), same.nextFloat());
        assertThat(ParticleTrail.CHROMA.id()).isEqualTo("chroma");
        assertThat(calls.particles).containsExactly(
            new StageCalls.Emission("SPELL_MOB", HERE, 5, first, 1, List.of()),
            new StageCalls.Emission("SPELL_MOB", HERE, 5, second, 1, List.of()));
        assertThat(first).isNotEqualTo(second);
        assertThat(calls.others).isEmpty();
    }

    @Test
    void theRandomSpreadStaysBetweenZeroAndOneOnEveryAxis() {
        RandomGenerator random = new SplittableRandom(7);

        for (int draw = 0; draw < 1000; draw++) {
            Vec spread = ParticleSpread.RANDOM.next(random);
            assertThat(List.of(spread.x(), spread.y(), spread.z())).allSatisfy(value -> assertThat(value).isGreaterThanOrEqualTo(0).isLessThan(1));
        }
    }

    @Test
    void landingDrawsNothingAndTheStageClosesAtOnce() {
        Stream.of(ParticleTrail.AGUA, ParticleTrail.ALDEAO_FURIOSO, ParticleTrail.CHAMAS, ParticleTrail.CHROMA, ParticleTrail.CORACOES,
            ParticleTrail.CRITICO, ParticleTrail.CRITICO_ENCANTADO, ParticleTrail.ESPECTRO_VERDE).forEach(trail -> {
                trail.land(flight(HERE, 15, ProjectileKind.ARROW), calls.stage, new SplittableRandom(1));
                assertThat(trail.linger()).isZero();
            });

        assertThat(calls.particles).isEmpty();
        assertThat(calls.others).isEmpty();
    }

    @Test
    void aTrailNeedsAnIdAParticleAndACountThatIsNotNegative() {
        assertThatThrownBy(() -> ParticleTrail.single(null, "CRIT")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ParticleTrail.single("critico", null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ParticleTrail("critico", "CRIT", 0, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ParticleTrail("critico", "CRIT", -1, ParticleSpread.NONE)).isInstanceOf(IllegalArgumentException.class);
    }

    private static Flight flight(Vec position, int age, ProjectileKind kind) {
        return new Flight(position, new Vec(0, 0.2, -1.4), age, kind);
    }
}
