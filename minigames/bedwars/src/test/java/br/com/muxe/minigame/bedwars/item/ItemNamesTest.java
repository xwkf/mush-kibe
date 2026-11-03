package br.com.muxe.minigame.bedwars.item;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ItemNamesTest {

    private final ItemNames names = RoomFixtures.SETTINGS.items();

    @Test
    void everyItemOfTheMatchHasItsPortugueseName() {
        assertThat(names.of(Material.IRON_INGOT)).contains("Barra de Ferro");
        assertThat(names.of(Material.GOLD_INGOT)).contains("Barra de Ouro");
        assertThat(names.of(Material.FIREBALL)).contains("Bola de Fogo");
        assertThat(names.of(Material.MAGMA_CREAM)).contains("Estilingue");
        assertThat(names.of(Material.WOOD_SWORD)).contains("Espada de Madeira");
        assertThat(names.of(Material.WOOL)).contains("Lã");
        assertThat(names.of(Material.LEATHER_HELMET)).contains("Capacete de Couro");
    }

    @Test
    void anItemWithoutANameReadsItsMaterial() {
        assertThat(names.of(Material.CAKE)).isEmpty();
        assertThat(ItemNames.readable(Material.CAKE)).isEqualTo("Cake");
    }

    @Test
    void storingAnItemSaysWhatAndHowManyWentIntoTheChest() {
        MessageCatalog messages = MessageCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("messages"));

        assertThat(messages.get("chest-deposited", "item", "Barra de Ferro", "amount", "4"))
            .isEqualTo("§eVocê guardou: §aBarra de Ferro (4x) §eem seu baú.");
        assertThat(messages.lines("chest-hologram")).containsExactly("§eBata para", "§eguardar!");
    }
}
