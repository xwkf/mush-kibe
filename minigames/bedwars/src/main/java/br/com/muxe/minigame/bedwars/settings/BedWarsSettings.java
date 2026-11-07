package br.com.muxe.minigame.bedwars.settings;

import br.com.muxe.minigame.api.timeline.Timeline.Entry;
import br.com.muxe.minigame.api.timeline.Timeline;
import br.com.muxe.minigame.bedwars.game.BedWarsEvent.Kind;
import br.com.muxe.minigame.bedwars.game.BedWarsEvent;
import br.com.muxe.minigame.bedwars.game.GeneratorTimer.Rate;
import br.com.muxe.minigame.bedwars.game.XpRewards;
import br.com.muxe.minigame.bedwars.item.ItemNames;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public record BedWarsSettings(
    Timings timings,
    Forges forges,
    Timeline<BedWarsEvent> timeline,
    XpRewards xp,
    Holograms holograms,
    MatchSounds sounds,
    ItemNames items
) {

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};

    public static BedWarsSettings from(FileConfiguration config) {
        Forges forges = Forges.from(section(config, "forges"));
        return new BedWarsSettings(
            Timings.from(section(config, "timings")),
            forges,
            timeline(forges, section(config, "events")),
            XpRewards.from(section(config, "xp-rewards")),
            Holograms.from(section(config, "holograms")),
            MatchSounds.from(config.getConfigurationSection("sounds")),
            ItemNames.from(config.getConfigurationSection("item-names"))
        );
    }

    public static String roman(int tier) {
        return tier > 0 && tier < ROMAN.length ? ROMAN[tier] : String.valueOf(tier);
    }

    static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + path);
    }

    private static Timeline<BedWarsEvent> timeline(Forges forges, ConfigurationSection events) {
        ConfigurationSection names = section(events, "names");
        List<Entry<BedWarsEvent>> list = new ArrayList<>();
        for (int tier = 2; tier <= forges.diamond().size(); tier++) {
            list.add(new Entry<>(new BedWarsEvent(Kind.DIAMOND_TIER, tier), forges.diamond().get(tier - 1).start(), names.getString("diamond", "Diamante {tier}").replace("{tier}", roman(tier))));
        }
        for (int tier = 2; tier <= forges.emerald().size(); tier++) {
            list.add(new Entry<>(new BedWarsEvent(Kind.EMERALD_TIER, tier), forges.emerald().get(tier - 1).start(), names.getString("emerald", "Esmeralda {tier}").replace("{tier}", roman(tier))));
        }
        list.add(new Entry<>(new BedWarsEvent(Kind.BEDS_DESTROYED, 0), events.getInt("beds-destroyed", 1800), names.getString("beds-destroyed", "Camas destruídas")));
        list.add(new Entry<>(new BedWarsEvent(Kind.DRAGONS, 0), events.getInt("dragons", 2400), names.getString("dragons", "Morte súbita")));
        list.add(new Entry<>(new BedWarsEvent(Kind.GAME_END, 0), events.getInt("game-end", 2520), names.getString("game-end", "Fim de jogo")));
        return new Timeline<>(list);
    }

    public record Timings(int islandRadius, int spawnRadius, int npcRadius, int generatorRadius) {

        static Timings from(ConfigurationSection section) {
            return new Timings(
                section.getInt("island-radius", 17),
                section.getInt("spawn-protection-radius", 5),
                section.getInt("npc-protection-radius", 1),
                section.getInt("generator-protection-radius", 1)
            );
        }
    }

    public record Tier(Rate rate, int start) {
    }

    public record Forges(boolean stackItems, double splitRadius, double ironDelay, int ironPerPlayer, int ironMax, int ironLimit,
                         Rate gold, Rate islandEmerald, List<Tier> diamond, List<Tier> emerald) {

        public Forges {
            diamond = List.copyOf(diamond);
            emerald = List.copyOf(emerald);
        }

        static Forges from(ConfigurationSection section) {
            ConfigurationSection island = section(section, "island");
            ConfigurationSection iron = section(island, "iron");
            return new Forges(
                section.getBoolean("stack-items", false),
                section.getDouble("split-radius", 2),
                iron.getDouble("delay", 0.9),
                iron.getInt("amount-per-player", 1),
                iron.getInt("max-amount", 4),
                iron.getInt("spawn-limit", 48),
                rate(section(island, "gold")),
                rate(section(island, "emerald")),
                tiers(section(section, "diamond")),
                tiers(section(section, "emerald"))
            );
        }

        public Rate iron(int teamSize) {
            return new Rate(ironDelay, Math.max(1, Math.min(ironMax, ironPerPlayer * teamSize)), ironLimit);
        }

        private static Rate rate(ConfigurationSection section) {
            return new Rate(section.getDouble("delay"), section.getInt("amount", 1), section.getInt("spawn-limit", 0));
        }

        private static List<Tier> tiers(ConfigurationSection section) {
            List<Tier> tiers = new ArrayList<>();
            for (String key : List.of("tierI", "tierII", "tierIII")) {
                ConfigurationSection tier = section.getConfigurationSection(key);
                if (tier != null) {
                    tiers.add(new Tier(rate(tier), tier.getInt("start", 0)));
                }
            }
            if (tiers.isEmpty()) {
                throw new IllegalArgumentException("Generator " + section.getCurrentPath() + " has no tiers");
            }
            return tiers;
        }
    }

    public record Holograms(List<String> shop, List<String> shopTeam, List<String> upgrades, List<String> upgradesTeam, List<String> diamond, List<String> emerald) {

        static Holograms from(ConfigurationSection section) {
            return new Holograms(
                section.getStringList("shop"),
                section.getStringList("shop-team"),
                section.getStringList("upgrades"),
                section.getStringList("upgrades-team"),
                section.getStringList("diamond"),
                section.getStringList("emerald")
            );
        }
    }
}
