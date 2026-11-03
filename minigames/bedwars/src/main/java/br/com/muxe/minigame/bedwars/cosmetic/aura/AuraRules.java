package br.com.muxe.minigame.bedwars.cosmetic.aura;

import br.com.muxe.minigame.bedwars.room.Room;
import br.com.muxe.minigame.bedwars.room.RoomPlayer;

public final class AuraRules {

    private AuraRules() {
    }

    public static boolean shows(Room.Phase phase, RoomPlayer.State state, boolean spectator, boolean invisible) {
        return phase == Room.Phase.PLAYING && state == RoomPlayer.State.ALIVE && !spectator && !invisible;
    }
}
