package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SoundPitchTest {

    @Test
    void everyRecordedPitchByteReachesTheClientUnchanged() {
        for (int recorded = 0; recorded < 128; recorded++) {
            assertThat((int) (SoundPitch.of(recorded) * SoundPitch.STEPS)).as("byte " + recorded).isEqualTo(recorded);
        }
    }
}
