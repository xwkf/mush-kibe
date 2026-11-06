package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.minigame.bedwars.room.Room;
import br.com.muxe.minigame.bedwars.room.RoomPlayer;

public final class TrailRules {

    private TrailRules() {
    }

    public static boolean launches(Room.Phase phase, RoomPlayer.State state, boolean spectator) {
        return phase == Room.Phase.PLAYING && state == RoomPlayer.State.ALIVE && !spectator;
    }

    public static boolean flying(boolean valid, boolean onGround) {
        return valid && !onGround;
    }
}
