package br.com.muxe.minigame.bedwars.shop;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.ShopItem;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class QuickBuyEditorTest {

    private final ShopCatalog catalog = ShopCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("shop"));
    private final QuickBuyEditor.Settings settings = QuickBuyEditor.Settings.from(RoomFixtures.CONFIG.getConfigurationSection("shop.quick-buy-editor"));

    @Test
    void theEditorUsesTheExportedTitlesAndButtons() {
        assertThat(settings.title()).isEqualTo("Editando compra rápida...");
        assertThat(settings.pickerTitle()).isEqualTo("Selecionando item... ({page}/{pages})");
        assertThat(settings.rows()).isEqualTo(5);
        assertThat(settings.back().slot()).isEqualTo(39);
        assertThat(settings.importButton().slot()).isEqualTo(44);
        assertThat(settings.next().slot()).isEqualTo(41);
        assertThat(QuickBuyEditor.SLOTS).hasSize(21).startsWith(10, 11).endsWith(33, 34);
    }

    @Test
    void anItemInTheQuickBuyCanBeSwappedOrRemoved() {
        assertThat(QuickBuyEditor.slotLore(catalog.items().get("argila").editor().lore(), settings)).containsExactly(
            " ",
            "&7Bloco básico para proteger a sua",
            "&7cama.",
            " ",
            "&eBotão esquerdo: &fTrocar",
            "&eBotão direito: &fRemover"
        );
        assertThat(QuickBuyEditor.slotLore(catalog.items().get("maca-dourada").editor().lore(), settings))
            .containsExactly(" ", "&eBotão esquerdo: &fTrocar", "&eBotão direito: &fRemover");
    }

    @Test
    void theLobbyShowsToolsWithoutTheirLevelAndKeepsItsOwnLineBreaks() {
        ShopItem pickaxe = catalog.items().get("picareta");
        assertThat(pickaxe.editor().name()).isEqualTo("Picareta");
        assertThat(pickaxe.editor().material()).isEqualTo(Material.DIAMOND_PICKAXE);
        assertThat(pickaxe.editor().lore()).isEmpty();
        assertThat(catalog.items().get("machado").editor().material()).isEqualTo(Material.DIAMOND_AXE);

        assertThat(QuickBuyEditor.pickerLore(catalog.items().get("bola-de-fogo").editor().lore(), settings)).containsExactly(
            " ",
            "&7Use Direito para lançar! Ótimo para",
            "&7empurrar jogadores fazendo ponte.",
            " ",
            "&eClique para selecionar."
        );
        assertThat(catalog.items().get("bola-de-fogo").description()).containsExactly(
            "&7Use Direito para lançar! Ótimo", "&7para empurrar jogadores fazendo", "&7ponte.");
    }

    @Test
    void thePickerListsEveryItemTheShopSellsInCategoryOrder() {
        assertThat(catalog.sellable()).hasSize(37).startsWith("la", "argila", "vidro", "pedra-do-fim").endsWith("rastreador", "estilingue")
            .doesNotHaveDuplicates();
        assertThat(catalog.items().get("argila").editor().name()).isEqualTo("Argila");
    }
}
