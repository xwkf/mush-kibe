package br.com.muxe.minigame.api.match;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.match.TeamStatus.Kind;
import org.junit.jupiter.api.Test;

class TeamStatusTest {

    @Test
    void theScoreboardShowsGreenWhileSomeoneIsOnlineAndYellowWhenTheWholeTeamLeft() {
        assertThat(TeamStatus.of(true, 2, 1, true).kind()).isEqualTo(Kind.RESPAWNS);
        assertThat(TeamStatus.of(true, 2, 0, true).kind()).isEqualTo(Kind.RESPAWNS_OFFLINE);
        assertThat(TeamStatus.of(false, 2, 2, false)).isEqualTo(new TeamStatus(Kind.LAST_LIFE, 2));
        assertThat(TeamStatus.of(false, 0, 0, false).isEliminated()).isTrue();
        assertThat(TeamStatus.of(true, 0, 0, false).isEliminated()).isTrue();
    }
}
