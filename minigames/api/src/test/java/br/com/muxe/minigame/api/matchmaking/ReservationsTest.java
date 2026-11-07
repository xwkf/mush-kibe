package br.com.muxe.minigame.api.matchmaking;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.network.RoomInfo;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReservationsTest {

    private static final Instant NOW = Instant.parse("2026-11-02T20:00:00Z");

    private final Reservations reservations = new Reservations(Duration.ofSeconds(30));

    @Test
    void aReservedSeatIsNotPromisedTwice() {
        UUID first = UUID.randomUUID();

        assertThat(pick(7)).isPresent();
        reservations.hold(first, "aaaaaa", "solo", Optional.empty(), NOW);

        assertThat(reservations.pending("aaaaaa", NOW)).isEqualTo(1);
        assertThat(pick(7)).isEmpty();
        assertThat(reservations.take(first, NOW)).map(Reservations.Reservation::code).contains("aaaaaa");
        assertThat(pick(7)).isPresent();
    }

    @Test
    void expiredReservationsFreeTheirSeatAndArePurged() {
        UUID player = UUID.randomUUID();
        reservations.hold(player, "aaaaaa", "solo", Optional.empty(), NOW);

        Instant later = NOW.plusSeconds(31);

        assertThat(reservations.pending("aaaaaa", later)).isZero();
        assertThat(reservations.purge(later)).isEqualTo(1);
        assertThat(reservations.size()).isZero();
        assertThat(reservations.take(player, later)).isEmpty();
    }

    private Optional<RoomInfo> pick(int joined) {
        RoomInfo room = new RoomInfo("aaaaaa", "solo", "farol", true, joined + reservations.pending("aaaaaa", NOW), 8);
        return RoomChoice.pick(List.of(room), "solo", Optional.empty(), Optional.empty());
    }
}
