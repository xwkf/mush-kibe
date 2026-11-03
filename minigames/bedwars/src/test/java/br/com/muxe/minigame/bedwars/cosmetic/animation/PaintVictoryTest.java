package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PaintVictoryTest {

    @Test
    void aGiftFallsTwelveBlocksInTwentyTwoTicksSpeedingUp() {
        assertThat(PaintVictory.fallen(0)).isZero();
        assertThat(PaintVictory.fallen(22)).isCloseTo(12, offset(1e-9));
        assertThat(PaintVictory.fallen(11) - PaintVictory.fallen(10)).isLessThan(PaintVictory.fallen(21) - PaintVictory.fallen(20));
        assertThat(PaintVictory.fallen(40)).isEqualTo(PaintVictory.fallen(22));
    }

    @Test
    void bucketsFallAllOverTheMapButNeverOnTopOfTheWinner() {
        Vec origin = new Vec(0, 64, 0);
        Area map = new Area(-60, -60, 60, 60);
        Random random = new Random(7);
        double farthest = 0;
        for (int index = 0; index < 200; index++) {
            Vec spot = Kit.drop(map, origin, PaintVictory.MIN_DISTANCE, random);
            assertThat(spot.x()).isBetween(-60.0, 60.0);
            assertThat(spot.z()).isBetween(-60.0, 60.0);
            assertThat(Math.hypot(spot.x(), spot.z())).isGreaterThanOrEqualTo(2.5);
            farthest = Math.max(farthest, Math.hypot(spot.x(), spot.z()));
        }
        assertThat(farthest).isGreaterThan(50);
    }

    @Test
    void aBigMapGetsTwiceTheBuckets() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 64);
        Timeline timeline = new Timeline();
        new PaintVictory().script(new Scene(new Vec(0.5, 64, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(), new Area(-60, -60, 60, 60)), stage, timeline, new Random(3));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.sounds).filteredOn("random.anvil_land"::equals).hasSize(PaintVictory.BUCKETS * Area.MAX_COPIES);
    }

    @Test
    void theStarIsAFivePointedShapeWithItsTipUp() {
        List<double[]> pixels = PaintVictory.starPixels(1.1, 0.45, 0.12);

        assertThat(pixels).hasSizeBetween(60, 200);
        assertThat(pixels).anySatisfy(pixel -> assertThat(pixel[1]).isGreaterThan(0.9));
        assertThat(pixels).noneSatisfy(pixel -> assertThat(Math.hypot(pixel[0], pixel[1])).isGreaterThan(1.1));
        assertThat(pixels).anySatisfy(pixel -> assertThat(pixel).containsExactly(0.0, 0.0));
        assertThat(pixels).noneSatisfy(pixel -> {
            assertThat(pixel[0]).isEqualTo(0.0);
            assertThat(pixel[1]).isLessThan(-0.6);
        });
    }

    @Test
    void eachFireworkPaintsTheFloorAroundTheBucketWithItsColour() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);

        PaintVictory.paint(stage, new Random(5), new Vec(10.5, 27, 3.5), 14);

        assertThat(stage.blocks).contains("10,26,3=WOOL").hasSizeBetween(2, 10);
        assertThat(stage.blocks).allSatisfy(block -> assertThat(block).contains(",26,").endsWith("=WOOL"));
    }

    @Test
    void aBucketOverTheVoidFallsStraightThroughWithoutSplashing() {
        RecordingStage stage = run(new RecordingStage(block -> false));

        assertThat(stage.sounds).doesNotContain("random.anvil_land", "random.splash");
        assertThat(stage.blocks).isEmpty();
        assertThat(stage.actors).allMatch(actor -> actor.removed);
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("ArmorStand")).hasSize(PaintVictory.BUCKETS)
            .allSatisfy(bucket -> assertThat(bucket.position.y()).isLessThan(64 - 20));
    }

    @Test
    void aBucketStopsOnTheFirstBlockItHitsEvenAboveTheWinner() {
        RecordingStage stage = run(new RecordingStage(block -> block[1] <= 68));

        assertThat(stage.sounds).contains("random.anvil_land");
        assertThat(stage.blocks).isNotEmpty().allSatisfy(block -> assertThat(block).contains(",68,"));
    }

    private static RecordingStage run(RecordingStage stage) {
        Timeline timeline = new Timeline();
        new PaintVictory().script(new Scene(new Vec(0.5, 64, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of()), stage, timeline, new Random(3));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }
        return stage;
    }

    @Test
    void paintFollowsTheFloorDownASlope() {
        RecordingStage stage = new RecordingStage(block -> block[1] < (block[0] > 10 ? 25 : 27));

        PaintVictory.paint(stage, new Random(2), new Vec(10.5, 27, 3.5), 5);

        assertThat(stage.blocks).allSatisfy(block -> {
            int x = Integer.parseInt(block.split(",")[0]);
            assertThat(block).contains(x > 10 ? ",24," : ",26,");
        });
    }
}
