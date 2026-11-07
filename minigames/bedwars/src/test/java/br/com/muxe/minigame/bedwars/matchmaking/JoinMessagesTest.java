package br.com.muxe.minigame.bedwars.matchmaking;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.junit.jupiter.api.Test;

class JoinMessagesTest {

    @Test
    void theMessagesFollowTheReference() {
        assertThat(RoomFixtures.CONFIG.getString("messages.room-requesting")).isEqualTo("&aSolicitando nova sala...");
        assertThat(RoomFixtures.CONFIG.getString("messages.room-sending")).isEqualTo("&aEnviando para {room}...");
        assertThat(RoomFixtures.CONFIG.getString("messages.room-created")).isEqualTo("&aSala criada!");
    }
}
