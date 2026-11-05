package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class HauntedAnimationTest {

    private static final BedFrame BED = BedFrame.of(6, 11, 17, 6, 16);
    private static final Vec BREAKER = new Vec(6.5, 11, 21.5);
    private static final int FLOOR = 10;

    private final RecordingStage stage = new RecordingStage(block -> block[1] <= FLOOR);
    private final Timeline timeline = new Timeline();
    private final List<String> schedule = new ArrayList<>();

    @Test
    void theBonesStayUntilFiveSecondsAfterTheBedBreaksThenEverythingIsGone() {
        runUntil(Integer.MAX_VALUE);

        assertThat(timeline.length()).isEqualTo(HauntedAnimation.END);
        assertThat(stage.alive()).isZero();
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:BONE")).hasSize(HauntedAnimation.BONES);
        assertThat(stage.actors).noneMatch(actor -> actor.type.startsWith("FallingBlock"));
    }

    @Test
    void everyBoneThatLandsTurnsTheFloorUnderItIntoAGraveyardBlockWithAPlop() {
        runUntil(Integer.MAX_VALUE);

        assertThat(stage.blocks).hasSize(HauntedAnimation.BONES).allMatch(block -> block.matches("-?\\d+,10,-?\\d+=(QUARTZ_BLOCK|WOOL|STONE)"));
        assertThat(stage.sounds.stream().filter("mob.chicken.plop"::equals)).hasSize(HauntedAnimation.BONES);
        List<Integer> landings = schedule.stream().filter(entry -> entry.endsWith("mob.chicken.plop")).map(entry -> Integer.parseInt(entry.split(" ")[0])).toList();
        assertThat(landings).allMatch(tick -> tick >= 12 && tick <= 23);
    }

    @Test
    void aBoneCountsAsLandedOnceItFallsWithinABlockOfTheFloor() {
        HauntedAnimation.Bone bone = new HauntedAnimation.Bone(new Vec(0.5, 12.5, 0.5), new Vec(0, -0.3, 0));

        assertThat(bone.fall(stage)).isFalse();
        assertThat(bone.fall(stage)).isTrue();
        assertThat(bone.grave()).containsExactly(0, FLOOR, 0);
        assertThat(bone.fall(stage)).isFalse();
        assertThat(new HauntedAnimation.Bone(new Vec(0.5, 11.5, 0.5), new Vec(0, 0.3, 0)).fall(stage)).isFalse();
    }

    @Test
    void theBonesBurstOutAllAroundTheBed() {
        Random random = new Random(5);
        for (int index = 0; index < HauntedAnimation.BONES; index++) {
            Vec velocity = HauntedAnimation.boneVelocity(index, random);
            double angle = Math.toDegrees(Math.atan2(velocity.z(), velocity.x()));
            double expected = index * 10.0;
            double difference = ((angle - expected) % 360 + 540) % 360 - 180;
            assertThat(Math.abs(difference)).isLessThanOrEqualTo(HauntedAnimation.BONE_JITTER + 1.0E-9);
            assertThat(Math.hypot(velocity.x(), velocity.z())).isBetween(HauntedAnimation.BONE_MIN_SPEED, HauntedAnimation.BONE_MAX_SPEED);
            assertThat(velocity.y()).isBetween(HauntedAnimation.BONE_MIN_UP, HauntedAnimation.BONE_MAX_UP);
        }
    }

    @Test
    void threeGhostsRiseFromTheBedWanderAroundItAndFadeAway() {
        runUntil(Integer.MAX_VALUE);

        assertThat(schedule).filteredOn(entry -> entry.contains("mob.bat")).containsExactly(
            "0 mob.bat.death", "0 mob.bat.loop", "4 mob.bat.death", "4 mob.bat.loop", "9 mob.bat.death", "9 mob.bat.loop");
        assertThat(schedule).filteredOn(entry -> entry.contains("random.fizz")).containsExactly("49 random.fizz", "55 random.fizz", "58 random.fizz");
        List<RecordingStage.FakeActor> ghosts = stage.actors.stream().filter(actor -> HauntingGhost.SKIN.equals(actor.skulls.get(Slot.HELMET))).toList();
        assertThat(ghosts).hasSize(3).allMatch(ghost -> ghost.style.equals(StandStyle.HIDDEN)).allMatch(RecordingStage.FakeActor::removed);
        assertThat(ghosts).allMatch(ghost -> Math.hypot(ghost.position.x() - BED.middle().x(), ghost.position.z() - BED.middle().z()) < 3);
        assertThat(ghosts).allMatch(ghost -> ghost.position.y() > 11 && ghost.position.y() < 13);
    }

    @Test
    void aGhostEasesTowardsWhereItIsGoingAndTurnsToFaceIt() {
        Vec velocity = HauntingGhost.steer(Vec.ZERO, new Vec(2, 0, 0));
        assertThat(velocity.x()).isCloseTo(HauntingGhost.SPEED * HauntingGhost.STEERING, offset(1.0E-9));
        for (int tick = 0; tick < 30; tick++) {
            velocity = HauntingGhost.steer(velocity, new Vec(2, 0, 0));
        }
        assertThat(velocity.length()).isCloseTo(HauntingGhost.SPEED, offset(1.0E-3));
        assertThat(HauntingGhost.steer(Vec.ZERO, Vec.ZERO)).isEqualTo(Vec.ZERO);
        assertThat(HauntingGhost.turn(170, -170)).isCloseTo(182, offset(1.0E-3f));
        assertThat(HauntingGhost.turn(0, 100)).isCloseTo(60, offset(1.0E-3f));
    }

    private void runUntil(int last) {
        new HauntedAnimation().script(BED, BREAKER, stage, timeline, new Random(19));
        for (int tick = 0; tick <= Math.min(last, timeline.length()); tick++) {
            int before = stage.sounds.size();
            timeline.run(tick);
            for (String sound : stage.sounds.subList(before, stage.sounds.size())) {
                schedule.add(tick + " " + sound);
            }
        }
    }
}
