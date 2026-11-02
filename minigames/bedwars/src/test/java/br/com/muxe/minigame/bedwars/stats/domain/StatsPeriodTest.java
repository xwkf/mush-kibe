package br.com.muxe.minigame.bedwars.stats.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class StatsPeriodTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    @Test
    void aWednesdayStartsItsWeekOnMondayAndItsMonthOnTheFirst() {
        Instant wednesday = Instant.parse("2026-11-04T15:00:00Z");

        assertThat(StatsPeriod.DAILY.start(wednesday, SAO_PAULO)).isEqualTo(LocalDate.of(2026, 11, 4));
        assertThat(StatsPeriod.WEEKLY.start(wednesday, SAO_PAULO)).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(StatsPeriod.MONTHLY.start(wednesday, SAO_PAULO)).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(StatsPeriod.TOTAL.start(wednesday, SAO_PAULO)).isEqualTo(LocalDate.of(1970, 1, 1));
    }

    @Test
    void justAfterMidnightInUtcIsStillThePreviousDayInSaoPaulo() {
        Instant afterUtcMidnight = Instant.parse("2026-11-01T01:30:00Z");

        assertThat(StatsPeriod.DAILY.start(afterUtcMidnight, SAO_PAULO)).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(StatsPeriod.WEEKLY.start(afterUtcMidnight, SAO_PAULO)).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(StatsPeriod.MONTHLY.start(afterUtcMidnight, SAO_PAULO)).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void mondayAndSundayBelongToTheSameWeek() {
        assertThat(StatsPeriod.WEEKLY.start(Instant.parse("2026-11-02T03:00:00Z"), SAO_PAULO)).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(StatsPeriod.WEEKLY.start(Instant.parse("2026-11-09T02:59:59Z"), SAO_PAULO)).isEqualTo(LocalDate.of(2026, 11, 2));
    }

    @Test
    void periodsAreFoundByTheirIds() {
        assertThat(StatsPeriod.DAILY.id()).isEqualTo("diario");
        assertThat(StatsPeriod.WEEKLY.id()).isEqualTo("semanal");
        assertThat(StatsPeriod.MONTHLY.id()).isEqualTo("mensal");
        assertThat(StatsPeriod.TOTAL.id()).isEqualTo("total");
        assertThat(StatsPeriod.fromId("semanal")).contains(StatsPeriod.WEEKLY);
        assertThat(StatsPeriod.fromId("anual")).isEmpty();
    }
}
