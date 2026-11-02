package br.com.muxe.lobby.listener;

import java.util.Set;

public record VoidRule(Set<String> lobbyWorlds, int level) {

    public VoidRule {
        lobbyWorlds = Set.copyOf(lobbyWorlds);
    }

    public boolean sendsBack(String world, double y) {
        return y < level && lobbyWorlds.contains(world);
    }
}
