package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class BedWarsMenuSettingsTest {

    private final CosmeticMenuSettings settings = CosmeticMenuSettings.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus"));

    @Test
    void theBedWarsMenuHasFiveRowsWithCosmeticsInTheMiddle() {
        ItemBuilder cosmetics = settings.cosmetics().item();

        assertThat(settings.bedwarsTitle()).isEqualTo("Menu do Bed Wars");
        assertThat(settings.bedwarsRows()).isEqualTo(5);
        assertThat(settings.cosmetics().slot()).isEqualTo(13);
        assertThat(cosmetics.material()).isEqualTo(Material.RAW_FISH);
        assertThat(cosmetics.data()).isEqualTo((short) 3);
        assertThat(cosmetics.name()).isEqualTo("§aCosméticos");
        assertThat(cosmetics.lore()).containsExactly("§7Visualize os cosméticos", "§7disponíveis.", " ", "§eClique para ver mais.");
    }

    @Test
    void aComingSoonItemLeftInAnOlderFileNeverCoversAButtonThatWorks() throws InvalidConfigurationException {
        YamlConfiguration older = new YamlConfiguration();
        older.loadFromString(RoomFixtures.CONFIG.saveToString());
        older.set("cosmetics.menus.bedwars.soon.29.material", "CHEST");
        older.set("cosmetics.menus.bedwars.soon.29.name", "&aCompra rápida");
        older.set("cosmetics.menus.bedwars.soon.11.material", "STORAGE_MINECART");

        CosmeticMenuSettings fromOlderFile = CosmeticMenuSettings.from(older.getConfigurationSection("cosmetics.menus"));

        assertThat(fromOlderFile.quickBuy().orElseThrow().slot()).isEqualTo(29);
        assertThat(fromOlderFile.bedwarsSoon()).containsOnlyKeys(31, 33);
    }

    @Test
    void theOtherBedWarsMenuItemsFollowTheReference() {
        assertThat(settings.bedwarsItems()).containsOnlyKeys(15);
        assertThat(settings.bedwarsSoon()).containsOnlyKeys(31, 33);
        assertThat(settings.missions().orElseThrow().slot()).isEqualTo(11);
        assertThat(settings.missions().orElseThrow().item().material()).isEqualTo(Material.STORAGE_MINECART);
        assertThat(settings.missions().orElseThrow().item().name()).isEqualTo("§aMissões");
        assertThat(settings.missions().orElseThrow().item().lore()).containsExactly("§7Complete as missões", "§7e ganhe recompensas.", " ", "§eClique para ver mais.");
        assertThat(settings.bedwarsItems().get(15).material()).isEqualTo(Material.PAPER);
        assertThat(settings.bedwarsItems().get(15).name()).isEqualTo("§aLista de Níveis");
        assertThat(settings.bedwarsItems().get(15).lore()).containsExactly(
            " ",
            "§7[0§l✫§7] §a[10§l✶§a] §b[20§l✽§b]",
            "§d[30§l✿§d] §e[40§l✸§e] §6[50§l✹§6]",
            "§5[60§l✣§5] §2[70§l✦§2] §9[80§l✵§9]",
            "§c[90§l❊§c] §4[100§l❤§4] §e[§6200✪§e]",
            "§b[§3300❂§b] §d[§5400❁§d] §7[§f500❋§7]",
            "§1[§9600✟§1] §2[§a700§l۞§2] §6[§e800§l✌§6]",
            "§4[§c900§l⚝§4] §c[§61§e0§a0§b0§dΩ§5]");
        assertThat(settings.quickBuy().orElseThrow().slot()).isEqualTo(29);
        assertThat(settings.quickBuy().orElseThrow().item().material()).isEqualTo(Material.CHEST);
        assertThat(settings.quickBuy().orElseThrow().item().name()).isEqualTo("§aCompra rápida");
        assertThat(settings.bedwarsSoon().get(31).material()).isEqualTo(Material.BLAZE_POWDER);
        assertThat(settings.bedwarsSoon().get(31).name()).isEqualTo("§aEditor de hotbar");
        assertThat(settings.bedwarsSoon().get(33).material()).isEqualTo(Material.DIODE);
        assertThat(settings.bedwarsSoon().get(33).name()).isEqualTo("§aPreferências");
        assertThat(settings.bedwarsSoon().get(33).lore()).containsExactly("§7Altere as preferências", "§7do modo.", " ", "§eClique para ver mais.");
    }
}
