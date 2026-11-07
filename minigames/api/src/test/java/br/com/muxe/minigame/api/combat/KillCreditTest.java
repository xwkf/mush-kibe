package br.com.muxe.minigame.api.combat;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KillCreditTest {

    private static final Instant T0 = Instant.parse("2026-10-30T18:00:00Z");
    private static final UUID A = new UUID(0, 1);
    private static final UUID B = new UUID(0, 2);
    private static final UUID C = new UUID(0, 3);

    @Test
    void playersInCombatAreTheOnesWhoHitOrWereHitRecently() {
        KillCredit credit = new KillCredit();
        UUID red = UUID.randomUUID();
        UUID blue = UUID.randomUUID();
        UUID green = UUID.randomUUID();
        UUID yellow = UUID.randomUUID();
        Instant now = Instant.parse("2026-11-03T12:00:00Z");
        credit.hit(red, blue, now.minusSeconds(2));
        credit.hit(green, yellow, now.minusSeconds(30));

        assertThat(credit.fighting(now, Duration.ofSeconds(5))).containsExactlyInAnyOrder(red, blue);
    }

    @Test
    void theKillGoesToTheLastHitWithinFifteenSeconds() {
        KillCredit credit = new KillCredit();
        credit.hit(B, A, T0);
        credit.hit(B, C, T0.plusSeconds(3));

        assertThat(credit.killer(B, T0.plusSeconds(10), false, Optional.empty())).contains(C);
        assertThat(credit.killer(B, T0.plusSeconds(19), false, Optional.empty())).isEmpty();
    }

    @Test
    void aFinalKillWithoutAHitterGoesToTheFallback() {
        KillCredit credit = new KillCredit(Duration.ofSeconds(15));

        assertThat(credit.killer(B, T0, true, Optional.of(A))).contains(A);
        assertThat(credit.killer(B, T0, false, Optional.of(A))).isEmpty();
        assertThat(credit.killer(B, T0, true, Optional.of(B))).isEmpty();
    }

    @Test
    void hittingYourselfNeverCounts() {
        KillCredit credit = new KillCredit();
        credit.hit(A, A, T0);

        assertThat(credit.lastHitter(A, T0)).isEmpty();
    }

    @Test
    void assistersAreTheOtherRecentAttackers() {
        KillCredit credit = new KillCredit();
        credit.hit(B, A, T0);
        credit.hit(B, C, T0.plusSeconds(5));

        assertThat(credit.assisters(B, C, T0.plusSeconds(10))).containsExactly(A);
        assertThat(credit.assisters(B, C, T0.plusSeconds(16))).isEmpty();
    }
}
