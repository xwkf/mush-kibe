package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class LevelFillTest {

    @Test
    void theXpBarShowsHowFarThePlayerIsInTheCurrentLevel() {
        Levels.Progress halfway = Levels.progress(2_500);
        Levels.Progress nextLevel = Levels.progress(5_000);

        assertThat(halfway.level()).isZero();
        assertThat(halfway.fill()).isCloseTo(0.5f, within(1e-6f));
        assertThat(nextLevel.level()).isEqualTo(1);
        assertThat(nextLevel.fill()).isZero();
    }

    @Test
    void theBarNeverLooksFull() {
        assertThat(new Levels.Progress(3, 5_000, 5_000).fill()).isEqualTo(Levels.Progress.FULLEST_BAR);
        assertThat(new Levels.Progress(3, 0, 0).fill()).isZero();
    }
}
