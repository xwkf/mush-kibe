package br.com.muxe.minigame.bedwars.spectator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;

public record SpectatorHotbar(List<Entry> spectator) {

    public enum Action {
        TELEPORTER("teleporter"),
        RANDOM_PLAYER("random-player"),
        PLAYER_IN_COMBAT("player-in-combat"),
        PLAY_AGAIN("play-again"),
        LEAVE("leave");

        private final String key;

        Action(String key) {
            this.key = key;
        }
    }

    public record Entry(Action action, int slot, String material, String name) {
    }

    public static SpectatorHotbar from(ConfigurationSection section) {
        Objects.requireNonNull(section, "Missing configuration section hotbar.spectator");
        List<Entry> entries = new ArrayList<>();
        for (Action action : Action.values()) {
            ConfigurationSection item = Objects.requireNonNull(section.getConfigurationSection(action.key), "Missing hotbar.spectator." + action.key);
            entries.add(new Entry(action, item.getInt("slot"), item.getString("material", "STONE").toUpperCase(Locale.ROOT), item.getString("name", "")));
        }
        return new SpectatorHotbar(List.copyOf(entries));
    }

    public List<Entry> winner() {
        return spectator.stream().filter(entry -> entry.action() == Action.PLAY_AGAIN || entry.action() == Action.LEAVE).toList();
    }

    public Optional<Action> action(int slot, String material) {
        return spectator.stream().filter(entry -> entry.slot() == slot && entry.material().equals(material)).map(Entry::action).findFirst();
    }
}
