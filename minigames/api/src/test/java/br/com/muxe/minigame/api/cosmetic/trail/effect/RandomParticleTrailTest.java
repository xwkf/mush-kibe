package br.com.muxe.minigame.api.cosmetic.trail.effect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.trail.Flight;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileKind;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RandomParticleTrailTest {

    private static final Vec HERE = new Vec(10.656, 22.321, 19.117);

    private final TrailRecorder recorder = new TrailRecorder();

    @Test
    void megaMadnessSendsOneStillParticleOfARandomTypeEveryTick() {
        RandomParticleTrail.MEGA_LOUCURA.tick(new Flight(HERE, new Vec(0, 1.7, -2.4), 0, ProjectileKind.ARROW), recorder.stage, new SplittableRandom(3));

        assertThat(RandomParticleTrail.MEGA_LOUCURA.id()).isEqualTo("mega-loucura");
        assertThat(recorder.particles).singleElement().satisfies(puff -> {
            assertThat(RandomParticleTrail.MEGA_LOUCURA.particles()).contains(puff.type());
            assertThat(puff.position()).isEqualTo(HERE);
            assertThat(puff.count()).isZero();
            assertThat(puff.spread()).isEqualTo(Vec.ZERO);
            assertThat(puff.speed()).isEqualTo(1);
            assertThat(puff.data()).isEmpty();
        });
    }

    @Test
    void megaMadnessPicksAmongTheTwentyEightRecordedTypesOnly() {
        SplittableRandom random = new SplittableRandom(7);
        for (int age = 0; age < 2000; age++) {
            RandomParticleTrail.MEGA_LOUCURA.tick(new Flight(HERE, Vec.ZERO, age, ProjectileKind.SNOWBALL), recorder.stage, random);
        }

        Set<String> seen = recorder.particles.stream().map(TrailRecorder.Puff::type).collect(Collectors.toSet());
        assertThat(RandomParticleTrail.MEGA_LOUCURA.particles()).hasSize(28).doesNotHaveDuplicates();
        assertThat(seen).containsExactlyInAnyOrderElementsOf(RandomParticleTrail.MEGA_LOUCURA.particles());
    }

    @ParameterizedTest
    @EnumSource(ProjectileKind.class)
    void everyKindOfProjectileGetsTheSameTrail(ProjectileKind kind) {
        RandomParticleTrail.MEGA_LOUCURA.tick(new Flight(HERE, new Vec(0, 0, -1), 4, kind), recorder.stage, new SplittableRandom(1));

        assertThat(recorder.particles).hasSize(1);
        assertThat(recorder.others).isEmpty();
    }

    @Test
    void landingDrawsNothing() {
        RandomParticleTrail.MEGA_LOUCURA.land(new Flight(HERE, Vec.ZERO, 9, ProjectileKind.POTION), recorder.stage, new SplittableRandom(1));

        assertThat(recorder.particles).isEmpty();
        assertThat(RandomParticleTrail.MEGA_LOUCURA.linger()).isZero();
    }

    @Test
    void aTrailNeedsSomethingToPick() {
        assertThatThrownBy(() -> new RandomParticleTrail("empty", List.of())).isInstanceOf(IllegalArgumentException.class);
    }
}
