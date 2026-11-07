package br.com.muxe.minigame.bedwars.cosmetic.aura;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.match.MatchPhase;
import br.com.muxe.minigame.api.match.Participant;
import org.junit.jupiter.api.Test;

class AuraRulesTest {

    @Test
    void anAlivePlayerInTheMatchShowsTheAura() {
        assertThat(AuraRules.shows(MatchPhase.PLAYING, Participant.State.ALIVE, false, false)).isTrue();
    }

    @Test
    void theInvisibilityPotionHidesTheAura() {
        assertThat(AuraRules.shows(MatchPhase.PLAYING, Participant.State.ALIVE, false, true)).isFalse();
    }

    @Test
    void spectatorsShowNoAura() {
        assertThat(AuraRules.shows(MatchPhase.PLAYING, Participant.State.ALIVE, true, false)).isFalse();
    }

    @Test
    void deadEliminatedAndDisconnectedPlayersShowNoAura() {
        assertThat(AuraRules.shows(MatchPhase.PLAYING, Participant.State.RESPAWNING, false, false)).isFalse();
        assertThat(AuraRules.shows(MatchPhase.PLAYING, Participant.State.ELIMINATED, false, false)).isFalse();
        assertThat(AuraRules.shows(MatchPhase.PLAYING, Participant.State.DISCONNECTED, false, false)).isFalse();
    }

    @Test
    void thereIsNoAuraInTheWaitingLobbyOrAfterTheMatch() {
        assertThat(AuraRules.shows(MatchPhase.WAITING, Participant.State.WAITING, false, false)).isFalse();
        assertThat(AuraRules.shows(MatchPhase.STARTING, Participant.State.WAITING, false, false)).isFalse();
        assertThat(AuraRules.shows(MatchPhase.ENDING, Participant.State.ALIVE, false, false)).isFalse();
    }
}
