package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.api.cosmetic.catalog.Availability;
import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticCategory;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticIcons;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticListSettings;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings.CategoryMenu;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.api.cosmetic.message.MessageDescriptions;
import br.com.muxe.minigame.api.cosmetic.message.MessagePreview;
import br.com.muxe.minigame.api.cosmetic.message.MessageSets;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticAccess;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticState;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class KillMessageCosmeticTest {

    private static final String VIEWER = "§8tadeu";

    private final MessageSets sets = MessageSets.from(RoomFixtures.KILL_MESSAGES);
    private final CosmeticCatalog catalog = RoomFixtures.COSMETICS.catalog("bedwars", CosmeticKind.KILL_MESSAGE, RoomFixtures.CONFIG.getConfigurationSection("cosmetics"),
        id -> sets.find(id).isPresent());
    private final CosmeticMenuSettings menus = BedWarsCosmetics.menuSettings(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus"));
    private final CategoryMenu menu = menus.category(CosmeticKind.KILL_MESSAGE.key());
    private final CosmeticIcons icons = new CosmeticIcons(menu, catalog);
    private final CosmeticListSettings list = menu.list();
    private final MessageDescriptions descriptions = new MessageDescriptions(sets,
        MessagePreview.from(RoomFixtures.KILL_MESSAGES.getConfigurationSection("preview")));
    private final Cosmetic standard = catalog.find("padrao").orElseThrow();
    private final Cosmetic statistic = catalog.find("estatistico").orElseThrow();

    private static CosmeticWallet selecting(String selected, String... owned) {
        return new CosmeticWallet(Set.of(owned), selected, 0, false);
    }

    private ItemBuilder icon(Cosmetic cosmetic, CosmeticWallet wallet) {
        return icons.cosmetic(cosmetic, wallet, descriptions.of(cosmetic, VIEWER));
    }

    @Test
    void theCatalogHasTheStandardSetForEveryoneAndTheStatisticSetAsATournamentPrize() {
        assertThat(catalog.all().stream().map(Cosmetic::id)).containsExactly("padrao", "estatistico");
        assertThat(catalog.defaultId()).contains("padrao");
        assertThat(standard.name()).isEqualTo("Padrão");
        assertThat(standard.icon().material()).isEqualTo(Material.PAPER);
        assertThat(standard.availability()).isEqualTo(Availability.DEFAULT);
        assertThat(statistic.name()).isEqualTo("Estatístico");
        assertThat(statistic.icon().material()).isEqualTo(Material.BOOK);
        assertThat(statistic.availability()).isEqualTo(Availability.EVENT);
        assertThat(statistic.tag()).contains("&6Premiação de torneio");
        assertThat(catalog.all()).allMatch(cosmetic -> cosmetic.stars() == 0);
    }

    @Test
    void everyoneOwnsTheStandardSetAndCannotBuyIt() {
        assertThat(CosmeticWallet.EMPTY.owns(standard)).isTrue();
        assertThat(CosmeticWallet.EMPTY.state(standard)).isEqualTo(CosmeticState.OWNED);
        assertThat(standard.forSale()).isFalse();
        assertThat(CosmeticWallet.EMPTY.state(statistic)).isEqualTo(CosmeticState.UNAVAILABLE);
        assertThat(statistic.forSale()).isFalse();
    }

    @Test
    void theCategoryButtonReplacesTheComingSoonItem() {
        assertThat(menu.button().slot()).isEqualTo(31);
        assertThat(menus.soon()).doesNotContainKey(31);
        assertThat(menu.button().item().material()).isEqualTo(Material.PAPER);
        assertThat(menu.button().item().name()).isEqualTo("§aMensagens de kill");
        assertThat(icons.category(selecting("padrao")).lore()).containsExactly(
            "§81/2 (50%)", " ", "§7Customize as suas mensagens", "§7de kill.", " ", "§7Selecionado: §aPadrão", " ", "§eClique para ver mais.");
        assertThat(icons.category(selecting(CosmeticWallet.RANDOM, "estatistico")).lore()).containsExactly(
            "§82/2 (100%)", " ", "§7Customize as suas mensagens", "§7de kill.", " ", "§7Selecionado: §aAleatório", " ", "§eClique para ver mais.");
    }

    @Test
    void theListIsTitledLikeTheRecordingAndLaidOutLikeTheOtherCategories() {
        assertThat(list.title()).isEqualTo("Mensagens de kill ({page}/{pages})");
        assertThat(list.rows()).isEqualTo(6);
        assertThat(list.slots()).isEqualTo(menus.category(BedWarsCosmetics.BED_DESTRUCTION).list().slots());
        assertThat(list.back().slot()).isEqualTo(48);
        assertThat(list.back().item().material()).isEqualTo(Material.ARROW);
        assertThat(list.back().item().name()).isEqualTo("§aVoltar");
        assertThat(list.back().item().lore()).containsExactly("§7Para Cosméticos do Bed Wars");
        assertThat(list.back().item().hidesFlags()).isFalse();
        assertThat(list.selected().slot()).isEqualTo(49);
    }

    @Test
    void theListHasNoNoneEntryAndPutsRandomRightAfterTheStandardSet() {
        CosmeticListSettings.Entries entries = list.entries();

        assertThat(entries.none()).isFalse();
        assertThat(entries.random()).isTrue();
        assertThat(entries.randomAfter()).isEqualTo(1);
        assertThat(entries.ownedFirst()).isFalse();
        assertThat(entries.previews()).isFalse();
        assertThat(entries.entryIndex(0, 2)).isZero();
        assertThat(entries.entryIndex(1, 2)).isEqualTo(2);
    }

    @Test
    void theEntriesKeepTheOrderOfTheCatalogEvenWhenTheStatisticSetIsOwned() {
        CosmeticCategory category = CosmeticCategory.of(menu, catalog, null, null);

        assertThat(category.ordered(selecting("estatistico", "estatistico")).stream().map(Cosmetic::id)).containsExactly("padrao", "estatistico");
        assertThat(category.ordered(CosmeticWallet.EMPTY).stream().map(Cosmetic::id)).containsExactly("padrao", "estatistico");
    }

    @Test
    void theStandardSetLooksLikeTheRecording() {
        ItemBuilder item = icon(standard, selecting(CosmeticWallet.RANDOM));

        assertThat(item.material()).isEqualTo(Material.PAPER);
        assertThat(item.name()).isEqualTo("§aPadrão");
        assertThat(item.hidesFlags()).isTrue();
        assertThat(item.isGlowing()).isFalse();
        assertThat(item.lore()).containsExactly(
            " ",
            "§7Prévia:",
            "§7§cFulano §7morreu para §8tadeu§7.",
            "§7§cFulano §7morreu no void para §8tadeu§7.",
            "§7§8tadeu §7morreu§7.",
            "§7§8tadeu §7morreu no void§7.",
            "§7A §cCama do Vermelho §7foi destruída",
            "§7por §8tadeu§7.",
            " ",
            "§eBotão esquerdo: §fSelecionar"
        );
    }

    @Test
    void theSelectedStandardSetGlowsAndSaysSo() {
        ItemBuilder item = icon(standard, selecting("padrao"));

        assertThat(item.isGlowing()).isTrue();
        assertThat(item.lore()).endsWith(" ", "§aJá selecionado.");
    }

    @Test
    void theLockedStatisticSetLooksLikeTheRecording() {
        ItemBuilder item = icon(statistic, selecting(CosmeticWallet.RANDOM));

        assertThat(item.material()).isEqualTo(Material.BOOK);
        assertThat(item.name()).isEqualTo("§cEstatístico");
        assertThat(item.hidesFlags()).isTrue();
        assertThat(item.isGlowing()).isFalse();
        assertThat(item.lore()).containsExactly(
            " ",
            "§7Prévia:",
            "§7§cFulano §7foi a §b17.089ª§7 kill de §8tadeu§7.",
            "§7§cFulano §7foi a §b19.215ª§7 kill final",
            "§7de §8tadeu§7.",
            "§7§8tadeu §7morreu pela §b12.538ª§7 vez§7.",
            "§7§8tadeu §7teve sua §b2.537ª§7 morte final§7.",
            "§7A §cCama do Vermelho §7foi a §b11.938ª§7",
            "§7cama destruída de §8tadeu§7.",
            " ",
            "§6Premiação de torneio",
            " ",
            "§cIndisponível."
        );
    }

    @Test
    void theOwnedStatisticSetCanBeSelected() {
        ItemBuilder item = icon(statistic, selecting("padrao", "estatistico"));

        assertThat(item.name()).isEqualTo("§aEstatístico");
        assertThat(item.lore()).endsWith("§6Premiação de torneio", " ", "§eBotão esquerdo: §fSelecionar");
    }

    @Test
    void randomIsANameTagThatGlowsWhileSelected() {
        ItemBuilder random = icons.random(selecting(CosmeticWallet.RANDOM));

        assertThat(random.material()).isEqualTo(Material.NAME_TAG);
        assertThat(random.name()).isEqualTo("§aAleatório");
        assertThat(random.isGlowing()).isTrue();
        assertThat(random.lore()).containsExactly("§7Seleciona um cosmético", "§7aleatório que você possui", " ", "§aJá selecionado.");
        assertThat(icons.random(selecting("padrao")).lore()).endsWith("§eBotão esquerdo: §fSelecionar");
    }

    @Test
    void theSelectedItemLooksLikeTheRecording() {
        ItemBuilder selected = icons.selected(selecting(CosmeticWallet.RANDOM));

        assertThat(selected.material()).isEqualTo(Material.NAME_TAG);
        assertThat(selected.name()).isEqualTo("§eSelecionado: §7Aleatório");
        assertThat(selected.lore()).containsExactly("§7Seleciona um cosmético", "§7aleatório que você possui");
        assertThat(selected.hidesFlags()).isFalse();
        assertThat(icons.selected(selecting("padrao")).name()).isEqualTo("§eSelecionado: §7Padrão");
    }

    @Test
    void aProfileWithNothingSavedShowsTheStandardSetSelectedBecauseThatIsTheDefault() {
        CosmeticWallet loaded = CosmeticWallet.loaded(Set.of(), catalog.defaultId().orElseThrow(), 0,
            CosmeticAccess.REGULAR, catalog);

        assertThat(icons.selectedName(loaded)).isEqualTo("Padrão");
        assertThat(icon(standard, loaded).isGlowing()).isTrue();
    }

    @Test
    void thePreviewOfACosmeticWithoutASetIsItsOwnDescription() {
        Cosmetic other = new Cosmetic("outra", "Outra", ItemBuilder.of(Material.PAPER), 0, Optional.empty(), Availability.EVENT, List.of("&7linha"), true);

        assertThat(descriptions.of(other, VIEWER)).containsExactly("&7linha");
    }
}
