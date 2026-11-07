package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.api.cosmetic.catalog.Availability;
import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticIcons;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticListSettings;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings.CategoryMenu;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticAccess;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticState;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class WoodTypeCosmeticTest {

    private final CosmeticCatalog catalog = CosmeticCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.wood-type"), "cosmetics", "bedwars-madeira", id -> true);
    private final CosmeticMenuSettings menus = CosmeticMenuSettings.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus"));
    private final CategoryMenu menu = menus.category(CosmeticMenuSettings.WOOD_TYPE);
    private final CosmeticIcons icons = new CosmeticIcons(menu, catalog);
    private final CosmeticListSettings list = menu.list();
    private final CosmeticWallet ultra = new CosmeticWallet(Set.of(), "pinheiro", 0, CosmeticAccess.ULTRA);

    private ItemBuilder icon(String id) {
        return icons.cosmetic(catalog.find(id).orElseThrow(), ultra);
    }

    @Test
    void theCatalogHasTheTwelveWoodsInTheRecordedOrder() {
        assertThat(catalog.category()).isEqualTo("bedwars-madeira");
        assertThat(catalog.defaultId()).contains("carvalho");
        assertThat(catalog.all().stream().map(Cosmetic::id)).containsExactly(
            "carvalho", "pinheiro", "eucalipto", "selva", "acacia", "carvalho-escuro",
            "tronco-carvalho", "tronco-pinheiro", "tronco-eucalipto", "tronco-selva", "tronco-acacia", "tronco-carvalho-escuro");
        assertThat(catalog.all().stream().map(cosmetic -> cosmetic.availability())).containsExactly(
            Availability.DEFAULT, Availability.VIP, Availability.VIP, Availability.ULTRA, Availability.ULTRA, Availability.ULTRA_PLUS,
            Availability.ULTRA_PLUS, Availability.EVENT, Availability.EVENT, Availability.EVENT, Availability.EVENT, Availability.EVENT);
        assertThat(catalog.all()).allMatch(cosmetic -> cosmetic.stars() == 0);
    }

    @Test
    void theCategoryButtonReplacesTheComingSoonItem() {
        assertThat(menu.button().slot()).isEqualTo(29);
        assertThat(menus.soon()).doesNotContainKey(29);
        assertThat(menu.button().item().material()).isEqualTo(Material.WOOD);
        assertThat(menu.button().item().name()).isEqualTo("§aTipos de madeira");
        assertThat(icons.category(ultra).lore()).containsExactly(
            "§85/12 (41%)", " ", "§7Customize o tipo de", "§7madeira de sua loja.", " ", "§7Selecionado: §aMadeira de pinheiro", " ", "§eClique para ver mais.");
    }

    @Test
    void theListHasTheRecordedTitleSlotsAndNeitherNoneNorRandom() {
        assertThat(list.title()).isEqualTo("Tipos de madeira ({page}/{pages})");
        assertThat(list.rows()).isEqualTo(6);
        assertThat(list.slots().subList(0, 12)).containsExactly(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23);
        assertThat(list.entries().none()).isFalse();
        assertThat(list.entries().random()).isFalse();
        assertThat(list.entries().ownedFirst()).isFalse();
        assertThat(list.entries().previews()).isFalse();
        assertThat(list.entries().entryIndex(0, 12)).isZero();
        assertThat(list.entries().entryIndex(11, 12)).isEqualTo(11);
    }

    @Test
    void oakIsTheOpenOptionEveryoneOwns() {
        ItemBuilder oak = icon("carvalho");

        assertThat(oak.material()).isEqualTo(Material.WOOD);
        assertThat(oak.data()).isZero();
        assertThat(oak.name()).isEqualTo("§aMadeira de carvalho");
        assertThat(oak.hidesFlags()).isTrue();
        assertThat(oak.isGlowing()).isFalse();
        assertThat(oak.lore()).containsExactly(" ", "§eBotão esquerdo: §fSelecionar");
    }

    @Test
    void theSelectedWoodGlowsAndShowsTheRankThatIncludesIt() {
        ItemBuilder spruce = icon("pinheiro");

        assertThat(spruce.material()).isEqualTo(Material.WOOD);
        assertThat(spruce.data()).isEqualTo((short) 1);
        assertThat(spruce.name()).isEqualTo("§aMadeira de pinheiro");
        assertThat(spruce.isGlowing()).isTrue();
        assertThat(spruce.lore()).containsExactly(" ", "§7Incluso no §a§lVIP", " ", "§aJá selecionado.");
    }

    @Test
    void theOtherPlanksAreIncludedInTheirRanks() {
        assertThat(icon("eucalipto").lore()).containsExactly(" ", "§7Incluso no §a§lVIP", " ", "§eBotão esquerdo: §fSelecionar");
        assertThat(icon("eucalipto").data()).isEqualTo((short) 2);
        assertThat(icon("selva").lore()).containsExactly(" ", "§7Incluso no §d§lULTRA", " ", "§eBotão esquerdo: §fSelecionar");
        assertThat(icon("selva").name()).isEqualTo("§aMadeira da selva");
        assertThat(icon("selva").data()).isEqualTo((short) 3);
        assertThat(icon("acacia").lore()).containsExactly(" ", "§7Incluso no §d§lULTRA", " ", "§eBotão esquerdo: §fSelecionar");
        assertThat(icon("acacia").name()).isEqualTo("§aMadeira de acácia");
        assertThat(icon("acacia").data()).isEqualTo((short) 4);
    }

    @Test
    void theUltraPlusWoodsAreLockedForAnUltraPlayer() {
        ItemBuilder darkOak = icon("carvalho-escuro");
        ItemBuilder oakLog = icon("tronco-carvalho");

        assertThat(darkOak.material()).isEqualTo(Material.WOOD);
        assertThat(darkOak.data()).isEqualTo((short) 5);
        assertThat(darkOak.name()).isEqualTo("§cMadeira de carvalho escuro");
        assertThat(darkOak.lore()).containsExactly(" ", "§7Incluso no §d§lULTRA§6+", " ", "§cIndisponível.");
        assertThat(oakLog.material()).isEqualTo(Material.LOG);
        assertThat(oakLog.data()).isZero();
        assertThat(oakLog.name()).isEqualTo("§cTronco de carvalho");
        assertThat(oakLog.lore()).containsExactly(" ", "§7Incluso no §d§lULTRA§6+", " ", "§cIndisponível.");
    }

    @Test
    void theLogsNobodyOwnsAreUnavailableAndOneIsATournamentPrize() {
        assertThat(icon("tronco-pinheiro").lore()).containsExactly(" ", "§cIndisponível.");
        assertThat(icon("tronco-pinheiro").material()).isEqualTo(Material.LOG);
        assertThat(icon("tronco-pinheiro").data()).isEqualTo((short) 1);
        assertThat(icon("tronco-eucalipto").lore()).containsExactly(" ", "§6Premiação de torneio", " ", "§cIndisponível.");
        assertThat(icon("tronco-eucalipto").data()).isEqualTo((short) 2);
        assertThat(icon("tronco-selva").name()).isEqualTo("§cTronco de árvore da selva");
        assertThat(icon("tronco-selva").lore()).containsExactly(" ", "§cIndisponível.");
        assertThat(icon("tronco-selva").data()).isEqualTo((short) 3);
        assertThat(icon("tronco-acacia").material()).isEqualTo(Material.LOG_2);
        assertThat(icon("tronco-acacia").data()).isZero();
        assertThat(icon("tronco-acacia").name()).isEqualTo("§cTronco de acácia");
        assertThat(icon("tronco-carvalho-escuro").material()).isEqualTo(Material.LOG_2);
        assertThat(icon("tronco-carvalho-escuro").data()).isEqualTo((short) 1);
        assertThat(icon("tronco-carvalho-escuro").name()).isEqualTo("§cTronco de carvalho escuro");
        assertThat(catalog.all().stream().map(cosmetic -> icons.cosmetic(cosmetic, ultra)).filter(item -> !item.hidesFlags())).isEmpty();
    }

    @Test
    void theSelectedItemShowsTheIconOfTheSelectedWood() {
        ItemBuilder selected = icons.selected(ultra);

        assertThat(selected.material()).isEqualTo(Material.WOOD);
        assertThat(selected.data()).isEqualTo((short) 1);
        assertThat(selected.name()).isEqualTo("§eSelecionado: §7Madeira de pinheiro");
        assertThat(selected.lore()).containsExactly("§7");
        assertThat(selected.hidesFlags()).isFalse();
        assertThat(selected.isGlowing()).isFalse();
        assertThat(list.selected().slot()).isEqualTo(49);
    }

    @Test
    void theSelectedItemOfALogShowsTheLog() {
        ItemBuilder selected = icons.selected(ultra.selecting("tronco-acacia"));

        assertThat(selected.material()).isEqualTo(Material.LOG_2);
        assertThat(selected.data()).isZero();
        assertThat(selected.name()).isEqualTo("§eSelecionado: §7Tronco de acácia");
    }

    @Test
    void backGoesToTheCosmeticsMenu() {
        assertThat(list.back().slot()).isEqualTo(48);
        assertThat(list.back().item().material()).isEqualTo(Material.ARROW);
        assertThat(list.back().item().name()).isEqualTo("§aVoltar");
        assertThat(list.back().item().lore()).containsExactly("§7Para Cosméticos do Bed Wars");
        assertThat(list.back().item().hidesFlags()).isFalse();
    }

    @Test
    void nothingSavedMeansOakSelectedBecauseItIsTheDefault() {
        CosmeticWallet fresh = CosmeticWallet.loaded(Set.of(), catalog.defaultId().orElseThrow(), 0, CosmeticAccess.REGULAR, catalog);

        assertThat(fresh.state(catalog.find("carvalho").orElseThrow())).isEqualTo(CosmeticState.SELECTED);
        assertThat(icons.selectedName(fresh)).isEqualTo("Madeira de carvalho");
        assertThat(icons.selected(fresh).name()).isEqualTo("§eSelecionado: §7Madeira de carvalho");
        assertThat(icons.selected(fresh).material()).isEqualTo(Material.WOOD);
    }

    @Test
    void aStoredRandomSelectionIsShownAsOakBecauseThisCategoryHasNoRandom() {
        assertThat(icons.selectedName(CosmeticWallet.EMPTY)).isEqualTo("Madeira de carvalho");
        assertThat(icons.cosmetic(catalog.find("carvalho").orElseThrow(), CosmeticWallet.EMPTY).isGlowing()).isTrue();
        assertThat(icons.selected(CosmeticWallet.EMPTY).material()).isEqualTo(Material.WOOD);
    }

    @Test
    void everyRankOwnsItsWoodsAndTheOnesBelow() {
        assertThat(owned(CosmeticAccess.REGULAR)).containsExactly("carvalho");
        assertThat(owned(CosmeticAccess.VIP)).containsExactly("carvalho", "pinheiro", "eucalipto");
        assertThat(owned(CosmeticAccess.ULTRA)).containsExactly("carvalho", "pinheiro", "eucalipto", "selva", "acacia");
        assertThat(owned(CosmeticAccess.ULTRA_PLUS)).containsExactly("carvalho", "pinheiro", "eucalipto", "selva", "acacia", "carvalho-escuro", "tronco-carvalho");
        assertThat(owned(CosmeticAccess.ALL)).hasSize(12);
    }

    @Test
    void aGrantedLogBelongsToThePlayerEvenWithoutARank() {
        CosmeticWallet granted = new CosmeticWallet(Set.of("tronco-eucalipto"), "tronco-eucalipto", 0, CosmeticAccess.REGULAR);

        assertThat(granted.state(catalog.find("tronco-eucalipto").orElseThrow())).isEqualTo(CosmeticState.SELECTED);
        assertThat(icons.cosmetic(catalog.find("tronco-eucalipto").orElseThrow(), granted).name()).isEqualTo("§aTronco de eucalipto");
    }

    private List<String> owned(CosmeticAccess access) {
        CosmeticWallet wallet = CosmeticWallet.loaded(Set.of(), "carvalho", 0, access, catalog);
        return catalog.all().stream().filter(wallet::owns).map(Cosmetic::id).toList();
    }
}
