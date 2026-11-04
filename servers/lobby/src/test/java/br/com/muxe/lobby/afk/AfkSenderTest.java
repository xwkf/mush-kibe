package br.com.muxe.lobby.afk;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.network.NetworkGateway;
import br.com.muxe.core.common.activity.IdleTracker;
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

class AfkSenderTest {

    private static final Duration IDLE_AFTER = Duration.ofMinutes(5);
    private static final Duration ORIGIN_TTL = Duration.ofHours(12);

    private final MutableClock clock = MutableClock.startingAt("2026-11-03T20:00:00Z");
    private final TestNetwork network = new TestNetwork();
    private final ServerDirectory servers = new ServerDirectory(clock, Duration.ofDays(1));
    private final IdleTracker idle = new IdleTracker(clock, IDLE_AFTER);
    private final AfkReturns returns = new AfkReturns(new InMemoryExpiringStore(clock));
    private final List<ConnectPlayerPacket> proxyReceived = new CopyOnWriteArrayList<>();
    private final UUID player = UUID.randomUUID();
    private AfkSender sender;

    @BeforeEach
    void setUp() {
        TestNetwork.Node proxy = network.node("proxy-1", "proxy", ServerKind.PROXY);
        proxy.bus().subscribe(ConnectPlayerPacket.TYPE, context -> proxyReceived.add(context.packet()));
        proxy.start();
        sender = sender("bedwars-lobby-1", "bedwars-lobby", true);
        afkServerOnline(true);
    }

    @AfterEach
    void tearDown() {
        network.close();
    }

    @Test
    void aPlayerStandingStillForFiveMinutesGoesToTheAfkServerRememberingWhereTheyWere() {
        idle.active(player);
        clock.advance(IDLE_AFTER);

        sender.check(List.of(new AfkCandidate(player, false)));

        assertThat(proxyReceived).containsExactly(ConnectPlayerPacket.toGroup(player, "afk"));
        assertThat(returns.originOf(player)).contains("bedwars-lobby-1");
    }

    @Test
    void aPlayerWhoMovedRecentlyStays() {
        idle.active(player);
        clock.advance(IDLE_AFTER.minusSeconds(1));

        sender.check(List.of(new AfkCandidate(player, false)));

        assertThat(proxyReceived).isEmpty();
        assertThat(returns.originOf(player)).isEmpty();
    }

    @Test
    void vipsAreNeverSent() {
        idle.active(player);
        clock.advance(IDLE_AFTER.multipliedBy(3));

        sender.check(List.of(new AfkCandidate(player, true)));

        assertThat(proxyReceived).isEmpty();
    }

    @Test
    void withoutAnAfkServerWithRoomThePlayerStays() {
        afkServerOnline(false);
        idle.active(player);
        clock.advance(IDLE_AFTER);

        sender.check(List.of(new AfkCandidate(player, false)));

        assertThat(proxyReceived).isEmpty();
        assertThat(returns.originOf(player)).isEmpty();
    }

    @Test
    void aPlayerIsSentOnceAndTheCountStartsAgainIfTheyAreStillHere() {
        idle.active(player);
        clock.advance(IDLE_AFTER);

        sender.check(List.of(new AfkCandidate(player, false)));
        clock.advance(Duration.ofSeconds(1));
        sender.check(List.of(new AfkCandidate(player, false)));

        assertThat(proxyReceived).hasSize(1);

        clock.advance(IDLE_AFTER);
        sender.check(List.of(new AfkCandidate(player, false)));

        assertThat(proxyReceived).hasSize(2);
    }

    @Test
    void turnedOffNobodyIsSent() {
        AfkSender off = sender("lobby-9", "lobby", false);
        idle.active(player);
        clock.advance(IDLE_AFTER);

        off.check(List.of(new AfkCandidate(player, false)));

        assertThat(proxyReceived).isEmpty();
    }

    private AfkSender sender(String id, String group, boolean enabled) {
        NetworkGateway gateway = new NetworkGateway(network.node(id, group, ServerKind.LOBBY).start().bus());
        AfkSettings settings = new AfkSettings(enabled, IDLE_AFTER, "afk", Duration.ofSeconds(1), ORIGIN_TTL);
        return new AfkSender(idle, returns, servers, gateway, id, settings, Runnable::run);
    }

    private void afkServerOnline(boolean joinable) {
        servers.update(new ServerStatus(new ServerIdentity("afk-1", "afk", ServerKind.AFK), 0, 500, joinable, clock.instant(), clock.instant()));
    }
}
