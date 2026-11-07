package br.com.muxe.minigame.api.runtime;

import br.com.muxe.minigame.api.match.Participant;

public record Playing<S extends MatchState>(MatchRuntime<S> runtime, Participant participant) {

    public S state() {
        return runtime.state();
    }
}
