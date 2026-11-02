package br.com.muxe.minigame.bedwars.matchmaking;

import br.com.muxe.minigame.bedwars.network.JoinRoomReply;
import java.util.List;

public final class JoinNotices {

    public static final String REQUESTING = "room-requesting";
    public static final String SENDING = "room-sending";
    public static final String CREATED = "room-created";

    private JoinNotices() {
    }

    public static List<String> beforeRequest(Matchmaker.Target target) {
        return target.createsRoom() ? List.of(REQUESTING) : List.of();
    }

    public static List<String> afterReply(JoinRoomReply reply) {
        return reply.created() ? List.of(SENDING, CREATED) : List.of(SENDING);
    }
}
