package br.com.muxe.minigame.bedwars.room;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record ChatView(Set<UUID> audience, String prefix) {

    public ChatView {
        audience = Set.copyOf(audience);
    }

    public static Map<UUID, ChatView> of(Room room, Set<UUID> online) {
        boolean teamsShown = room.phase() == Room.Phase.PLAYING || room.phase() == Room.Phase.ENDING;
        Map<UUID, ChatView> views = new HashMap<>();
        for (RoomPlayer player : room.players()) {
            TeamColor team = player.team();
            String prefix = teamsShown && team != null ? team.color() + "[" + team.letter() + "] " : "";
            views.put(player.id(), new ChatView(online, prefix));
        }
        return views;
    }
}
