package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import org.junit.jupiter.api.Test;

class GiftShowerFinalKillTest {

    @Test
    void eightDifferentGiftsPopOutOfTheVictim() {
        assertThat(GiftShowerFinalKill.GIFTS).hasSize(8).doesNotHaveDuplicates();
    }

    @Test
    void eachGiftIsThrownUpAndOutwards() {
        Vec east = GiftShowerFinalKill.launch(0);
        Vec south = GiftShowerFinalKill.launch(90);

        assertThat(east.y()).isCloseTo(1.19, offset(1e-9));
        assertThat(east.x()).isCloseTo(0.28, offset(1e-9));
        assertThat(south.z()).isCloseTo(0.28, offset(1e-9));
        assertThat(south.x()).isCloseTo(0, offset(1e-9));
    }

    @Test
    void aThrownGiftArcsAboutEightBlocksUpAndLandsAboutThreeBlocksAway() {
        RecordingStage stage = new RecordingStage(block -> false);
        Body body = Body.living(Vec.ZERO, GiftShowerFinalKill.launch(0));
        double highest = 0;
        for (int tick = 0; tick < 14; tick++) {
            body.step(stage);
            highest = Math.max(highest, body.position().y());
        }

        assertThat(highest).isBetween(6.5, 9.0);
        assertThat(body.position().x()).isBetween(2.0, 3.5);
    }
}
