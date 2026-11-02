package br.com.muxe.core.bukkit.buildmode;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.GameMode;

public final class GameModeLock {

    private final Set<UUID> released = ConcurrentHashMap.newKeySet();

    public void release(UUID playerId) {
        released.add(playerId);
    }

    public void lock(UUID playerId) {
        released.remove(playerId);
    }

    public boolean allows(UUID playerId, GameMode target, boolean building) {
        return released.contains(playerId) || BuildRules.allowsGameMode(target, building);
    }
}
