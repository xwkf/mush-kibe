package br.com.muxe.minigame.api.cosmetic.aura;

import br.com.muxe.minigame.api.match.MatchPhase;
import br.com.muxe.minigame.api.match.Participant;

public final class AuraRules {

    private AuraRules() {
    }

    public static boolean shows(MatchPhase phase, Participant.State state, boolean spectator, boolean invisible) {
        return phase == MatchPhase.PLAYING && state == Participant.State.ALIVE && !spectator && !invisible;
    }
}
