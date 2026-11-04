package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KillCreditTest {

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
}
