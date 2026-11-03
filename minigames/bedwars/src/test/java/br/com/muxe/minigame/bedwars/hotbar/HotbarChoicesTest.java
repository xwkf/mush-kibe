package br.com.muxe.minigame.bedwars.hotbar;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarEntry;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.Category;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.ShopItem;
import org.junit.jupiter.api.Test;

class HotbarChoicesTest {

    private final ShopCatalog catalog = ShopCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("shop"));
    private final HotbarChoices choices = new HotbarChoices(catalog);

    @Test
    void everyCategoryExceptTheArmorOneCanBeChosenInShopOrder() {
        assertThat(choices.categories()).extracting(Category::id)
            .containsExactly("blocos", "combate", "ferramentas", "arcos", "pocoes", "utilidades");
    }

    @Test
    void everyItemThatIsNotArmorCanBeChosenInShopOrderWithoutRepeats() {
        assertThat(choices.items()).hasSize(34).extracting(ShopItem::id).doesNotHaveDuplicates()
            .startsWith("la", "argila", "vidro", "pedra-do-fim", "escada", "madeira", "espada-pedra")
            .doesNotContain("armadura-malha", "armadura-ferro", "armadura-diamante")
            .endsWith("rastreador", "estilingue");
        assertThat(choices.items()).noneMatch(item -> item.type() == ShopCatalog.Type.ARMOR);
    }

    @Test
    void anEntryExistsOnlyWhileTheShopStillSellsIt() {
        assertThat(choices.exists(HotbarEntry.category("blocos"))).isTrue();
        assertThat(choices.exists(HotbarEntry.item("la"))).isTrue();
        assertThat(choices.exists(HotbarEntry.category("armadura"))).isFalse();
        assertThat(choices.exists(HotbarEntry.item("armadura-ferro"))).isFalse();
        assertThat(choices.exists(HotbarEntry.item("inexistente"))).isFalse();
        assertThat(choices.exists(HotbarEntry.category("la"))).isFalse();
    }

    @Test
    void anItemKnowsTheCategoriesItBelongsTo() {
        assertThat(choices.categoriesOf("la")).containsExactly("blocos");
        assertThat(choices.categoriesOf("picareta")).containsExactly("ferramentas");
        assertThat(choices.categoriesOf("inexistente")).isEmpty();
    }
}
