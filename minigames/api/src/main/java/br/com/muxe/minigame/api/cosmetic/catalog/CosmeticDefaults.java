package br.com.muxe.minigame.api.cosmetic.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class CosmeticDefaults {

    public static final String RESOURCE = "/cosmetics.yml";
    public static final String KILL_MESSAGES = "kill-messages";
    public static final String LIST = "cosmetics";
    public static final String HIDDEN = "hidden";

    private final ConfigurationSection defaults;

    private CosmeticDefaults(ConfigurationSection defaults) {
        this.defaults = defaults;
    }

    public static CosmeticDefaults load() {
        try (InputStream stream = Objects.requireNonNull(CosmeticDefaults.class.getResourceAsStream(RESOURCE), "Missing " + RESOURCE);
             Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new CosmeticDefaults(YamlConfiguration.loadConfiguration(reader));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    public static CosmeticDefaults of(ConfigurationSection defaults) {
        return new CosmeticDefaults(defaults);
    }

    public ConfigurationSection section(CosmeticKind kind, ConfigurationSection gameCosmetics) {
        ConfigurationSection override = gameCosmetics == null ? null : gameCosmetics.getConfigurationSection(kind.key());
        MemoryConfiguration merged = merge(defaults.getConfigurationSection(kind.key()), override);
        merged.set("category", null);
        ConfigurationSection list = merged.getConfigurationSection(LIST);
        if (list != null) {
            for (String id : list.getKeys(false)) {
                if (list.getBoolean(id + "." + HIDDEN, false)) {
                    list.set(id, null);
                }
            }
        }
        return merged;
    }

    public CosmeticCatalog catalog(String game, CosmeticKind kind, ConfigurationSection gameCosmetics, Predicate<String> animated) {
        return CosmeticCatalog.from(section(kind, gameCosmetics), LIST, kind.category(game), animated);
    }

    public ConfigurationSection killMessages(ConfigurationSection gameKillMessages) {
        return merge(defaults.getConfigurationSection(KILL_MESSAGES), gameKillMessages);
    }

    private static MemoryConfiguration merge(ConfigurationSection base, ConfigurationSection override) {
        MemoryConfiguration merged = new MemoryConfiguration();
        copy(base, merged);
        copy(override, merged);
        return merged;
    }

    private static void copy(ConfigurationSection from, MemoryConfiguration into) {
        if (from == null) {
            return;
        }
        for (String key : from.getKeys(true)) {
            if (!from.isConfigurationSection(key)) {
                into.set(key, from.get(key));
            }
        }
    }
}
