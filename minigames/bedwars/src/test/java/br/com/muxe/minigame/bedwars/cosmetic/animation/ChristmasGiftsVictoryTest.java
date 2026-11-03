package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ChristmasGiftsVictoryTest {

    @Test
    void aGiftFallsTwelveBlocksInTwentyTwoTicksSpeedingUp() {
        assertThat(ChristmasGiftsVictory.fallen(0)).isZero();
        assertThat(ChristmasGiftsVictory.fallen(22)).isCloseTo(12, offset(1e-9));
        assertThat(ChristmasGiftsVictory.fallen(11) - ChristmasGiftsVictory.fallen(10)).isLessThan(ChristmasGiftsVictory.fallen(21) - ChristmasGiftsVictory.fallen(20));
        assertThat(ChristmasGiftsVictory.fallen(40)).isEqualTo(ChristmasGiftsVictory.fallen(22));
    }

    @Test
    void giftsLandInFrontOfTheWinnerAndNeverOnTopOfThem() {
        Vec origin = new Vec(0, 64, 0);
        Vec forward = new Vec(0, 0, -1);
        Vec right = new Vec(1, 0, 0);
        Random random = new Random(7);
        for (int index = 0; index < 200; index++) {
            Vec spot = ChristmasGiftsVictory.spot(origin, forward, right, random);
            assertThat(spot.z()).isBetween(-8.0, -1.5);
            assertThat(Math.abs(spot.x())).isLessThanOrEqualTo(7);
            assertThat(Math.hypot(spot.x(), spot.z())).isGreaterThanOrEqualTo(2.5);
        }
    }

    @Test
    void theStarIsAFivePointedShapeWithItsTipUp() {
        List<double[]> pixels = ChristmasGiftsVictory.starPixels(1.1, 0.45, 0.12);

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
    void aWinnerLookingStraightUpStillGetsAHorizontalDirection() {
        assertThat(ChristmasGiftsVictory.flat(new Vec(0, 1, 0))).isEqualTo(new Vec(0, 0, 1));
        assertThat(ChristmasGiftsVictory.flat(new Vec(2, 5, 0))).isEqualTo(new Vec(1, 0, 0));
    }
}
