package br.com.muxe.minigame.bedwars.cosmetic.trail.effect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.trail.Flight;
import br.com.muxe.minigame.bedwars.cosmetic.trail.ProjectileKind;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ColorCycleTrailTest {

    private static final Vec HERE = new Vec(10.6783, 20.5999, 19.9995);

    private final TrailRecorder recorder = new TrailRecorder();

    @Test
    void ultraSendsTenPinkDustPacketsWhereTheProjectileIsEveryTick() {
        ColorCycleTrail.ULTRA.tick(new Flight(HERE, new Vec(0.018, 0.049, -1.486), 0, ProjectileKind.SNOWBALL), recorder.stage, new SplittableRandom(1));

        Vec pink = ColorCycleTrail.rgb(249, 39, 203);
        assertThat(ColorCycleTrail.ULTRA.id()).isEqualTo("ultra");
        assertThat(recorder.particles).hasSize(10).containsOnly(new TrailRecorder.Puff("REDSTONE", HERE, 0, pink, 1, List.of()));
        assertThat((float) pink.x()).isCloseTo(0.976471f, within(1.0E-6f));
        assertThat((float) pink.y()).isCloseTo(0.152941f, within(1.0E-6f));
        assertThat((float) pink.z()).isCloseTo(0.796078f, within(1.0E-6f));
        assertThat(recorder.items).isEmpty();
    }

    @Test
    void ultraKeepsTheSameColorForTheWholeFlight() {
        IntStream.range(0, 30).forEach(age -> ColorCycleTrail.ULTRA.tick(flight(age), recorder.stage, new SplittableRandom(age)));

        assertThat(recorder.particles).hasSize(300).extracting(TrailRecorder.Puff::spread).containsOnly(ColorCycleTrail.rgb(249, 39, 203));
    }

    @Test
    void theRainbowGoesThroughSevenColorsOneEachTickAndStartsOver() {
        IntStream.range(0, 8).forEach(age -> ColorCycleTrail.ARCO_IRIS.tick(flight(age), recorder.stage, new SplittableRandom(age)));

        assertThat(ColorCycleTrail.ARCO_IRIS.id()).isEqualTo("arco-iris");
        assertThat(recorder.particles).hasSize(80);
        List<Vec> perTick = IntStream.range(0, 8).mapToObj(tick -> recorder.particles.get(tick * 10).spread()).toList();
        assertThat(perTick).containsExactly(
            ColorCycleTrail.rgb(255, 51, 51),
            ColorCycleTrail.rgb(255, 153, 51),
            ColorCycleTrail.rgb(255, 255, 51),
            ColorCycleTrail.rgb(51, 255, 51),
            ColorCycleTrail.rgb(51, 255, 255),
            ColorCycleTrail.rgb(51, 51, 255),
            ColorCycleTrail.rgb(255, 51, 255),
            ColorCycleTrail.rgb(255, 51, 51));
        assertThat(recorder.particles.subList(10, 20)).extracting(TrailRecorder.Puff::spread).containsOnly(ColorCycleTrail.rgb(255, 153, 51));
    }

    @Test
    void christmasAlternatesRedGreenAndWhiteWithOneDustPacketATick() {
        IntStream.range(0, 4).forEach(age -> ColorCycleTrail.NATALINO.tick(flight(age), recorder.stage, new SplittableRandom(age)));

        assertThat(ColorCycleTrail.NATALINO.id()).isEqualTo("natalino");
        assertThat(recorder.particles).extracting(TrailRecorder.Puff::spread).containsExactly(
            ColorCycleTrail.rgb(181, 37, 26),
            ColorCycleTrail.rgb(44, 127, 40),
            ColorCycleTrail.rgb(255, 255, 255),
            ColorCycleTrail.rgb(181, 37, 26));
        assertThat(recorder.particles).extracting(TrailRecorder.Puff::count).containsOnly(0);
    }

    @ParameterizedTest
    @EnumSource(ProjectileKind.class)
    void everyKindOfProjectileGetsTheSameTrail(ProjectileKind kind) {
        ColorCycleTrail.ARCO_IRIS.tick(new Flight(HERE, new Vec(0, 0.5, -1), 3, kind), recorder.stage, new SplittableRandom(1));

        assertThat(recorder.particles).hasSize(10).containsOnly(new TrailRecorder.Puff("REDSTONE", HERE, 0, ColorCycleTrail.rgb(51, 255, 51), 1, List.of()));
    }

    @Test
    void aProjectileRestingOnTheGroundKeepsItsTrailInPlace() {
        Vec resting = new Vec(10.0853, 21.0002, 6.612);
        ColorCycleTrail.ULTRA.tick(new Flight(resting, new Vec(0, -0.04, 0), 24, ProjectileKind.FISHING_HOOK), recorder.stage, new SplittableRandom(1));

        assertThat(recorder.particles).hasSize(10).extracting(TrailRecorder.Puff::position).containsOnly(resting);
    }

    @Test
    void landingDrawsNothingAndTheStageClosesAtOnce() {
        ColorCycleTrail.ULTRA.land(flight(5), recorder.stage, new SplittableRandom(1));

        assertThat(recorder.particles).isEmpty();
        assertThat(recorder.others).isEmpty();
        assertThat(ColorCycleTrail.ULTRA.linger()).isZero();
    }

    @Test
    void aTrailNeedsAtLeastOneColorAndOnePacket() {
        assertThatThrownBy(() -> new ColorCycleTrail("empty", 1, List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ColorCycleTrail("none", 0, List.of(Vec.ZERO))).isInstanceOf(IllegalArgumentException.class);
    }

    private static Flight flight(int age) {
        return new Flight(HERE.add(0, 0, -1.5 * age), new Vec(0, 0, -1.5), age, ProjectileKind.ARROW);
    }
}
