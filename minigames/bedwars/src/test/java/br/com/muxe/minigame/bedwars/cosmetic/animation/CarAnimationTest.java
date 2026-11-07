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
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class CarAnimationTest {

    private static final BedFrame BED = BedFrame.of(18, 15, 8, 17, 8);
    private static final Vec BREAKER = new Vec(22.5, 15, 8.5);

    private final RecordingStage stage = new RecordingStage(block -> block[1] <= 14);
    private final Timeline timeline = new Timeline();
    private final List<String> schedule = new ArrayList<>();

    @Test
    void theCarDrivesOffLessThanASecondAfterTheBedBreaksAndLeavesNothingBehind() {
        runUntil(Integer.MAX_VALUE);

        assertThat(timeline.length()).isEqualTo(CarAnimation.END).isEqualTo(19);
        assertThat(stage.alive()).isZero();
        assertThat(schedule).containsExactly("0 mob.enderdragon.wings");
        assertThat(stage.pitches).extracting(pitch -> (int) (pitch * 63)).containsExactly(94);
    }

    @Test
    void theBedComesUpOutOfTheFloorWithALanternDriverAndFourWheels() {
        runUntil(1);

        assertThat(stage.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock:BED_BLOCK")).extracting(actor -> actor.type)
            .containsExactly("FallingBlock:BED_BLOCK:1", "FallingBlock:BED_BLOCK:9");
        assertThat(stage.actors.getFirst().position).isEqualTo(BED.foot());
        assertThat(stage.actors).filteredOn(actor -> actor.equipment.get(Slot.HELMET) == Material.JACK_O_LANTERN).hasSize(1);
        List<RecordingStage.FakeActor> wheels = wheels();
        assertThat(wheels).hasSize(4).allMatch(wheel -> wheel.style.equals(CarAnimation.WHEEL_STAND));
        assertThat(wheels).extracting(wheel -> wheel.position.y()).containsOnly(15 + CarAnimation.WHEEL_DROP);
        assertThat(wheels).allMatch(wheel -> wheel.yaw == BED.yaw());
    }

    @Test
    void theWheelsSpinFourTurnsEasingInAndOut() {
        assertThat(CarAnimation.spin(1)).isCloseTo(0.072, offset(1.0E-9));
        assertThat(CarAnimation.spin(4)).isCloseTo(18.432, offset(1.0E-9));
        assertThat(CarAnimation.spin(10)).isCloseTo(720, offset(1.0E-9));
        assertThat(CarAnimation.spin(19)).isCloseTo(1439.928, offset(1.0E-9));
        assertThat(CarAnimation.spin(20)).isCloseTo(1440, offset(1.0E-9));
        for (int step = 2; step <= 20; step++) {
            assertThat(CarAnimation.spin(step)).isGreaterThan(CarAnimation.spin(step - 1));
        }

        runUntil(19);
        assertThat(wheels()).allMatch(wheel -> wheel.pose.head().equals(new Vec(0, 1440, 90)));
    }

    @Test
    void theCarSpeedsAwayFromTheHeadAndSlowsDownTwentyEightBlocksLater() {
        assertThat(CarAnimation.travel(CarAnimation.DRIVE - 1)).isZero();
        assertThat(CarAnimation.travel(CarAnimation.DRIVE)).isCloseTo(0.09375, offset(1.0E-9));
        assertThat(CarAnimation.travel(15)).isCloseTo(15, offset(1.0E-9));
        assertThat(CarAnimation.travel(CarAnimation.END)).isCloseTo(28.03125, offset(1.0E-9));

        runUntil(CarAnimation.END - 1);
        Vec away = BED.forward().multiply(-CarAnimation.travel(CarAnimation.END - 1));
        assertThat(stage.actors.getFirst().position.distance(BED.foot().add(away))).isLessThan(1.0E-9);
        assertThat(stage.actors.getFirst().position.x()).isGreaterThan(BREAKER.x());
    }

    @Test
    void everyWheelPuffsSmokeThatTrailsThreeTicksBehind() {
        runUntil(Integer.MAX_VALUE);

        assertThat(stage.particles).containsEntry("SMOKE_LARGE", 4 * CarAnimation.END);
        Vec puff = CarAnimation.smoke(BED, CarAnimation.WHEELS[0], 13);
        assertThat(puff.x()).isCloseTo(19.7875, offset(1.0E-9));
        assertThat(puff.y()).isCloseTo(15.22, offset(1.0E-9));
        assertThat(CarAnimation.smoke(BED, CarAnimation.WHEELS[0], 2).y()).isCloseTo(15.47, offset(1.0E-9));
    }

    private List<RecordingStage.FakeActor> wheels() {
        return stage.actors.stream().filter(actor -> CarAnimation.WHEEL.equals(actor.skulls.get(Slot.HELMET))).toList();
    }

    private void runUntil(int last) {
        new CarAnimation().script(BED, BREAKER, stage, timeline, new Random(7));
        for (int tick = 0; tick <= Math.min(last, timeline.length()); tick++) {
            int before = stage.sounds.size();
            timeline.run(tick);
            for (String sound : stage.sounds.subList(before, stage.sounds.size())) {
                schedule.add(tick + " " + sound);
            }
        }
    }
}
