package br.com.muxe.minigame.bedwars.room;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record ChatView(Set<UUID> team, Set<UUID> everyone, String prefix) {

    public ChatView {
        team = Set.copyOf(team);
        everyone = Set.copyOf(everyone);
    }

    public Set<UUID> audience(boolean global) {
        return global ? everyone : team;
    }

    public boolean teamOnly() {
        return !team.equals(everyone);
    }

    public static Map<UUID, ChatView> of(Room room, Set<UUID> online) {
        boolean teamsShown = room.phase() == Room.Phase.PLAYING || room.phase() == Room.Phase.ENDING;
        boolean teamChat = room.phase() == Room.Phase.PLAYING && room.mode().teamSize() > 1;
        Map<UUID, ChatView> views = new HashMap<>();
        for (RoomPlayer player : room.players()) {
            TeamColor team = player.team();
            String prefix = teamsShown && team != null ? team.color() + "[" + team.letter() + "] " : "";
            Set<UUID> audience = teamChat && team != null ? mates(room, team, online) : online;
            views.put(player.id(), new ChatView(audience, online, prefix));
        }
        return views;
    }

    private static Set<UUID> mates(Room room, TeamColor team, Set<UUID> online) {
        Set<UUID> mates = new HashSet<>();
        for (RoomPlayer member : room.members(team)) {
            if (online.contains(member.id())) {
                mates.add(member.id());
            }
        }
        return mates;
    }
}
