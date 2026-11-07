package br.com.muxe.minigame.api.runtime;

import br.com.muxe.minigame.api.match.MatchArena;
import br.com.muxe.minigame.engine.arena.LoadedArena;

public interface Minigame<A extends MatchArena, S extends MatchState> {

    String id();

    MatchRules rules();

    A arena(LoadedArena loaded);

    S open(MatchRuntime<S> runtime, A arena);

    default TeamPreferences teamPreferences() {
        return TeamPreferences.NONE;
    }
}
