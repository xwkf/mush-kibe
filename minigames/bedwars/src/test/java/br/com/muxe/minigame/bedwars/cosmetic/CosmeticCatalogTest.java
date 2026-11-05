package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.bedwars.cosmetic.animation.BedAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.Availability;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticIcons;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.bedwars.cosmetic.preview.PreviewSettings;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticAccess;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticState;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.text.Collator;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CosmeticCatalogTest {

    private static final BedAnimations ANIMATIONS = new BedAnimations();
    private static final Set<String> WITHOUT_ANIMATION = Set.of("tornado", "tubarao");
    private final CosmeticCatalog catalog = CosmeticCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics"),
        id -> ANIMATIONS.find(id).isPresent() && !WITHOUT_ANIMATION.contains(id));
    private final CosmeticIcons icons = new CosmeticIcons(CosmeticMenuSettings.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus")).category(CosmeticMenuSettings.BED_DESTRUCTION), catalog);

    @Test
    void theCatalogListsThirtyCosmeticsAndEveryAnimationHasAnEntry() {
        CosmeticCatalog real = CosmeticCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics"), id -> ANIMATIONS.find(id).isPresent());
        assertThat(real.all()).hasSize(30).allMatch(BedCosmetic::animated);
        assertThat(catalog.all().stream().filter(BedCosmetic::animated)).hasSize(28);
        ANIMATIONS.all().forEach(animation -> assertThat(catalog.find(animation.id())).as(animation.id()).isPresent());
        assertThat(catalog.price(catalog.find("tinta").orElseThrow())).isEqualTo(1_750);
        assertThat(catalog.price(catalog.find("sal-na-ferida").orElseThrow())).isEqualTo(750);
    }

    @Test
    void cosmeticsWithoutAnAnimationCannotBeBoughtOrSelected() {
        BedCosmetic tornado = catalog.find("tornado").orElseThrow();
        CosmeticWallet rich = new CosmeticWallet(Set.of("tornado"), CosmeticWallet.RANDOM, 10_000, false);

        assertThat(tornado.forSale()).isFalse();
        assertThat(rich.state(tornado)).isEqualTo(CosmeticState.UNAVAILABLE);
    }

    @Test
    void ultraPlayersOwnTheUltraCosmetics() {
        BedCosmetic fireworks = catalog.find("fogos-de-artificio").orElseThrow();

        assertThat(fireworks.availability()).isEqualTo(Availability.ULTRA);
        assertThat(new CosmeticWallet(Set.of(), CosmeticWallet.RANDOM, 0, true).state(fireworks)).isEqualTo(CosmeticState.OWNED);
        assertThat(new CosmeticWallet(Set.of(), CosmeticWallet.RANDOM, 0, false).state(fireworks)).isEqualTo(CosmeticState.UNAVAILABLE);
    }

    @Test
    void opsOwnEveryCosmeticButStillCannotUseTheOnesWithoutAnimation() {
        CosmeticAccess access = CosmeticAccess.of(permission -> permission.equals(CosmeticAccess.ALL_PERMISSION));
        CosmeticWallet wallet = CosmeticWallet.loaded(Set.of(), CosmeticWallet.RANDOM, 0, access, catalog);

        assertThat(access).isEqualTo(CosmeticAccess.ALL);
        assertThat(wallet.ownedCount(catalog)).isEqualTo(30);
        assertThat(wallet.state(catalog.find("apavorante").orElseThrow())).isEqualTo(CosmeticState.OWNED);
        assertThat(wallet.state(catalog.find("tubarao").orElseThrow())).isEqualTo(CosmeticState.UNAVAILABLE);
    }

    @Test
    void accessFollowsThePermissions() {
        assertThat(CosmeticAccess.of(permission -> false)).isEqualTo(CosmeticAccess.REGULAR);
        assertThat(CosmeticAccess.of(permission -> permission.equals(CosmeticAccess.ULTRA_PERMISSION))).isEqualTo(CosmeticAccess.ULTRA);
        assertThat(CosmeticWallet.loaded(Set.of("tinta"), "tinta", 10, CosmeticAccess.ULTRA, catalog))
            .isEqualTo(new CosmeticWallet(Set.of("tinta"), "tinta", 10, true));
        assertThat(CosmeticWallet.loaded(Set.of(), null, 0, CosmeticAccess.REGULAR, catalog)).isEqualTo(CosmeticWallet.EMPTY);
    }

    @Test
    void randomPicksAnOwnedAnimationAndNonePicksNothing() {
        CosmeticWallet wallet = new CosmeticWallet(Set.of("tinta", "tornado"), CosmeticWallet.RANDOM, 0, false);

        assertThat(wallet.animation(catalog, new Random(1))).contains("tinta");
        assertThat(wallet.selecting(CosmeticWallet.NONE).animation(catalog, new Random(1))).isEmpty();
        assertThat(wallet.selecting("deserto").animation(catalog, new Random(1))).contains("tinta");
        assertThat(CosmeticWallet.EMPTY.animation(catalog, new Random(1))).isEmpty();
    }

    @Test
    void ownedCosmeticsComeFirstInAlphabeticalOrderThenTheRestAlphabetically() {
        CosmeticWallet wallet = new CosmeticWallet(Set.of("tinta", "enderman", "apavorante"), CosmeticWallet.RANDOM, 0, false);
        Collator alphabetical = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        alphabetical.setStrength(Collator.PRIMARY);

        List<BedCosmetic> sorted = wallet.sorted(catalog);

        assertThat(sorted.subList(0, 3)).extracting(BedCosmetic::id).containsExactly("apavorante", "enderman", "tinta");
        assertThat(sorted.subList(3, sorted.size())).extracting(BedCosmetic::name).isSortedAccordingTo(alphabetical::compare);
        assertThat(sorted.subList(3, sorted.size())).noneMatch(wallet::owns);
    }

    @Test
    void aCosmeticForSaleShowsThePriceAndWhatIsMissing() {
        ItemBuilder icon = icons.cosmetic(catalog.find("apavorante").orElseThrow(), new CosmeticWallet(Set.of(), CosmeticWallet.RANDOM, 15, false));

        assertThat(icon.name()).isEqualTo("§cApavorante");
        assertThat(icon.lore()).containsExactly(
            "§6§l✩✩✩✩§7§l✩",
            " ",
            "§6Halloween 2026",
            " ",
            "§7Preço: §d1.750 créditos",
            "§cFaltam 1.735 créditos!",
            " ",
            "§eBotão esquerdo: §fComprar",
            "§eBotão direito: §fVisualizar"
        );
    }

    @Test
    void theSelectedCosmeticGlowsAndUnavailableOnesOnlyPreview() {
        CosmeticWallet wallet = new CosmeticWallet(Set.of("avalanche"), "avalanche", 0, false);

        ItemBuilder avalanche = icons.cosmetic(catalog.find("avalanche").orElseThrow(), wallet);
        ItemBuilder tubarao = icons.cosmetic(catalog.find("tubarao").orElseThrow(), wallet);

        assertThat(avalanche.isGlowing()).isTrue();
        assertThat(avalanche.lore()).endsWith("§aJá selecionado.", "§eBotão direito: §fVisualizar");
        assertThat(tubarao.lore()).endsWith("§aAniversário 2026", " ", "§cIndisponível.", "§eBotão direito: §fVisualizar");
        assertThat(icons.selectedName(wallet)).isEqualTo("Avalanche");
        assertThat(icons.category(wallet).lore().getFirst()).isEqualTo("§81/30 (3%)");
    }

    @Test
    void theRandomItemCyclesThroughTheIconsOfTheCosmeticsThePlayerCanDraw() {
        CosmeticWallet wallet = new CosmeticWallet(Set.of("tinta", "enderman", "tornado"), CosmeticWallet.RANDOM, 0, false);
        ItemBuilder random = icons.random(wallet);

        List<ItemBuilder> frames = icons.randomFrames(wallet);

        assertThat(frames).extracting(ItemBuilder::material)
            .containsExactly(catalog.find("enderman").orElseThrow().icon().material(), catalog.find("tinta").orElseThrow().icon().material());
        assertThat(frames).extracting(ItemBuilder::skullTexture)
            .containsExactly(catalog.find("enderman").orElseThrow().icon().skullTexture(), catalog.find("tinta").orElseThrow().icon().skullTexture());
        assertThat(frames).allSatisfy(frame -> {
            assertThat(frame.name()).isEqualTo(random.name());
            assertThat(frame.lore()).isEqualTo(random.lore());
            assertThat(frame.isGlowing()).isTrue();
        });
        assertThat(icons.randomFrames(CosmeticWallet.EMPTY)).isEmpty();
    }

    @Test
    void randomIsTheDefaultSelection() {
        ItemBuilder random = icons.random(CosmeticWallet.EMPTY);

        assertThat(random.isGlowing()).isTrue();
        assertThat(random.lore()).containsExactly("§7Seleciona um cosmético", "§7aleatório que você possui", " ", "§aJá selecionado.");
        assertThat(icons.none(CosmeticWallet.EMPTY).lore()).containsExactly(" ", "§eBotão esquerdo: §fSelecionar");
        assertThat(icons.selectedName(CosmeticWallet.EMPTY)).isEqualTo("Aleatório");
    }

    @Test
    void thePreviewKnowsTheBedOfThePreviewMap() {
        PreviewSettings preview = PreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("preview"));

        assertThat(preview.isBed(6, 11, 17)).isTrue();
        assertThat(preview.isBed(6, 11, 16)).isTrue();
        assertThat(preview.isBed(6, 11, 15)).isFalse();
        assertThat(preview.timeout()).isEqualTo(Duration.ofMinutes(2));
        assertThat(preview.lines("Tinta")).containsExactly("", "&fVisualizando:", "&eTinta", "", "&amuxe.com.br");
    }

    @Test
    void thePreviewHasABedInTheLastSlotThatTakesThePlayerBack() {
        PreviewSettings preview = PreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("preview"));

        assertThat(preview.back().slot()).isEqualTo(8);
        assertThat(preview.back().material()).isEqualTo("BED");
        assertThat(preview.back().name()).isEqualTo("&aVoltar ao Lobby");
        assertThat(preview.goesBack(true, 8)).isTrue();
        assertThat(preview.goesBack(false, 8)).isFalse();
        assertThat(preview.goesBack(true, 0)).isFalse();
    }
}
