package br.com.muxe.minigame.bedwars.stats.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StatCountersTest {

    private final UUID player = UUID.randomUUID();

    @Test
    void anOutcomeAddsAGameAndEveryCounter() {
        MatchOutcome outcome = new MatchOutcome(player, "solo", 1, 1, 4, 2, 3, 1, 1, 2, MatchResult.LOSS, 50);

        StatCounters counters = StatCounters.ZERO.plus(outcome).plus(outcome);

        assertThat(counters).isEqualTo(new StatCounters(2, 2, 2, 8, 4, 6, 2, 2, 4, 0, 2, 0, 0, 100));
    }

    @Test
    void winsGrowTheWinstreakAndLossesResetItKeepingTheBest() {
        StatCounters counters = StatCounters.ZERO
            .plus(outcome(MatchResult.WIN))
            .plus(outcome(MatchResult.WIN))
            .plus(outcome(MatchResult.WIN));

        assertThat(counters.winstreak()).isEqualTo(3);
        assertThat(counters.bestWinstreak()).isEqualTo(3);

        StatCounters lost = counters.plus(outcome(MatchResult.LOSS)).plus(outcome(MatchResult.WIN));

        assertThat(lost.wins()).isEqualTo(4);
        assertThat(lost.losses()).isEqualTo(1);
        assertThat(lost.winstreak()).isEqualTo(1);
        assertThat(lost.bestWinstreak()).isEqualTo(3);
    }

    @Test
    void aGameWithoutResultKeepsTheWinstreak() {
        StatCounters counters = StatCounters.ZERO.plus(outcome(MatchResult.WIN)).plus(outcome(MatchResult.NONE));

        assertThat(counters.games()).isEqualTo(2);
        assertThat(counters.wins()).isEqualTo(1);
        assertThat(counters.losses()).isZero();
        assertThat(counters.winstreak()).isEqualTo(1);
    }

    @Test
    void outcomesRejectNegativeCountersBlankModesAndTheGeneralMode() {
        assertThatThrownBy(() -> new MatchOutcome(player, "solo", -1, 0, 0, 0, 0, 0, 0, 0, MatchResult.NONE, 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MatchOutcome(player, " ", 0, 0, 0, 0, 0, 0, 0, 0, MatchResult.NONE, 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MatchOutcome(player, PlayerStats.GENERAL, 0, 0, 0, 0, 0, 0, 0, 0, MatchResult.NONE, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anOutcomeTouchesItsModeAndTheGeneralRowInEveryPeriod() {
        assertThat(StatsSlot.touchedBy(outcome(MatchResult.WIN), Instant.parse("2026-11-04T15:00:00Z"), ZoneId.of("America/Sao_Paulo")))
            .hasSize(8)
            .extracting(StatsSlot::mode)
            .containsOnly("solo", PlayerStats.GENERAL);
    }

    @Test
    void missingModesReadAsZero() {
        PlayerStats stats = PlayerStats.EMPTY;

        assertThat(stats.of("dupla")).isEqualTo(StatCounters.ZERO);
        assertThat(stats.general()).isEqualTo(StatCounters.ZERO);
    }

    private MatchOutcome outcome(MatchResult result) {
        return new MatchOutcome(player, "solo", 0, 0, 0, 0, 0, 0, 0, 0, result, 10);
    }
}
