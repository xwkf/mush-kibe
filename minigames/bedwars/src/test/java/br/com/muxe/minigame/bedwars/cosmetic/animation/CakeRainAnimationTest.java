package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class CakeRainAnimationTest {

    private static final BedFrame BED = BedFrame.of(0, 24, 2, 1, 2);
    private static final Vec BREAKER = new Vec(-3.5, 24, 2.5);

    private final RecordingStage stage = new RecordingStage(block -> block[1] <= 23);
    private final Timeline timeline = new Timeline();
    private final List<String> schedule = new ArrayList<>();

    @Test
    void theCakesStayOnTheGroundForTwoAndAHalfSecondsWithoutABed() {
        runUntil(Integer.MAX_VALUE);

        assertThat(timeline.length()).isEqualTo(CakeRainAnimation.END);
        assertThat(stage.alive()).isZero();
        assertThat(stage.actors).noneMatch(actor -> actor.type.startsWith("FallingBlock"));
        assertThat(schedule).containsExactly("0 fireworks.blast", "0 random.pop", "1 random.pop", "2 random.pop");
        assertThat(stage.pitches).extracting(pitch -> (int) (pitch * 63)).containsExactly(94, 63, 63, 63);
    }

    @Test
    void sixCakesAreThrownInPairsSixtyDegreesApartAndLandBackOnTheGround() {
        runUntil(CakeRainAnimation.BURST);

        List<RecordingStage.FakeActor> cakes = stage.actors.stream().filter(actor -> actor.skulls.containsKey(Slot.HELMET)).toList();
        assertThat(cakes).hasSize(CakeRainAnimation.COUNT).allMatch(cake -> cake.style.equals(CakeRainAnimation.CAKE_STAND));
        assertThat(cakes).extracting(cake -> cake.skulls.get(Slot.HELMET))
            .containsExactly(CakeRainAnimation.CAKES[0], CakeRainAnimation.CAKES[1], CakeRainAnimation.CAKES[0], CakeRainAnimation.CAKES[1], CakeRainAnimation.CAKES[0], CakeRainAnimation.CAKES[1]);
        for (int index = 0; index < cakes.size(); index++) {
            Vec landed = cakes.get(index).position.subtract(BED.middle());
            assertThat(landed.y()).isCloseTo(CakeRainAnimation.CAKE_DROP, offset(1.0E-9));
            assertThat(Math.hypot(landed.x(), landed.z())).isBetween(1.0, 2.2);
            assertThat(Math.toDegrees(Math.atan2(landed.z(), landed.x()))).isCloseTo(index * 60.0 > 180 ? index * 60.0 - 360 : index * 60.0, offset(1.0E-6));
        }
        assertThat(stage.actors).filteredOn(actor -> actor.type.startsWith("Item:")).extracting(actor -> actor.type)
            .containsExactly("Item:SUGAR", "Item:CAKE", "Item:SUGAR", "Item:CAKE", "Item:SUGAR", "Item:CAKE");
    }

    @Test
    void aCakeFliesUpAndComesDownInTenTicks() {
        assertThat(CakeRainAnimation.height(1)).isCloseTo(0.875, offset(1.0E-9));
        assertThat(CakeRainAnimation.height(5)).isCloseTo(2.375, offset(1.0E-9));
        assertThat(CakeRainAnimation.height(9)).isCloseTo(0.675, offset(1.0E-9));
        assertThat(CakeRainAnimation.height(10)).isZero();
        assertThat(CakeRainAnimation.distance(0.2, 1)).isCloseTo(0.2, offset(1.0E-9));
        assertThat(CakeRainAnimation.distance(0.2, 2)).isCloseTo(0.384, offset(1.0E-9));
        assertThat(CakeRainAnimation.launch(0)).isZero();
        assertThat(CakeRainAnimation.launch(5)).isEqualTo(2);
    }

    @Test
    void theCakesLeaveAWhiteAndRedTrailAndTheLandingSparksFanOut() {
        runUntil(Integer.MAX_VALUE);

        assertThat(stage.particles).containsEntry("REDSTONE", CakeRainAnimation.COUNT * CakeRainAnimation.FLIGHT)
            .containsEntry("FIREWORKS_SPARK", CakeRainAnimation.SPARKS);
    }

    private void runUntil(int last) {
        new CakeRainAnimation().script(BED, BREAKER, stage, timeline, new Random(29));
        for (int tick = 0; tick <= Math.min(last, timeline.length()); tick++) {
            int before = stage.sounds.size();
            timeline.run(tick);
            for (String sound : stage.sounds.subList(before, stage.sounds.size())) {
                schedule.add(tick + " " + sound);
            }
        }
    }
}
