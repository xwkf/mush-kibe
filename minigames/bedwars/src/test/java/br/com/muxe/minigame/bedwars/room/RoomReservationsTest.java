package br.com.muxe.minigame.bedwars.room;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.matchmaking.RoomChoice;
import br.com.muxe.minigame.bedwars.network.RoomInfo;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomReservationsTest {

    private static final Instant NOW = Instant.parse("2026-11-02T20:00:00Z");

    private final RoomReservations reservations = new RoomReservations(Duration.ofSeconds(30));

    @Test
    void aReservedSeatIsNotPromisedTwice() {
        UUID first = UUID.randomUUID();

        assertThat(pick(7)).isPresent();
        reservations.hold(first, "aaaaaa", "solo", Optional.empty(), NOW);

        assertThat(reservations.pending("aaaaaa", NOW)).isEqualTo(1);
        assertThat(pick(7)).isEmpty();
        assertThat(reservations.take(first, NOW)).map(RoomReservations.Reservation::code).contains("aaaaaa");
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

    @Test
    void anEmptyWaitingRoomClosesAfterTheLimitUnlessSomeoneIsComing() {
        Room room = RoomFixtures.room("2v7bss", RoomFixtures.SOLO, RoomFixtures.arena("solo", "Slip", List.of(TeamColor.VERMELHO, TeamColor.AZUL)));

        assertThat(room.idle(true, 3)).isFalse();
        assertThat(room.idle(false, 3)).isFalse();
        assertThat(room.idle(false, 3)).isFalse();
        assertThat(room.idle(false, 3)).isTrue();

        UUID player = UUID.randomUUID();
        room.join(player, "tadeu");

        assertThat(room.idle(false, 1)).isFalse();
    }

    @Test
    void chatGoesToTheRoomAndShowsTheTeamOnlyDuringTheMatch() {
        Room room = RoomFixtures.room("2v7bss", RoomFixtures.ONE_V_ONE, RoomFixtures.arena("1v1", "Slip", List.of(TeamColor.VERMELHO, TeamColor.AZUL)));
        UUID red = UUID.randomUUID();
        UUID blue = UUID.randomUUID();
        room.join(red, "tadeu");
        room.join(blue, "x572");

        Map<UUID, ChatView> waiting = ChatView.of(room, Set.of(red, blue));
        room.start(List.of(List.of(red), List.of(blue)), NOW);
        Map<UUID, ChatView> playing = ChatView.of(room, Set.of(red));

        assertThat(waiting.get(red).prefix()).isEmpty();
        assertThat(waiting.get(red).audience()).containsExactlyInAnyOrder(red, blue);
        assertThat(playing.get(red).prefix()).isEqualTo(room.player(red).orElseThrow().team().color() + "[" + room.player(red).orElseThrow().team().letter() + "] ");
        assertThat(playing.get(blue).audience()).containsExactly(red);
    }

    private Optional<RoomInfo> pick(int joined) {
        RoomInfo room = new RoomInfo("aaaaaa", "solo", "farol", true, joined + reservations.pending("aaaaaa", NOW), 8);
        return RoomChoice.pick(List.of(room), "solo", Optional.empty(), Optional.empty());
    }
}
