package br.com.muxe.minigame.bedwars.item;

import br.com.muxe.core.bukkit.text.Colors;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemNames {

    public static final String NAME_COLOR = "&f";

    private final Map<Material, String> names;

    public ItemNames(Map<Material, String> names) {
        this.names = names.isEmpty() ? Map.of() : new EnumMap<>(names);
    }

    public static ItemNames from(ConfigurationSection section) {
        Map<Material, String> names = new EnumMap<>(Material.class);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material == null) {
                    throw new IllegalArgumentException("Unknown material in item-names: " + key);
                }
                names.put(material, section.getString(key));
            }
        }
        return new ItemNames(names);
    }

    public Optional<String> of(Material material) {
        return Optional.ofNullable(names.get(material));
    }

    public ItemStack named(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }
        return of(item.getType()).map(name -> named(item, name)).orElse(item);
    }

    public ItemStack named(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.hasDisplayName()) {
            return item;
        }
        meta.setDisplayName(Colors.translate(NAME_COLOR + name));
        item.setItemMeta(meta);
        return item;
    }

    public String display(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return ChatColor.stripColor(meta.getDisplayName());
        }
        return of(item.getType()).orElseGet(() -> readable(item.getType()));
    }

    static String readable(Material material) {
        String words = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }
}
