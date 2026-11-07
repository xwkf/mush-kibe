package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.minigame.bedwars.game.TeamStatus.Kind;
import org.junit.jupiter.api.Test;

class GameRulesTest {

    @Test
    void theScoreboardShowsGreenWhileSomeoneIsOnlineAndYellowWhenTheWholeTeamLeft() {
        assertThat(TeamStatus.of(true, 2, 1, true).kind()).isEqualTo(Kind.BED_ONLINE);
        assertThat(TeamStatus.of(true, 2, 0, true).kind()).isEqualTo(Kind.BED_OFFLINE);
        assertThat(TeamStatus.of(false, 2, 2, false)).isEqualTo(new TeamStatus(Kind.NO_BED, 2));
        assertThat(TeamStatus.of(false, 0, 0, false).isEliminated()).isTrue();
        assertThat(TeamStatus.of(true, 0, 0, false).isEliminated()).isTrue();
    }

    @Test
    void levelsCostFiveThousandUntilNineThenFiveThousandMoreEveryTenLevels() {
        assertThat(Levels.costOf(1)).isEqualTo(5_000);
        assertThat(Levels.costOf(9)).isEqualTo(5_000);
        assertThat(Levels.costOf(10)).isEqualTo(10_000);
        assertThat(Levels.costOf(19)).isEqualTo(10_000);
        assertThat(Levels.costOf(20)).isEqualTo(15_000);
        assertThat(Levels.progress(0).compactNeeded()).isEqualTo("5k");
        assertThat(Levels.progress(45_000 + 250)).isEqualTo(new Levels.Progress(10, 250, 10_000));
        assertThat(Levels.progress(55_000)).isEqualTo(new Levels.Progress(11, 0, 10_000));
        assertThatThrownBy(() -> Levels.costOf(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aNewPlayerStartsAtLevelOne() {
        assertThat(Levels.progress(0)).isEqualTo(new Levels.Progress(1, 0, 5_000));
        assertThat(Levels.progress(4_999).level()).isEqualTo(1);
        assertThat(Levels.progress(5_000)).isEqualTo(new Levels.Progress(2, 0, 5_000));
    }

    @Test
    void endOfMatchXpAddsMinutesTeammatesAndTheWin() {
        XpRewards rewards = new XpRewards(10, 5, 250, 100, 12, 60);

        assertThat(rewards.endOfMatch(3, 2, true)).isEqualTo(30 + 10 + 250);
        assertThat(rewards.endOfMatch(3, 2, false)).isEqualTo(40);
    }
}
