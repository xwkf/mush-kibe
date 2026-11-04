package br.com.muxe.minigame.bedwars.tnt;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.junit.jupiter.api.Test;

class TntTimerSettingsTest {

    private final TntTimerSettings settings = TntTimerSettings.from(RoomFixtures.CONFIG.getConfigurationSection("tnt-timer"));

    @Test
    void theTimerShowsTheSecondsLeftWithTwoDecimalsAndAComma() {
        assertThat(settings.text(59, 60)).isEqualTo("&22,95s");
        assertThat(settings.text(0, 60)).isEqualTo("&c0,00s");
    }

    @Test
    void theColourGoesFromDarkGreenToRedAsTheFuseBurns() {
        assertThat(settings.text(54, 60)).startsWith("&2");
        assertThat(settings.text(53, 60)).startsWith("&a");
        assertThat(settings.text(36, 60)).startsWith("&a");
        assertThat(settings.text(35, 60)).startsWith("&e");
        assertThat(settings.text(18, 60)).startsWith("&e");
        assertThat(settings.text(17, 60)).startsWith("&c");
    }

    @Test
    void itIsSeenWithinTheViewDistanceAndUpdatedEveryTick() {
        assertThat(settings.viewDistance()).isEqualTo(24);
        assertThat(settings.updateTicks()).isEqualTo(1);
    }
}
