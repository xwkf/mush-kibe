package br.com.muxe.minigame.bedwars.spectator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SpectatorTargetsTest {

    private static final UUID SELF = UUID.randomUUID();
    private static final UUID RED = UUID.randomUUID();
    private static final UUID BLUE = UUID.randomUUID();

    @Test
    void aRandomTargetIsAnyPlayerAliveButTheSpectator() {
        for (int seed = 0; seed < 20; seed++) {
            assertThat(SpectatorTargets.random(List.of(SELF, RED, BLUE), SELF, new SplittableRandom(seed))).get().isIn(RED, BLUE);
        }
        assertThat(SpectatorTargets.random(List.of(SELF), SELF, new SplittableRandom(1))).isEmpty();
    }

    @Test
    void aPlayerInCombatIsOneAliveThatFoughtRecently() {
        assertThat(SpectatorTargets.inCombat(List.of(RED, BLUE), Set.of(BLUE, UUID.randomUUID()), SELF, new SplittableRandom(3))).contains(BLUE);
        assertThat(SpectatorTargets.inCombat(List.of(RED, BLUE), Set.of(), SELF, new SplittableRandom(3))).isEmpty();
    }

    @Test
    void spectatorsAreHiddenOnlyFromPlayersStillInTheGame() {
        assertThat(SpectatorTargets.hidden(false, true)).isTrue();
        assertThat(SpectatorTargets.hidden(true, true)).isFalse();
        assertThat(SpectatorTargets.hidden(false, false)).isFalse();
        assertThat(SpectatorTargets.hidden(true, false)).isFalse();
    }
}
