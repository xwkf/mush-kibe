package br.com.muxe.minigame.bedwars.lobby.top;

import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public record TopTexts(
    String header,
    String entry,
    String empty,
    String click,
    String periodSelected,
    String periodOther,
    String general,
    Map<StatsPeriod, String> periodNames,
    double labelAboveStand,
    int refreshSeconds,
    Optional<Sound> clickSound
) {

    public TopTexts {
        periodNames = Map.copyOf(periodNames);
    }

    public static TopTexts from(ConfigurationSection section) {
        Map<StatsPeriod, String> names = new EnumMap<>(StatsPeriod.class);
        ConfigurationSection configured = section.getConfigurationSection("period-names");
        for (StatsPeriod period : StatsPeriod.values()) {
            names.put(period, configured == null ? period.id() : configured.getString(period.name(), period.id()));
        }
        return new TopTexts(
            section.getString("header", "&b&lTOP 100&r &7({page}/{pages})"),
            section.getString("entry", "&e{position}.&r &f{name}&r &7- &e{value}&r"),
            section.getString("empty", "&e{position}.&r &f&7Ninguém&r &7- &e0&r"),
            section.getString("click", "&6&lClique para trocar!"),
            section.getString("period-selected", "&a&l{period}&r"),
            section.getString("period-other", "&7{period}&r"),
            section.getString("general", "GERAL"),
            names,
            section.getDouble("label-above-stand", 2.175),
            Math.max(10, section.getInt("refresh-seconds", 60)),
            sound(section.getString("click-sound", "CLICK"))
        );
    }

    public static Optional<Sound> sound(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Sound.valueOf(name.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException unknown) {
            return Optional.empty();
        }
    }
}
