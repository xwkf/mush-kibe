package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ActivityTest {

    private static final Instant NOW = Instant.parse("2026-11-02T12:00:00Z");

    @Test
    void aPlayerIsAfkAfterFortyFiveSecondsWithoutActivity() {
        Activity activity = new Activity();
        UUID player = UUID.randomUUID();
        activity.active(player, NOW);

        assertThat(activity.isAfk(player, NOW.plusSeconds(44))).isFalse();
        assertThat(activity.isAfk(player, NOW.plusSeconds(45))).isTrue();

        activity.active(player, NOW.plusSeconds(50));

        assertThat(activity.isAfk(player, NOW.plusSeconds(60))).isFalse();
    }

    @Test
    void aPlayerNeverSeenIsNotAfk() {
        Activity activity = new Activity();
        UUID player = UUID.randomUUID();

        assertThat(activity.isAfk(player, NOW)).isFalse();

        activity.active(player, NOW);
        activity.forget(player);

        assertThat(activity.isAfk(player, NOW.plusSeconds(100))).isFalse();
    }
}
