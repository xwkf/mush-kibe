package br.com.muxe.minigame.bedwars.shop;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.QuickBuyTexts;
import org.junit.jupiter.api.Test;

class QuickBuyScreensTest {

    private final ShopCatalog catalog = ShopCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("shop"));

    @Test
    void theAddingScreenUsesTheExportedTexts() {
        QuickBuyTexts texts = catalog.quickBuyTexts();

        assertThat(texts.addingTitle()).isEqualTo("Adicionando à Compra Rápida...");
        assertThat(texts.replace()).containsExactly("&eClique para substituir!");
        assertThat(texts.emptyName()).isEqualTo("&cEspaço vazio!");
        assertThat(texts.emptyAdd()).containsExactly("&eClique para adicionar!");
    }

    @Test
    void theItemBeingAddedShowsItsDescriptionAndNoPrice() {
        assertThat(ShopService.addingLore(catalog.items().get("argila"), catalog.quickBuyTexts())).containsExactly(
            " ",
            "&7Bloco básico para proteger a sua",
            "&7cama.",
            " ",
            "&eAdicionando à compra rápida..."
        );
    }

    @Test
    void anItemWithoutDescriptionOnlyShowsThatItIsBeingAdded() {
        assertThat(ShopService.addingLore(catalog.items().get("maca-dourada"), catalog.quickBuyTexts()))
            .containsExactly(" ", "&eAdicionando à compra rápida...");
    }
}
