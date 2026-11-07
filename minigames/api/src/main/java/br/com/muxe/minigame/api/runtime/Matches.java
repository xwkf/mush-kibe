package br.com.muxe.minigame.api.runtime;

import br.com.muxe.minigame.api.match.MatchPhase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;

public interface Matches<S extends MatchState> {

    Optional<MatchRuntime<S>> of(UUID player);

    Optional<MatchRuntime<S>> at(Location location);

    boolean isMatchWorld(Location location);

    List<MatchRuntime<S>> all();

    default Optional<Playing<S>> playing(UUID player) {
        return of(player).flatMap(runtime -> runtime.match().player(player)
            .filter(participant -> participant.isAlive() && runtime.match().phase() == MatchPhase.PLAYING)
            .map(participant -> new Playing<>(runtime, participant)));
    }
}
