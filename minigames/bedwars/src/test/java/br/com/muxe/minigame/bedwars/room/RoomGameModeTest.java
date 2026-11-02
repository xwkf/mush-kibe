package br.com.muxe.minigame.bedwars.room;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.GameMode;
import org.junit.jupiter.api.Test;

class RoomGameModeTest {

    @Test
    void playersAreInSurvivalFromTheWaitingRoomToTheEnd() {
        assertThat(RoomRuntime.PLAYER_MODE).isEqualTo(GameMode.SURVIVAL);
    }
}
