package br.com.muxe.minigame.bedwars.cosmetic.aura;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.Room;
import br.com.muxe.minigame.bedwars.room.RoomPlayer;
import org.junit.jupiter.api.Test;

class AuraRulesTest {

    @Test
    void anAlivePlayerInTheMatchShowsTheAura() {
        assertThat(AuraRules.shows(Room.Phase.PLAYING, RoomPlayer.State.ALIVE, false, false)).isTrue();
    }

    @Test
    void theInvisibilityPotionHidesTheAura() {
        assertThat(AuraRules.shows(Room.Phase.PLAYING, RoomPlayer.State.ALIVE, false, true)).isFalse();
    }

    @Test
    void spectatorsShowNoAura() {
        assertThat(AuraRules.shows(Room.Phase.PLAYING, RoomPlayer.State.ALIVE, true, false)).isFalse();
    }

    @Test
    void deadEliminatedAndDisconnectedPlayersShowNoAura() {
        assertThat(AuraRules.shows(Room.Phase.PLAYING, RoomPlayer.State.RESPAWNING, false, false)).isFalse();
        assertThat(AuraRules.shows(Room.Phase.PLAYING, RoomPlayer.State.ELIMINATED, false, false)).isFalse();
        assertThat(AuraRules.shows(Room.Phase.PLAYING, RoomPlayer.State.DISCONNECTED, false, false)).isFalse();
    }

    @Test
    void thereIsNoAuraInTheWaitingLobbyOrAfterTheMatch() {
        assertThat(AuraRules.shows(Room.Phase.WAITING, RoomPlayer.State.WAITING, false, false)).isFalse();
        assertThat(AuraRules.shows(Room.Phase.STARTING, RoomPlayer.State.WAITING, false, false)).isFalse();
        assertThat(AuraRules.shows(Room.Phase.ENDING, RoomPlayer.State.ALIVE, false, false)).isFalse();
    }
}
