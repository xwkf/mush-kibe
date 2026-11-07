package br.com.muxe.minigame.api.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TitleRunsTest {

    private final TitleRuns runs = new TitleRuns();
    private final List<String> stopped = new ArrayList<>();
    private final UUID player = UUID.randomUUID();

    @Test
    void leavingThePreviewStopsTheVictoryTitleThatIsStillSweeping() {
        runs.start(player, () -> stopped.add("victory"));

        runs.stop(player);

        assertThat(stopped).containsExactly("victory");
        assertThat(runs.running(player)).isFalse();
    }

    @Test
    void aNewTitleForTheSamePlayerStopsTheOldOne() {
        runs.start(player, () -> stopped.add("first"));

        runs.start(player, () -> stopped.add("second"));

        assertThat(stopped).containsExactly("first");
        assertThat(runs.running(player)).isTrue();
    }

    @Test
    void aTitleThatEndedOnItsOwnIsNotStoppedAgainNorDoesItForgetANewerOne() {
        Runnable first = () -> stopped.add("first");
        runs.start(player, first);
        runs.done(player, first);
        runs.stop(player);

        Runnable second = () -> stopped.add("second");
        runs.start(player, second);
        runs.done(player, first);

        assertThat(stopped).isEmpty();
        assertThat(runs.running(player)).isTrue();
    }

    @Test
    void stoppingAPlayerWithoutATitleDoesNothing() {
        runs.stop(player);

        assertThat(stopped).isEmpty();
    }

    @Test
    void eachPlayerHasTheirOwnTitle() {
        UUID other = UUID.randomUUID();
        runs.start(player, () -> stopped.add("mine"));
        runs.start(other, () -> stopped.add("theirs"));

        runs.stop(other);

        assertThat(stopped).containsExactly("theirs");
        assertThat(runs.running(player)).isTrue();
    }
}
