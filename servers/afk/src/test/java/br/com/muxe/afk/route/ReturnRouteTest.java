package br.com.muxe.afk.route;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.server.ServerIdentity;
import br.com.muxe.core.common.server.ServerKind;
import br.com.muxe.core.common.server.directory.ServerDirectory;
import br.com.muxe.core.common.server.directory.ServerStatus;
import br.com.muxe.core.common.testing.MutableClock;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReturnRouteTest {

    private final MutableClock clock = MutableClock.startingAt("2026-11-03T20:00:00Z");
    private final ServerDirectory servers = new ServerDirectory(clock, Duration.ofSeconds(30));
    private final ReturnRoute route = new ReturnRoute(servers, "lobby");

    @Test
    void thePlayerGoesBackToTheExactServerTheyCameFrom() {
        online("bedwars-lobby-2", "bedwars-lobby", 10, 100);

        assertThat(route.resolve(Optional.of("bedwars-lobby-2"))).isEqualTo(new Destination.Server("bedwars-lobby-2"));
    }

    @Test
    void whenThatServerIsGoneThePlayerGoesToTheMainLobby() {
        online("lobby-1", "lobby", 10, 100);

        assertThat(route.resolve(Optional.of("lobby-2"))).isEqualTo(new Destination.Group("lobby"));
    }

    @Test
    void whenThatServerIsFullThePlayerGoesToTheMainLobby() {
        online("lobby-2", "lobby", 100, 100);

        assertThat(route.resolve(Optional.of("lobby-2"))).isEqualTo(new Destination.Group("lobby"));
    }

    @Test
    void withoutAKnownOriginThePlayerGoesToTheMainLobby() {
        assertThat(route.resolve(Optional.empty())).isEqualTo(new Destination.Group("lobby"));
    }

    private void online(String id, String group, int players, int max) {
        servers.update(new ServerStatus(new ServerIdentity(id, group, ServerKind.LOBBY), players, max, true, clock.instant(), clock.instant()));
    }
}
