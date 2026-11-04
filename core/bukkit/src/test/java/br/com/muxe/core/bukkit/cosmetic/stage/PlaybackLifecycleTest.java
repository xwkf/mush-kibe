package br.com.muxe.core.bukkit.cosmetic.stage;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.stage.PlaybackLifecycle.Step;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlaybackLifecycleTest {

    @Test
    void theTimelinePlaysToItsLastTickThenFinishesOnce() {
        PlaybackLifecycle lifecycle = new PlaybackLifecycle(2);
        List<Step> steps = new ArrayList<>();

        for (int tick = 0; tick < 5; tick++) {
            steps.add(lifecycle.next(true));
        }

        assertThat(steps).containsExactly(Step.PLAY, Step.PLAY, Step.PLAY, Step.FINISH, Step.HOLD);
    }

    @Test
    void aFinishedAnimationStaysUntilNobodyIsWatchingItAnymore() {
        PlaybackLifecycle lifecycle = new PlaybackLifecycle(0);
        lifecycle.next(true);
        lifecycle.next(true);

        assertThat(lifecycle.next(true)).isEqualTo(Step.HOLD);
        assertThat(lifecycle.next(true)).isEqualTo(Step.HOLD);
        assertThat(lifecycle.next(false)).isEqualTo(Step.CLOSE);
        assertThat(lifecycle.next(false)).isEqualTo(Step.DONE);
    }

    @Test
    void anAnimationNobodyWatchesAnymoreClosesEvenMidway() {
        PlaybackLifecycle lifecycle = new PlaybackLifecycle(100);
        lifecycle.next(true);

        assertThat(lifecycle.next(false)).isEqualTo(Step.CLOSE);
    }

    @Test
    void aStoppedAnimationFreezesWithoutFinishingAndClosesOnceTheViewerLeaves() {
        PlaybackLifecycle lifecycle = new PlaybackLifecycle(100);
        lifecycle.next(true);

        lifecycle.stop();

        assertThat(lifecycle.stopped()).isTrue();
        assertThat(lifecycle.next(true)).isEqualTo(Step.HOLD);
        assertThat(lifecycle.next(false)).isEqualTo(Step.CLOSE);
    }

    @Test
    void theTickCountsOnlyPlayedTicks() {
        PlaybackLifecycle lifecycle = new PlaybackLifecycle(10);
        lifecycle.next(true);
        lifecycle.next(true);

        assertThat(lifecycle.tick()).isEqualTo(2);
    }
}
