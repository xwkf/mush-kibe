package br.com.muxe.afk.route;

import br.com.muxe.core.common.server.directory.ServerDirectory;
import br.com.muxe.core.common.server.directory.ServerStatus;
import java.util.Optional;

public final class ReturnRoute {

    private final ServerDirectory servers;
    private final String fallbackGroup;

    public ReturnRoute(ServerDirectory servers, String fallbackGroup) {
        this.servers = servers;
        this.fallbackGroup = fallbackGroup;
    }

    public Destination resolve(Optional<String> origin) {
        return origin.flatMap(servers::find)
            .filter(ServerStatus::hasRoom)
            .<Destination>map(status -> new Destination.Server(status.identity().id()))
            .orElseGet(() -> new Destination.Group(fallbackGroup));
    }
}
