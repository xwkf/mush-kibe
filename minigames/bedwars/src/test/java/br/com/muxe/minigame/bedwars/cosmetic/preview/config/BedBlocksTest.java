package br.com.muxe.minigame.bedwars.cosmetic.preview.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BedBlocksTest {

    private static final int HEAD_PART = 8;

    @Test
    void theFootHalfPointsItsHeadAlongTheFacing() {
        assertThat(BedBlocks.of(10, 64, 5, 0)).isEqualTo(new BedBlocks(10, 64, 5, 10, 6));
        assertThat(BedBlocks.of(10, 64, 5, 1)).isEqualTo(new BedBlocks(10, 64, 5, 9, 5));
        assertThat(BedBlocks.of(10, 64, 5, 2)).isEqualTo(new BedBlocks(10, 64, 5, 10, 4));
        assertThat(BedBlocks.of(10, 64, 5, 3)).isEqualTo(new BedBlocks(10, 64, 5, 11, 5));
    }

    @Test
    void lookingAtTheHeadHalfFindsTheSameBed() {
        assertThat(BedBlocks.of(10, 64, 6, HEAD_PART)).isEqualTo(BedBlocks.of(10, 64, 5, 0));
        assertThat(BedBlocks.of(9, 64, 5, HEAD_PART | 1)).isEqualTo(BedBlocks.of(10, 64, 5, 1));
        assertThat(BedBlocks.of(10, 64, 4, HEAD_PART | 2)).isEqualTo(BedBlocks.of(10, 64, 5, 2));
        assertThat(BedBlocks.of(11, 64, 5, HEAD_PART | 3)).isEqualTo(BedBlocks.of(10, 64, 5, 3));
    }

    @Test
    void negativeCoordinatesStayOnTheirBlocks() {
        assertThat(BedBlocks.of(-3, 20, -8, 1)).isEqualTo(new BedBlocks(-3, 20, -8, -4, -8));
        assertThat(BedBlocks.of(-4, 20, -8, HEAD_PART | 1)).isEqualTo(new BedBlocks(-3, 20, -8, -4, -8));
        assertThat(BedBlocks.of(-3, 20, -8, 2)).isEqualTo(new BedBlocks(-3, 20, -8, -3, -9));
    }

    @Test
    void theDescriptionListsBothHalves() {
        assertThat(BedBlocks.of(6, 11, 17, 2).describe()).isEqualTo("6 11 17 -> 6 11 16");
    }
}
