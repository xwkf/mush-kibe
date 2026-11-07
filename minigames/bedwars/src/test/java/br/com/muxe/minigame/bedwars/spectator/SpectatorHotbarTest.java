package br.com.muxe.minigame.bedwars.spectator;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.settings.SpectatorHotbar.Action;
import br.com.muxe.minigame.api.settings.SpectatorHotbar.Entry;
import br.com.muxe.minigame.api.settings.SpectatorHotbar;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.junit.jupiter.api.Test;

class SpectatorHotbarTest {

    private final SpectatorHotbar hotbar = SpectatorHotbar.from(RoomFixtures.CONFIG.getConfigurationSection("hotbar.spectator"));

    @Test
    void aSpectatorGetsTheTeleporterBothSwordsPlayAgainAndTheWayBackToTheLobby() {
        assertThat(hotbar.spectator()).containsExactly(
            new Entry(Action.TELEPORTER, 0, "COMPASS", "&aTeleportador"),
            new Entry(Action.RANDOM_PLAYER, 3, "IRON_SWORD", "&aTeleportar: Jogador aleatório"),
            new Entry(Action.PLAYER_IN_COMBAT, 4, "DIAMOND_SWORD", "&aTeleportar: Jogador em combate"),
            new Entry(Action.PLAY_AGAIN, 7, "PAPER", "&aJogar novamente"),
            new Entry(Action.LEAVE, 8, "BED", "&aVoltar ao Lobby")
        );
    }

    @Test
    void aWinnerOnlyGetsPlayAgainAndTheWayBackToTheLobby() {
        assertThat(hotbar.winner()).extracting(Entry::action).containsExactly(Action.PLAY_AGAIN, Action.LEAVE);
    }

    @Test
    void anItemIsRecognisedByItsSlotAndMaterial() {
        assertThat(hotbar.action(0, "COMPASS")).contains(Action.TELEPORTER);
        assertThat(hotbar.action(8, "BED")).contains(Action.LEAVE);
        assertThat(hotbar.action(7, "PAPER")).contains(Action.PLAY_AGAIN);
        assertThat(hotbar.action(8, "WOOL")).isEmpty();
        assertThat(hotbar.action(1, "COMPASS")).isEmpty();
    }
}
