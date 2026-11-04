package br.com.muxe.lobby.afk;

import br.com.muxe.core.bukkit.network.NetworkGateway;
import br.com.muxe.core.common.activity.IdleTracker;
import br.com.muxe.core.common.afk.AfkReturns;
import br.com.muxe.core.common.server.directory.ServerDirectory;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executor;

public final class AfkSender {

    private final IdleTracker idle;
    private final AfkReturns returns;
    private final ServerDirectory servers;
    private final NetworkGateway network;
    private final String here;
    private final AfkSettings settings;
    private final Executor async;

    public AfkSender(IdleTracker idle, AfkReturns returns, ServerDirectory servers, NetworkGateway network, String here, AfkSettings settings, Executor async) {
        this.idle = idle;
        this.returns = returns;
        this.servers = servers;
        this.network = network;
        this.here = here;
        this.settings = settings;
        this.async = async;
    }

    public void check(Collection<AfkCandidate> online) {
        if (!settings.enabled() || servers.leastLoaded(settings.serverGroup()).isEmpty()) {
            return;
        }
        online.stream()
            .filter(candidate -> !candidate.exempt())
            .map(AfkCandidate::id)
            .filter(idle::isIdle)
            .forEach(this::send);
    }

    private void send(UUID player) {
        idle.active(player);
        async.execute(() -> {
            returns.remember(player, here, settings.originTtl());
            network.connectToGroup(player, settings.serverGroup());
        });
    }
}
