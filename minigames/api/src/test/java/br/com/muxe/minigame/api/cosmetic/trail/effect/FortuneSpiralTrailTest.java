package br.com.muxe.minigame.api.cosmetic.trail.effect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.trail.Flight;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileKind;
import java.util.List;
import java.util.SplittableRandom;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class FortuneSpiralTrailTest {

    private static final Vec HERE = new Vec(10.625, 20.594, 20.0);
    private static final Vec FORWARD = new Vec(0, 0, -1.46);
    private static final FortuneSpiralTrail TRAIL = FortuneSpiralTrail.ESPIRAL_DA_FORTUNA;

    private final TrailRecorder recorder = new TrailRecorder();

    @Test
    void theSpiralStartsHalfABlockToTheSideWithTwoGoldenDustPacketsAndASunflower() {
        TRAIL.tick(new Flight(HERE, FORWARD, 0, ProjectileKind.SNOWBALL), recorder.stage, new SplittableRandom(1));

        Vec point = HERE.add(-0.5, 0, 0);
        Vec gold = ColorCycleTrail.rgb(255, 198, 23);
        assertThat(TRAIL.id()).isEqualTo("espiral-da-fortuna");
        assertThat(recorder.particles).hasSize(2).allSatisfy(puff -> {
            assertThat(puff.type()).isEqualTo("REDSTONE");
            assertClose(puff.position(), point, 1.0E-9);
            assertThat(puff.count()).isZero();
            assertThat(puff.spread()).isEqualTo(gold);
            assertThat(puff.speed()).isEqualTo(1);
        });
        assertThat((float) gold.y()).isCloseTo(0.776471f, within(1.0E-6f));
        assertThat((float) gold.z()).isCloseTo(0.090196f, within(1.0E-6f));
        assertThat(recorder.items).singleElement().satisfies(item -> {
            assertThat(item.material).isEqualTo(Material.DOUBLE_PLANT);
            assertThat(item.data).isZero();
            assertClose(item.position, point.add(0, -0.5, 0), 1.0E-9);
            assertThat(item.velocities).singleElement().satisfies(velocity -> assertThat(velocity.y()).isEqualTo(0.1));
        });
    }

    @Test
    void theSpiralTurnsFortyDegreesBackEachTickAroundTheFlightPath() {
        List<Vec> recorded = List.of(
            new Vec(-0.5, 0, 0),
            new Vec(-0.3833, 0.3214, 0),
            new Vec(-0.0871, 0.4925, 0),
            new Vec(0.2497, 0.433, 0),
            new Vec(0.4693, 0.171, 0),
            new Vec(0.4688, -0.1704, 0),
            new Vec(0.2482, -0.431, 0),
            new Vec(-0.089, -0.489, 0),
            new Vec(-0.3849, -0.3182, 0),
            new Vec(-0.5, 0, 0));
        for (int age = 0; age < recorded.size(); age++) {
            assertClose(FortuneSpiralTrail.SPIRAL.offset(FORWARD, age), recorded.get(age), 0.005);
        }
    }

    @Test
    void theSpiralTiltsLikeTheRecordedFallingPotion() {
        assertClose(FortuneSpiralTrail.SPIRAL.offset(new Vec(-0.002, -0.406, -0.408), 11), new Vec(-0.0856, 0.3503, 0.3443), 0.01);
        assertClose(FortuneSpiralTrail.SPIRAL.offset(new Vec(-0.0019, -0.2647, -0.4196), 8), new Vec(-0.3844, -0.271, -0.1726), 0.01);
    }

    @Test
    void aRestingHookGetsAFlatRingAroundIt() {
        Vec still = new Vec(0, -0.0004, 0);
        for (int age = 0; age < 9; age++) {
            Vec offset = FortuneSpiralTrail.SPIRAL.offset(still, age);
            assertThat(offset.y()).isCloseTo(0, within(1.0E-9));
            assertThat(offset.length()).isCloseTo(0.5, within(1.0E-9));
        }
        assertThat(FortuneSpiralTrail.SPIRAL.offset(Vec.ZERO, 3).length()).isCloseTo(0.5, within(1.0E-9));
    }

    @Test
    void eachSunflowerDisappearsFiveTicksAfterItDroppedAndLandingLeavesTheRest() {
        for (int age = 0; age <= 6; age++) {
            TRAIL.tick(new Flight(HERE.add(FORWARD.multiply(age)), FORWARD, age, ProjectileKind.ARROW), recorder.stage, new SplittableRandom(age));
        }

        assertThat(recorder.items).hasSize(7);
        assertThat(recorder.items.subList(0, 2)).allSatisfy(item -> assertThat(item.removed()).isTrue());
        assertThat(recorder.alive()).hasSize(5);

        TRAIL.land(new Flight(HERE, FORWARD, 7, ProjectileKind.ARROW), recorder.stage, new SplittableRandom(7));

        assertThat(recorder.alive()).hasSize(5);
        assertThat(recorder.particles).hasSize(14);
        assertThat(TRAIL.linger()).isEqualTo(5);
    }

    @ParameterizedTest
    @EnumSource(ProjectileKind.class)
    void everyKindOfProjectileGetsTheSameSpiral(ProjectileKind kind) {
        TRAIL.tick(new Flight(HERE, FORWARD, 2, kind), recorder.stage, new SplittableRandom(1));

        assertThat(recorder.particles).hasSize(2);
        assertClose(recorder.particles.getFirst().position(), HERE.add(-0.0868, 0.4924, 0), 0.001);
        assertThat(recorder.items).hasSize(1);
    }

    private static void assertClose(Vec actual, Vec expected, double tolerance) {
        assertThat(actual.distance(expected)).as("%s close to %s", actual, expected).isLessThanOrEqualTo(tolerance);
    }
}
