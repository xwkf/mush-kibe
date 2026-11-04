package br.com.muxe.afk.route;

import br.com.muxe.afk.session.AfkSessions;
import br.com.muxe.core.bukkit.network.NetworkGateway;
import br.com.muxe.core.common.afk.AfkReturns;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class AfkReturner {

    private final AfkSessions sessions;
    private final AfkReturns returns;
    private final ReturnRoute route;
    private final NetworkGateway network;
    private final Executor async;

    public AfkReturner(AfkSessions sessions, AfkReturns returns, ReturnRoute route, NetworkGateway network, Executor async) {
        this.sessions = sessions;
        this.returns = returns;
        this.route = route;
        this.network = network;
        this.async = async;
    }

    public void arrived(UUID player) {
        sessions.arrive(player);
    }

    public void moved(UUID player) {
        if (!sessions.startReturn(player)) {
            return;
        }
        CompletableFuture.supplyAsync(() -> route.resolve(returns.originOf(player)), async)
            .thenCompose(destination -> connect(player, destination))
            .whenComplete((ignored, error) -> {
                if (error != null) {
                    sessions.returnFailed(player);
                }
            });
    }

    public void left(UUID player) {
        sessions.leave(player);
        CompletableFuture.runAsync(() -> returns.forget(player), async);
    }

    private CompletableFuture<Void> connect(UUID player, Destination destination) {
        return switch (destination) {
            case Destination.Server server -> network.connectToServer(player, server.id());
            case Destination.Group group -> network.connectToGroup(player, group.name());
        };
    }
}
