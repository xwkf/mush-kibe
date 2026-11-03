package br.com.muxe.minigame.bedwars.tnt;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.junit.jupiter.api.Test;

class TntTimerSettingsTest {

    private final TntTimerSettings settings = TntTimerSettings.from(RoomFixtures.CONFIG.getConfigurationSection("tnt-timer"));

    @Test
    void theTimerShowsTheSecondsLeftWithOneDecimal() {
        assertThat(settings.text(52, 52)).isEqualTo("&a2.6s");
        assertThat(settings.text(0, 52)).isEqualTo("&c0.0s");
    }

    @Test
    void theColourGoesFromGreenToRedAsTheFuseBurns() {
        assertThat(settings.text(40, 52)).startsWith("&a");
        assertThat(settings.text(30, 52)).startsWith("&e");
        assertThat(settings.text(20, 52)).startsWith("&6");
        assertThat(settings.text(10, 52)).startsWith("&c");
    }

    @Test
    void itIsSeenWithinTheViewDistanceAndUpdatedOftenEnoughToCountTenths() {
        assertThat(settings.viewDistance()).isEqualTo(24);
        assertThat(settings.updateTicks()).isEqualTo(2);
    }
}
