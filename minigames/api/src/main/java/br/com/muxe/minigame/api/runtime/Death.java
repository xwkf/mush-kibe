package br.com.muxe.minigame.api.runtime;

import br.com.muxe.minigame.api.match.Participant;
import java.util.Optional;

public record Death(Participant victim, Optional<Participant> killer, boolean finalKill, Cause cause) {

    public enum Cause {
        DAMAGE,
        VOID,
        QUIT,
        TIMEOUT
    }

    public boolean byVoid() {
        return cause == Cause.VOID;
    }

    public boolean whileOnline() {
        return cause == Cause.DAMAGE || cause == Cause.VOID;
    }
}
