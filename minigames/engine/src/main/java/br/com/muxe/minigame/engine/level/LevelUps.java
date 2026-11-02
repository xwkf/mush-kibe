package br.com.muxe.minigame.engine.level;

import br.com.muxe.core.bukkit.text.Colors;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import org.bukkit.entity.Player;

public final class LevelUps {

    private final String message;
    private final IntFunction<String> badge;
    private final Map<UUID, Integer> levels = new ConcurrentHashMap<>();

    public LevelUps(String message, IntFunction<String> badge) {
        this.message = message;
        this.badge = badge;
    }

    public Optional<String> reach(UUID playerId, int level) {
        Integer before = levels.get(playerId);
        if (before != null && level <= before) {
            return Optional.empty();
        }
        levels.put(playerId, level);
        if (before == null) {
            return Optional.empty();
        }
        return Optional.of(Colors.translate(message.replace("{level}", badge.apply(level))));
    }

    public void announce(Player player, int level) {
        reach(player.getUniqueId(), level).ifPresent(player::sendMessage);
    }

    public void forget(UUID playerId) {
        levels.remove(playerId);
    }
}
