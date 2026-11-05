package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class TreasureAnimationTest {

    private static final BedFrame BED = BedFrame.of(6, 11, 17, 6, 16);
    private static final Vec BREAKER = new Vec(6.5, 11, 21.5);
    private static final int FLOOR = 10;

    private TracedStage play(int last) {
        return new TracedStage(block -> block[1] <= FLOOR).play(new TreasureAnimation(), BED, BREAKER, 3, last);
    }

    @Test
    void theBedSinksOneBlockIntoTheSandAndIsGoneWhenTheMarkIsDrawn() {
        TracedStage stage = play(TreasureAnimation.SINK - 1);
        RecordingStage.FakeActor footStand = stage.recording.actors.getFirst();

        assertThat(stage.recording.actors).extracting(actor -> actor.type).contains("FallingBlock:BED_BLOCK:2", "FallingBlock:BED_BLOCK:10");
        assertThat(footStand.position.y()).isCloseTo(BED.foot().y() - 13 / 14.0, offset(1.0E-9));
        assertThat(stage.recording.actors).filteredOn(actor -> actor.equipment.get(Slot.HELMET) == Material.JACK_O_LANTERN).hasSize(1);

        stage.timeline.run(TreasureAnimation.SINK);

        assertThat(stage.recording.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock:BED_BLOCK")).allMatch(actor -> actor.removed);
    }

    @Test
    void sandstoneSpreadsUnderTheBedAndARedCrossMarksTheSpot() {
        TracedStage stage = play(TreasureAnimation.MARK);
        List<String> blocks = stage.recording.blocks;

        assertThat(blocks.subList(0, 9)).allMatch(block -> block.endsWith("=SANDSTONE"))
            .contains("6,10,17=SANDSTONE", "5,10,16=SANDSTONE", "7,10,18=SANDSTONE");
        assertThat(blocks.subList(blocks.size() - 5, blocks.size()))
            .containsExactly("6,10,17=WOOL", "7,10,18=WOOL", "5,10,16=WOOL", "7,10,16=WOOL", "5,10,18=WOOL");
        assertThat(blocks).allMatch(block -> block.contains(",10,"));
    }

    @Test
    void groundOutsideTheIslandIsNeverTurnedIntoSand() {
        TracedStage stage = new TracedStage(block -> block[1] <= FLOOR && block[0] == 6).play(new TreasureAnimation(), BED, BREAKER, 3);

        assertThat(stage.recording.blocks).allMatch(block -> block.startsWith("6,"));
    }

    @Test
    void theShovelFallsSpinningFromTheSkyAndStaysBuriedUntilItIsPulledOut() {
        assertThat(TreasureAnimation.shovelHeight(TreasureAnimation.DROP)).isEqualTo(8.62);
        assertThat(TreasureAnimation.shovelHeight(TreasureAnimation.DROP + 1)).isCloseTo(8.56, offset(1.0E-9));
        assertThat(TreasureAnimation.shovelHeight(TreasureAnimation.IMPACT - 1)).isCloseTo(-0.56, offset(1.0E-9));
        assertThat(TreasureAnimation.shovelHeight(TreasureAnimation.IMPACT)).isEqualTo(-1.38);

        TracedStage stage = play(TreasureAnimation.PULL_OUT - 1);
        RecordingStage.FakeActor shovel = stage.recording.actors.stream()
            .filter(actor -> actor.equipment.get(Slot.HELMET) == Material.DIAMOND_SPADE).findFirst().orElseThrow();

        assertThat(shovel.removed).isFalse();
        assertThat(shovel.position).isEqualTo(BED.middle().add(0, -1.38, 0));
        assertThat(shovel.pose.head()).isEqualTo(new Vec(0, 0, 135));
        assertThat(stage.particles("CRIT")).extracting(TracedStage.Call::tick).containsOnly(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27);

        stage.timeline.run(TreasureAnimation.PULL_OUT);

        assertThat(shovel.removed).isTrue();
    }

    @Test
    void theImpactBlowsDiamondsAndGoldThatChimeWhereTheyLand() {
        TracedStage stage = play(Integer.MAX_VALUE);

        assertThat(stage.recording.actors).filteredOn(actor -> actor.type.equals("Item:DIAMOND")).hasSize(10);
        assertThat(stage.recording.actors).filteredOn(actor -> actor.type.equals("Item:GOLD_INGOT")).hasSize(10);
        assertThat(stage.sounds("random.orb")).hasSize(20).allSatisfy(orb -> {
            assertThat(orb.tick()).isBetween(TreasureAnimation.IMPACT + 1, TreasureAnimation.END - 1);
            assertThat(orb.at().y()).isEqualTo(FLOOR + 1.0);
        });
        assertThat(stage.particles("FIREWORKS_SPARK")).hasSize(21);
        assertThat(stage.timeline.length()).isEqualTo(TreasureAnimation.END);
        assertThat(stage.recording.alive()).isZero();
    }

    @Test
    void theSoundsFollowTheRecordedSchedule() {
        TracedStage stage = play(Integer.MAX_VALUE);

        assertThat(stage.schedule().stream().filter(entry -> !entry.endsWith("random.orb"))).containsExactly(
            "2 dig.sand", "2 dig.sand", "2 dig.sand",
            "3 dig.sand", "3 dig.sand",
            "5 dig.sand",
            "7 dig.cloth", "7 note.pling", "7 dig.sand",
            "9 dig.sand",
            "10 random.bow",
            "11 dig.sand",
            "28 random.explode", "28 random.anvil_land", "28 random.levelup",
            "59 mob.chicken.plop");
        assertThat(stage.sounds("random.levelup")).extracting(TracedStage.Call::pitchByte).containsExactly(94);
        assertThat(stage.sounds("random.anvil_land")).extracting(TracedStage.Call::pitchByte).containsExactly(88);
    }
}
