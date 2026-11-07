package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.minigame.api.match.MatchPhase;
import br.com.muxe.minigame.api.match.Participant;

public final class TrailRules {

    private TrailRules() {
    }

    public static boolean launches(MatchPhase phase, Participant.State state, boolean spectator) {
        return phase == MatchPhase.PLAYING && state == Participant.State.ALIVE && !spectator;
    }

    public static boolean flying(boolean valid, boolean onGround) {
        return valid && !onGround;
    }
}
