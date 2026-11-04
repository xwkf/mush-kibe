package br.com.muxe.afk.route;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.afk.session.AfkSessions;
import br.com.muxe.core.bukkit.network.NetworkGateway;
import br.com.muxe.core.common.afk.AfkReturns;
import br.com.muxe.core.common.network.player.ConnectPlayerPacket;
import br.com.muxe.core.common.server.ServerIdentity;
import br.com.muxe.core.common.server.ServerKind;
import br.com.muxe.core.common.server.directory.ServerDirectory;
import br.com.muxe.core.common.server.directory.ServerStatus;
import br.com.muxe.core.common.testing.InMemoryExpiringStore;
import br.com.muxe.core.common.testing.MutableClock;
import br.com.muxe.core.common.testing.TestNetwork;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AfkReturnerTest {

    private final MutableClock clock = MutableClock.startingAt("2026-11-03T20:00:00Z");
    private final TestNetwork network = new TestNetwork();
    private final ServerDirectory servers = new ServerDirectory(clock, Duration.ofDays(1));
    private final AfkSessions sessions = new AfkSessions(clock, Duration.ofSeconds(1), Duration.ofSeconds(5));
    private final AfkReturns returns = new AfkReturns(new InMemoryExpiringStore(clock));
    private final List<ConnectPlayerPacket> proxyReceived = new CopyOnWriteArrayList<>();
    private final UUID player = UUID.randomUUID();
    private AfkReturner returner;

    @BeforeEach
    void setUp() {
        TestNetwork.Node proxy = network.node("proxy-1", "proxy", ServerKind.PROXY);
        proxy.bus().subscribe(ConnectPlayerPacket.TYPE, context -> proxyReceived.add(context.packet()));
        proxy.start();
        NetworkGateway gateway = new NetworkGateway(network.node("afk-1", "afk", ServerKind.AFK).start().bus());
        returner = new AfkReturner(sessions, returns, new ReturnRoute(servers, "lobby"), gateway, Runnable::run);
        servers.update(new ServerStatus(new ServerIdentity("lobby-2", "lobby", ServerKind.LOBBY), 10, 100, true, clock.instant(), clock.instant()));
    }

    @AfterEach
    void tearDown() {
        network.close();
    }

    @Test
    void movingSendsThePlayerBackToTheServerTheyCameFrom() {
        returns.remember(player, "lobby-2", Duration.ofHours(12));
        sessions.arrive(player);
        clock.advance(Duration.ofSeconds(2));

        returner.moved(player);
        returner.moved(player);

        assertThat(proxyReceived).containsExactly(ConnectPlayerPacket.toServer(player, "lobby-2"));
    }

    @Test
    void movingWithoutAKnownOriginSendsThePlayerToTheMainLobby() {
        sessions.arrive(player);
        clock.advance(Duration.ofSeconds(2));

        returner.moved(player);

        assertThat(proxyReceived).containsExactly(ConnectPlayerPacket.toGroup(player, "lobby"));
    }

    @Test
    void movingDuringTheArrivalGraceDoesNothing() {
        returns.remember(player, "lobby-2", Duration.ofHours(12));
        sessions.arrive(player);

        returner.moved(player);

        assertThat(proxyReceived).isEmpty();
    }
}
