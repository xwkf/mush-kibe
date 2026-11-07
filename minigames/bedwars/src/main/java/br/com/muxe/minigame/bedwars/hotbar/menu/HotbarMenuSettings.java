package br.com.muxe.minigame.bedwars.hotbar.menu;

import br.com.muxe.minigame.api.cosmetic.menu.MenuSlot;
import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record HotbarMenuSettings(int frameTicks, String bedwarsMenu, String shop, Editing editing, Adding adding, Removing removing) {

    public static HotbarMenuSettings from(ConfigurationSection section) {
        ConfigurationSection destinations = section(section, "destinations");
        return new HotbarMenuSettings(
            section.getInt("frame-ticks", 20),
            destinations.getString("bedwars-menu"),
            destinations.getString("shop"),
            Editing.from(section(section, "editing")),
            Adding.from(section(section, "adding")),
            Removing.from(section(section, "removing"))
        );
    }

    public record Editing(String title, int rows, String slotName, String selectedOne, String selectedMany, String entryShown,
                          String entryOther, String add, String remove, MenuSlot back) {

        static Editing from(ConfigurationSection section) {
            return new Editing(
                section.getString("title"),
                section.getInt("rows", 3),
                section.getString("slot-name"),
                section.getString("selected-one"),
                section.getString("selected-many"),
                section.getString("entry-shown"),
                section.getString("entry-other"),
                section.getString("add"),
                section.getString("remove"),
                MenuSlot.from(section(section, "back"))
            );
        }
    }

    public record Adding(String title, int rows, String itemName, String categoryName, List<String> categoryDescription, String add,
                         String otherTab, MenuSlot categoriesTab, MenuSlot itemsTab, MenuSlot back, MenuSlot previous, MenuSlot next) {

        public Adding {
            categoryDescription = List.copyOf(categoryDescription);
        }

        static Adding from(ConfigurationSection section) {
            return new Adding(
                section.getString("title"),
                section.getInt("rows", 6),
                section.getString("item-name"),
                section.getString("category-name"),
                section.getStringList("category-description"),
                section.getString("add"),
                section.getString("other-tab"),
                MenuSlot.from(section(section, "categories-tab")),
                MenuSlot.from(section(section, "items-tab")),
                MenuSlot.from(section(section, "back")),
                MenuSlot.from(section(section, "previous")),
                MenuSlot.from(section(section, "next"))
            );
        }
    }

    public record Removing(String title, int rows, String remove, MenuSlot back) {

        static Removing from(ConfigurationSection section) {
            return new Removing(section.getString("title"), section.getInt("rows", 4), section.getString("remove"), MenuSlot.from(section(section, "back")));
        }
    }

    private static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + parent.getCurrentPath() + "." + path);
    }
}
