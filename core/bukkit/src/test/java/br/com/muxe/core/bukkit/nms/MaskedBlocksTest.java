package br.com.muxe.core.bukkit.nms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class MaskedBlocksTest {

    private final MaskedBlocks masks = new MaskedBlocks();
    private final UUID viewer = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @Test
    void aMaskedPositionAnswersTheFakeBlockOnlyToItsViewer() {
        masks.mask(viewer, 6, 11, 17, 0);

        assertThat(masks.find(viewer, 6, 11, 17)).hasValue(0);
        assertThat(masks.find(viewer, 6, 11, 16)).isEmpty();
        assertThat(masks.find(other, 6, 11, 17)).isEmpty();
        assertThat(masks.any(viewer)).isTrue();
        assertThat(masks.any(other)).isFalse();
    }

    @Test
    void unmaskingAndClearingGiveTheRealBlockBack() {
        masks.mask(viewer, 6, 11, 17, 0);
        masks.mask(viewer, 6, 11, 16, 1);

        masks.unmask(viewer, 6, 11, 17);

        assertThat(masks.find(viewer, 6, 11, 17)).isEmpty();
        assertThat(masks.find(viewer, 6, 11, 16)).hasValue(1);

        masks.clear(viewer);

        assertThat(masks.any(viewer)).isFalse();
    }

    @Test
    void theCombinedIdKeepsTheBlockAndItsData() {
        assertThat(MaskedBlocks.combinedId(26, 8)).isEqualTo(26 | 8 << 12);
        assertThat(MaskedBlocks.combinedId(0, 0)).isZero();
    }

    @Test
    void everyMaskGetsItsOwnPipelineName() {
        assertThat(BlockMask.handlerName(1)).isNotEqualTo(BlockMask.handlerName(2));
        assertThat(BlockMask.handlerName(1)).startsWith("muxe_block_mask_");
    }
}
