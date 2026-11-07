package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class EnchantedAnimationTest {

    private static final BedFrame BED = BedFrame.of(0, 24, 2, 1, 2);
    private static final Vec BREAKER = new Vec(-3.5, 24, 2.5);

    private final RecordingStage stage = new RecordingStage(block -> block[1] <= 23);
    private final Timeline timeline = new Timeline();
    private final List<String> schedule = new ArrayList<>();

    @Test
    void theFlowersLieAroundForAboutTwoSecondsAfterTheGardenBlooms() {
        runUntil(Integer.MAX_VALUE);

        assertThat(timeline.length()).isEqualTo(EnchantedAnimation.WILT);
        assertThat(stage.alive()).isZero();
    }

    @Test
    void theSoundsFollowTheRecordedSchedule() {
        runUntil(Integer.MAX_VALUE);

        assertThat(schedule).containsExactly(
            "0 note.pling", "0 dig.grass",
            "14 dig.grass", "14 random.orb", "14 step.grass",
            "17 step.grass",
            "31 random.explode", "31 dig.grass", "31 fireworks.twinkle", "31 fireworks.launch");
        assertThat(stage.pitches).extracting(pitch -> (int) (pitch * 63)).containsExactly(100, 37, 50, 88, 50, 69, 88, 37, 75, 63);
    }

    @Test
    void aGiantSunflowerFallsFromTwelveBlocksAndLandsOnTheBed() {
        assertThat(EnchantedAnimation.fall(0)).isEqualTo(EnchantedAnimation.DROP_HEIGHT);
        assertThat(EnchantedAnimation.fall(EnchantedAnimation.FALL)).isCloseTo(EnchantedAnimation.LANDED, offset(1.0E-9));
        assertThat(EnchantedAnimation.fall(4)).isCloseTo(11.063, offset(0.001));

        runUntil(EnchantedAnimation.ROOT - 1);
        List<RecordingStage.FakeActor> stalk = stage.actors.stream().filter(actor -> actor.type.startsWith("FallingBlock:DOUBLE_PLANT")).toList();
        assertThat(stalk).extracting(actor -> actor.type).containsExactly(
            "FallingBlock:DOUBLE_PLANT:0", "FallingBlock:DOUBLE_PLANT:0", "FallingBlock:DOUBLE_PLANT:0", "FallingBlock:DOUBLE_PLANT:0",
            "FallingBlock:DOUBLE_PLANT:0", "FallingBlock:DOUBLE_PLANT:0", "FallingBlock:DOUBLE_PLANT:0", "FallingBlock:DOUBLE_PLANT:10");
        for (int block = 0; block < stalk.size(); block++) {
            RecordingStage.FakeActor stand = (RecordingStage.FakeActor) stalk.get(block).vehicle;
            assertThat(stand.position.distance(BED.middle().add(0, EnchantedAnimation.LANDED + block, 0))).isLessThan(1.0E-9);
        }
    }

    @Test
    void theBedTurnsIntoLeavesThenBloomsIntoFlowers() {
        runUntil(EnchantedAnimation.SPROUT);
        assertThat(stage.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock:LEAVES")).extracting(actor -> actor.type)
            .containsExactly("FallingBlock:LEAVES:0", "FallingBlock:LEAVES:3");
        assertThat(stage.particles).containsEntry("VILLAGER_HAPPY", 3);

        runFrom(EnchantedAnimation.SPROUT + 1, EnchantedAnimation.BLOOM + 2);
        assertThat(stage.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock") || actor.type.equals("ArmorStand")).allMatch(RecordingStage.FakeActor::removed);
        Map<String, Long> flowers = stage.actors.stream().filter(actor -> actor.type.startsWith("Item:"))
            .collect(Collectors.groupingBy(actor -> actor.type, Collectors.counting()));
        assertThat(flowers).containsEntry("Item:YELLOW_FLOWER", 9L).containsEntry("Item:RED_ROSE", 15L);
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Firework")).hasSize(1).allMatch(RecordingStage.FakeActor::removed);
        assertThat(stage.particles).containsEntry("EXPLOSION_LARGE", 1);
        int petals = EnchantedAnimation.FALL * EnchantedAnimation.PETALS_PER_TICK + EnchantedAnimation.PETALS.length * 5;
        assertThat(stage.particles).containsEntry("REDSTONE", petals).containsEntry("FIREWORKS_SPARK", EnchantedAnimation.FALL);
    }

    @Test
    void theFlowersComeInTheRecordedMix() {
        Map<Integer, Long> kinds = IntStream.range(0, EnchantedAnimation.FLOWERS).boxed()
            .collect(Collectors.groupingBy(EnchantedAnimation::flowerKind, Collectors.counting()));
        assertThat(kinds).containsExactlyInAnyOrderEntriesOf(Map.of(0, 9L, 1, 7L, 2, 4L, 3, 3L, 4, 1L));
    }

    private void runUntil(int last) {
        new EnchantedAnimation().script(BED, BREAKER, stage, timeline, new Random(31));
        runFrom(0, last);
    }

    private void runFrom(int first, int last) {
        for (int tick = first; tick <= Math.min(last, timeline.length()); tick++) {
            int before = stage.sounds.size();
            timeline.run(tick);
            for (String sound : stage.sounds.subList(before, stage.sounds.size())) {
                schedule.add(tick + " " + sound);
            }
        }
    }
}
