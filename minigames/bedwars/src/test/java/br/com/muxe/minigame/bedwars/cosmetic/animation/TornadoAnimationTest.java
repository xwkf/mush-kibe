package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage;
import br.com.muxe.minigame.api.cosmetic.animation.TracedStage;
import java.util.List;
import org.junit.jupiter.api.Test;

class TornadoAnimationTest {

    private static final BedFrame BED = BedFrame.of(18, 15, 8, 17, 8);
    private static final Vec BREAKER = new Vec(22.5, 15, 8.5);
    private static final double EPSILON = 1.0E-9;

    private TracedStage play(int last) {
        return new TracedStage(block -> block[1] < 15).play(new TornadoAnimation(), BED, BREAKER, 2, last);
    }

    @Test
    void theFunnelGrowsOneLayerATickUpToTwentySixAndUnwindsAfterTheLaunch() {
        assertThat(TornadoAnimation.layers(0)).isEqualTo(1);
        assertThat(TornadoAnimation.layers(12)).isEqualTo(13);
        assertThat(TornadoAnimation.layers(25)).isEqualTo(26);
        assertThat(TornadoAnimation.layers(43)).isEqualTo(26);
        assertThat(TornadoAnimation.layers(48)).isEqualTo(19);
        assertThat(TornadoAnimation.layers(60)).isEqualTo(2);
        assertThat(TornadoAnimation.radius(0)).isEqualTo(0.3);
        assertThat(TornadoAnimation.radius(25)).isCloseTo(2.75, offset(EPSILON));
    }

    @Test
    void eachLayerHasOneGrayDustAndTwoCloudsOnATwistedSpinningSpiral() {
        TracedStage stage = play(1);
        List<TracedStage.Call> dust = stage.particles("REDSTONE");
        List<TracedStage.Call> clouds = stage.particles("CLOUD");

        assertThat(dust).hasSize(1 + 2);
        assertThat(clouds).hasSize(2 * (1 + 2));
        assertThat(dust).allSatisfy(dot -> assertThat(dot.spread()).isEqualTo(new Vec(0.48, 0.48, 0.48)));
        assertThat(dust.getFirst().at().x()).isCloseTo(BED.middle().x() + 0.3, offset(EPSILON));
        assertThat(dust.getFirst().at().z()).isCloseTo(BED.middle().z(), offset(EPSILON));
        Vec second = dust.get(2).at();
        assertThat(second.y() - BED.middle().y()).isCloseTo(0.35, offset(EPSILON));
        assertThat(Math.hypot(second.x() - BED.middle().x(), second.z() - BED.middle().z())).isCloseTo(0.398, offset(EPSILON));
        assertThat(stage.particles("BLOCK_DUST")).hasSize(2);
    }

    @Test
    void theBedHalvesCircleEachOtherWhileRisingAndThenShootIntoTheSky() {
        assertThat(TornadoAnimation.orbit(0)).isEqualTo(180);
        assertThat(TornadoAnimation.around(BED, TornadoAnimation.orbit(0), 0.5)).isEqualTo(BED.foot());
        assertThat(TornadoAnimation.lift(TornadoAnimation.LIFT_FROM)).isZero();
        assertThat(TornadoAnimation.lift(TornadoAnimation.HOVER)).isCloseTo(2.47, offset(EPSILON));
        assertThat(TornadoAnimation.lift(TornadoAnimation.BURST)).isCloseTo(31.28, offset(0.01));
        assertThat(TornadoAnimation.orbit(TornadoAnimation.HOVER + 1) - TornadoAnimation.orbit(TornadoAnimation.HOVER)).isCloseTo(-34, offset(EPSILON));

        TracedStage stage = play(TornadoAnimation.HOVER);
        RecordingStage.FakeActor foot = stage.recording.actors.get(0);
        RecordingStage.FakeActor head = stage.recording.actors.get(2);
        Vec center = foot.position.add(head.position).multiply(0.5);

        assertThat(foot.position.distance(head.position)).isCloseTo(1, offset(EPSILON));
        assertThat(center.x()).isCloseTo(BED.middle().x(), offset(EPSILON));
        assertThat(center.y() - BED.middle().y()).isCloseTo(2.47, offset(EPSILON));
    }

    @Test
    void atTheTopTheBedBurstsIntoRedWoolAndAGreenFirework() {
        TracedStage stage = play(Integer.MAX_VALUE);
        Vec top = BED.middle().add(0, TornadoAnimation.lift(TornadoAnimation.BURST) + 0.5, 0);

        assertThat(stage.particles("EXPLOSION_HUGE")).singleElement().satisfies(blast -> {
            assertThat(blast.tick()).isEqualTo(TornadoAnimation.BURST);
            assertThat(blast.at()).isEqualTo(top);
        });
        assertThat(stage.particles("BLOCK_CRACK")).singleElement().satisfies(crack -> assertThat(crack.count()).isEqualTo(60));
        assertThat(stage.recording.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock:BED_BLOCK"))
            .extracting(actor -> actor.type).containsExactly("FallingBlock:BED_BLOCK:1", "FallingBlock:BED_BLOCK:9");
        assertThat(stage.recording.alive()).isZero();
        assertThat(stage.timeline.length()).isEqualTo(TornadoAnimation.BURST + 2);
    }

    @Test
    void theWindHowlsOnTheRecordedTicks() {
        TracedStage stage = play(Integer.MAX_VALUE);

        assertThat(stage.sounds("ambient.weather.rain")).extracting(TracedStage.Call::tick).containsExactly(0, 7, 15, 22, 31, 40, 46, 53);
        assertThat(stage.sounds("mob.enderdragon.wings")).extracting(TracedStage.Call::pitchByte)
            .containsExactly(31, 36, 33, 30, 32, 30, 26, 75, 35, 25);
        assertThat(stage.schedule()).contains("43 fireworks.launch", "60 random.explode", "60 fireworks.largeBlast");
    }
}
