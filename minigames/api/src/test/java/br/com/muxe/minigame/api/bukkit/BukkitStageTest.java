package br.com.muxe.minigame.api.bukkit;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.GameMode;
import org.junit.jupiter.api.Test;

class BukkitStageTest {

    @Test
    void playersAreInSurvivalFromTheWaitingRoomToTheEndAndGhostsInAdventure() {
        assertThat(BukkitStage.PLAYER_MODE).isEqualTo(GameMode.SURVIVAL);
        assertThat(BukkitStage.SPECTATOR_MODE).isEqualTo(GameMode.ADVENTURE);
    }
}
