package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.api.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.api.cosmetic.aura.Aura;
import br.com.muxe.minigame.api.cosmetic.aura.AuraParticle;
import br.com.muxe.minigame.api.cosmetic.aura.Auras;
import br.com.muxe.minigame.api.cosmetic.catalog.Availability;
import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticIcons;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticListSettings;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings.CategoryMenu;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.api.cosmetic.preview.ScenePreviewSettings;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticState;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class AuraCosmeticTest {

    private final CosmeticCatalog catalog = RoomFixtures.COSMETICS.catalog("bedwars", CosmeticKind.AURA, RoomFixtures.CONFIG.getConfigurationSection("cosmetics"),
        id -> Auras.find(id).isPresent());
    private final CosmeticMenuSettings menus = CosmeticMenuSettings.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus"));
    private final CategoryMenu menu = menus.category(CosmeticMenuSettings.AURA);
    private final CosmeticIcons icons = new CosmeticIcons(menu, catalog);
    private final CosmeticListSettings list = menu.list();

    private static CosmeticWallet owning(String selected, String... owned) {
        return new CosmeticWallet(Set.of(owned), selected, 0, false);
    }

    @Test
    void theCatalogHasTheTwoTournamentAurasAndBothHaveAnEffect() {
        assertThat(catalog.category()).isEqualTo("bedwars-aura");
        assertThat(catalog.all().stream().map(Cosmetic::id)).containsExactly("liga-mestre", "liga-diamante");
        assertThat(catalog.all()).allMatch(Cosmetic::animated);
        Cosmetic master = catalog.find("liga-mestre").orElseThrow();
        assertThat(master.name()).isEqualTo("Liga Mestre");
        assertThat(master.icon().material()).isEqualTo(Material.REDSTONE);
        assertThat(master.tag()).contains("&6Premiação de torneio");
        assertThat(master.availability()).isEqualTo(Availability.EVENT);
        Cosmetic diamond = catalog.find("liga-diamante").orElseThrow();
        assertThat(diamond.name()).isEqualTo("Liga Diamante");
        assertThat(diamond.icon().material()).isEqualTo(Material.DIAMOND);
        assertThat(diamond.tag()).contains("&6Premiação de torneio");
        assertThat(diamond.availability()).isEqualTo(Availability.EVENT);
        assertThat(diamond.forSale()).isFalse();
    }

    @Test
    void everyAuraOfTheRegistryHasAnEntryAndAnAnimationForThePreview() {
        SceneAnimations animations = SceneAnimations.auras();

        Auras.all().forEach(aura -> {
            assertThat(catalog.find(aura.id())).as(aura.id()).isPresent();
            assertThat(animations.find(aura.id())).as(aura.id()).isPresent();
        });
        assertThat(animations.all()).hasSize(Auras.all().size());
    }

    @Test
    void theMasterLeagueBurnsAndTheDiamondLeagueIsWitchMagic() {
        assertThat(Auras.find("liga-mestre")).contains(new Aura("liga-mestre", AuraParticle.FLAME));
        assertThat(Auras.find("liga-diamante")).contains(new Aura("liga-diamante", AuraParticle.SPELL_WITCH));
        assertThat(Auras.find("nenhuma")).isEmpty();
    }

    @Test
    void theCategoryButtonReplacesTheComingSoonItemAndKeepsTheOthersThatAreStillComing() {
        assertThat(menu.button().slot()).isEqualTo(33);
        assertThat(menus.soon()).doesNotContainKey(33);
        assertThat(menus.soon()).containsOnlyKeys(14);
        assertThat(menu.button().item().material()).isEqualTo(Material.EMERALD);
        assertThat(menu.button().item().name()).isEqualTo("§aAura");
        assertThat(icons.category(CosmeticWallet.EMPTY).lore()).containsExactly(
            "§80/2 (0%)", " ", "§7Customize a sua aura.", " ", "§7Selecionado: §aAleatório", " ", "§eClique para ver mais.");
        assertThat(icons.category(owning("liga-mestre", "liga-mestre", "liga-diamante")).lore()).containsExactly(
            "§82/2 (100%)", " ", "§7Customize a sua aura.", " ", "§7Selecionado: §aLiga Mestre", " ", "§eClique para ver mais.");
    }

    @Test
    void theListIsTitledAuraAndLaidOutLikeTheOtherCategories() {
        List<Integer> bedSlots = menus.category(CosmeticMenuSettings.BED_DESTRUCTION).list().slots();

        assertThat(list.title()).isEqualTo("Aura ({page}/{pages})");
        assertThat(list.rows()).isEqualTo(6);
        assertThat(list.slots()).isEqualTo(bedSlots);
        assertThat(list.slots().subList(0, 3)).containsExactly(10, 11, 12);
        assertThat(list.back().slot()).isEqualTo(48);
        assertThat(list.back().item().name()).isEqualTo("§aVoltar");
        assertThat(list.back().item().lore()).containsExactly("§7Para Cosméticos do Bed Wars");
        assertThat(list.selected().slot()).isEqualTo(49);
    }

    @Test
    void noneIsABarrierAndRandomIsANameTagThatListsWhatItDoes() {
        ItemBuilder none = icons.none(CosmeticWallet.EMPTY);
        ItemBuilder random = icons.random(CosmeticWallet.EMPTY);

        assertThat(none.material()).isEqualTo(Material.BARRIER);
        assertThat(none.name()).isEqualTo("§aNenhuma");
        assertThat(random.material()).isEqualTo(Material.NAME_TAG);
        assertThat(random.name()).isEqualTo("§aAleatório");
        assertThat(random.lore()).startsWith("§7Seleciona um cosmético", "§7aleatório que você possui", " ", "§aJá selecionado.");
        assertThat(icons.random(owning(CosmeticWallet.NONE)).lore()).containsExactly(
            "§7Seleciona um cosmético", "§7aleatório que você possui", " ", "§eBotão esquerdo: §fSelecionar");
    }

    @Test
    void randomCyclesThroughTheIconsOfTheAurasThePlayerCanUse() {
        assertThat(icons.randomFrames(CosmeticWallet.EMPTY)).isEmpty();

        List<ItemBuilder> frames = icons.randomFrames(owning(CosmeticWallet.RANDOM, "liga-mestre", "liga-diamante"));

        assertThat(frames.stream().map(ItemBuilder::material)).containsExactly(Material.DIAMOND, Material.REDSTONE);
        assertThat(frames).allMatch(frame -> frame.name().equals("§aAleatório"));
    }

    @Test
    void theSelectedItemNamesWhatIsSelected() {
        assertThat(icons.selected(CosmeticWallet.EMPTY).name()).isEqualTo("§eSelecionado: §7Aleatório");
        assertThat(icons.selected(owning(CosmeticWallet.NONE)).name()).isEqualTo("§eSelecionado: §7Nenhuma");
        assertThat(icons.selected(owning("liga-diamante", "liga-diamante")).name()).isEqualTo("§eSelecionado: §7Liga Diamante");
        assertThat(icons.selected(CosmeticWallet.EMPTY).material()).isEqualTo(Material.NAME_TAG);
    }

    @Test
    void aLockedTournamentAuraLooksLikeTheRecording() {
        Cosmetic master = catalog.find("liga-mestre").orElseThrow();

        ItemBuilder locked = icons.cosmetic(master, CosmeticWallet.EMPTY);

        assertThat(CosmeticWallet.EMPTY.state(master)).isEqualTo(CosmeticState.UNAVAILABLE);
        assertThat(locked.name()).isEqualTo("§cLiga Mestre");
        assertThat(locked.material()).isEqualTo(Material.REDSTONE);
        assertThat(locked.lore()).containsExactly("§6§l✩✩✩✩✩", " ", "§6Premiação de torneio", " ", "§cIndisponível.", "§eBotão direito: §fVisualizar");
    }

    @Test
    void anOwnedAuraCanBeSelectedAndPreviewed() {
        Cosmetic diamond = catalog.find("liga-diamante").orElseThrow();

        ItemBuilder owned = icons.cosmetic(diamond, owning(CosmeticWallet.RANDOM, "liga-diamante"));
        ItemBuilder selected = icons.cosmetic(diamond, owning("liga-diamante", "liga-diamante"));

        assertThat(owned.name()).isEqualTo("§aLiga Diamante");
        assertThat(owned.lore()).containsExactly("§6§l✩✩✩✩✩", " ", "§6Premiação de torneio", " ", "§eBotão esquerdo: §fSelecionar", "§eBotão direito: §fVisualizar");
        assertThat(selected.isGlowing()).isTrue();
        assertThat(selected.lore()).endsWith("§aJá selecionado.", "§eBotão direito: §fVisualizar");
    }

    @Test
    void theOwnedAurasComeFirstAndThenTheRestAlphabetically() {
        CosmeticWallet wallet = owning(CosmeticWallet.RANDOM, "liga-mestre");

        assertThat(wallet.sorted(catalog).stream().map(Cosmetic::id)).containsExactly("liga-mestre", "liga-diamante");
        assertThat(CosmeticWallet.EMPTY.sorted(catalog).stream().map(Cosmetic::id)).containsExactly("liga-diamante", "liga-mestre");
    }

    @Test
    void thePreviewIsAWorldWithoutCountdownOrTargetThatStaysFor60Seconds() {
        ScenePreviewSettings preview = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("aura-preview"));

        assertThat(preview.world()).isEqualTo("previa_aura");
        assertThat(preview.origin()).isEqualTo(new Vec(10.5, 19, 21.5));
        assertThat(preview.yaw()).isEqualTo(180f);
        assertThat(preview.countdown()).isEmpty();
        assertThat(preview.sceneStart()).isZero();
        assertThat(preview.target()).isEmpty();
        assertThat(preview.timeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(preview.back().slot()).isEqualTo(8);
        assertThat(preview.back().material()).isEqualTo("BED");
        assertThat(preview.back().name()).isEqualTo("&aVoltar ao Lobby");
        assertThat(preview.lines("Liga Mestre")).contains("&eLiga Mestre");
        assertThat(preview.goesBack(true, 8)).isTrue();
        assertThat(preview.goesBack(false, 8)).isFalse();
    }
}
