package br.com.muxe.core.bukkit.buildmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.bukkit.GameMode;
import org.junit.jupiter.api.Test;

class GameModeLockTest {

    private final GameModeLock lock = new GameModeLock();
    private final UUID player = UUID.randomUUID();

    @Test
    void aLockedPlayerOnlyGetsTheBuildRules() {
        assertThat(lock.allows(player, GameMode.SURVIVAL, false)).isFalse();
        assertThat(lock.allows(player, GameMode.ADVENTURE, false)).isTrue();
        assertThat(lock.allows(player, GameMode.CREATIVE, true)).isTrue();
    }

    @Test
    void aReleasedPlayerCanSwitchToAnyModeUntilLockedAgain() {
        lock.release(player);

        assertThat(lock.allows(player, GameMode.SURVIVAL, false)).isTrue();
        assertThat(lock.allows(UUID.randomUUID(), GameMode.SURVIVAL, false)).isFalse();

        lock.lock(player);

        assertThat(lock.allows(player, GameMode.SURVIVAL, false)).isFalse();
    }
}
