package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.api.cosmetic.catalog.Availability;
import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticIcons;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticListSettings;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings.CategoryMenu;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.api.cosmetic.preview.PreviewKit;
import br.com.muxe.minigame.api.cosmetic.preview.ScenePreviewSettings;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticAccess;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticState;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileTrails;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ProjectileTrailCosmeticTest {

    private static final List<String> IDS = List.of("espiral-da-fortuna", "critico", "chamas", "ultra", "natalino", "ossos", "espectro-verde", "coracoes", "magia",
        "sorte-azul", "faisca-cosmica", "lava", "chroma", "fumaca", "vapor", "agua", "aldeao-furioso", "fogos-de-artificio", "notas-musicais", "sangrento",
        "critico-encantado", "mega-loucura", "arco-iris");

    private final CosmeticMenuSettings menus = CosmeticMenuSettings.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus"));
    private final CategoryMenu menu = menus.category(CosmeticMenuSettings.PROJECTILE_TRAIL);
    private final CosmeticCatalog catalog = catalog(id -> ProjectileTrails.find(id).isPresent());
    private final CosmeticCatalog animated = catalog(id -> true);
    private final CosmeticCatalog still = catalog(id -> false);
    private final CosmeticListSettings list = menu.list();

    private static CosmeticCatalog catalog(Predicate<String> animated) {
        return RoomFixtures.COSMETICS.catalog("bedwars", CosmeticKind.PROJECTILE_TRAIL, RoomFixtures.CONFIG.getConfigurationSection("cosmetics"), animated);
    }

    private static CosmeticWallet owning(String selected, String... owned) {
        return new CosmeticWallet(Set.of(owned), selected, 30, false);
    }

    @Test
    void theCatalogHasTheTwentyThreeTrailsInTheOrderOfTheReference() {
        assertThat(catalog.category()).isEqualTo("bedwars-rastro");
        assertThat(catalog.all().stream().map(Cosmetic::id)).containsExactlyElementsOf(IDS);
        assertThat(catalog.all().stream().map(Cosmetic::name)).containsExactly("Espiral da Fortuna", "Crítico", "Chamas", "Ultra", "Natalino", "Ossos",
            "Espectro Verde", "Corações", "Magia", "Sorte Azul", "Faísca Cósmica", "Lava", "Chroma", "Fumaça", "Vapor", "Água", "Aldeão Furioso",
            "Fogos de Artifício", "Notas Musicais", "Sangrento", "Crítico Encantado", "Mega-Loucura", "Arco-íris");
        assertThat(catalog.all().stream().map(Cosmetic::stars)).containsExactly(5, 1, 5, 5, 5, 5, 5, 5, 5, 4, 1, 1, 5, 3, 3, 4, 4, 3, 5, 5, 2, 4, 4);
        assertThat(catalog.all().stream().map(Cosmetic::availability)).containsExactly(Availability.EVENT, Availability.DEFAULT, Availability.ULTRA,
            Availability.ULTRA, Availability.EVENT, Availability.EVENT, Availability.ULTRA_PLUS, Availability.ULTRA_PLUS, Availability.ULTRA_PLUS, Availability.SALE,
            Availability.SALE, Availability.SALE, Availability.EVENT, Availability.SALE, Availability.SALE, Availability.SALE, Availability.SALE, Availability.SALE,
            Availability.EVENT, Availability.SALE, Availability.SALE, Availability.SALE, Availability.SALE);
    }

    @Test
    void theIconsTagsAndPricesFollowTheReferenceMenu() {
        assertThat(icon("espiral-da-fortuna").material()).isEqualTo(Material.DOUBLE_PLANT);
        assertThat(icon("ultra").material()).isEqualTo(Material.INK_SACK);
        assertThat(icon("ultra").data()).isEqualTo((short) 9);
        assertThat(icon("sorte-azul").data()).isEqualTo((short) 4);
        assertThat(icon("natalino").material()).isEqualTo(Material.SKULL_ITEM);
        assertThat(texture("natalino")).contains("28893da6c340975c37c9b990d3dc8a8944c5a62cb1034ace3add66980a59344");
        assertThat(texture("aldeao-furioso")).contains("d54e2f0a9b4f89b228449ea951a4dd1c6ccab43c620138fc70001e8bcdd73e69");
        assertThat(texture("arco-iris")).contains("9060d1220b268d74b181b3d6a6d1a7a9d40452759507b0f82a578ae2b0e545e1");
        assertThat(icon("chroma").material()).isEqualTo(Material.POTION);
        assertThat(icon("magia").material()).isEqualTo(Material.CAULDRON_ITEM);
        assertThat(cosmetic("espiral-da-fortuna").tag()).contains("&6Premiação de torneio");
        assertThat(cosmetic("natalino").tag()).contains("&cNatal 2025");
        assertThat(cosmetic("ossos").tag()).contains("&6Halloween 2025");
        assertThat(cosmetic("chroma").tag()).contains("&6Premiação mensal");
        assertThat(cosmetic("notas-musicais").tag()).contains("&6Premiação de torneio");
        assertThat(cosmetic("ultra").tag()).contains("&7Exclusivo para &d&lULTRA");
        assertThat(catalog.price(cosmetic("critico"))).isEqualTo(300);
        assertThat(catalog.price(cosmetic("critico-encantado"))).isEqualTo(350);
        assertThat(catalog.price(cosmetic("fumaca"))).isEqualTo(500);
        assertThat(catalog.price(cosmetic("agua"))).isEqualTo(700);
        assertThat(catalog.price(cosmetic("sangrento"))).isEqualTo(800);
    }

    @Test
    void theCategoryButtonReplacesTheComingSoonArrow() {
        CosmeticIcons icons = new CosmeticIcons(menu, still);

        assertThat(menu.button().slot()).isEqualTo(27);
        assertThat(menus.soon()).doesNotContainKey(27);
        assertThat(menu.button().item().material()).isEqualTo(Material.ARROW);
        assertThat(menu.button().item().name()).isEqualTo("§aRastros de Projétil");
        assertThat(icons.category(CosmeticWallet.EMPTY).lore()).containsExactly(
            "§81/23 (4%)", " ", "§7Customize o rastro de", "§7seus projéteis.", " ", "§7Selecionado: §aAleatório", " ", "§eClique para ver mais.");
    }

    @Test
    void theListIsTitledLikeTheReferenceWithNoneAndRandomFirst() {
        CosmeticIcons icons = new CosmeticIcons(menu, still);
        ItemBuilder none = icons.none(CosmeticWallet.EMPTY);
        ItemBuilder random = icons.random(CosmeticWallet.EMPTY);

        assertThat(list.title()).isEqualTo("Rastros de Projétil ({page}/{pages})");
        assertThat(list.slots().subList(0, 3)).containsExactly(10, 11, 12);
        assertThat(none.material()).isEqualTo(Material.BARRIER);
        assertThat(none.name()).isEqualTo("§aNenhuma");
        assertThat(none.lore()).containsExactly(" ", "§eBotão esquerdo: §fSelecionar");
        assertThat(random.material()).isEqualTo(Material.NAME_TAG);
        assertThat(random.isGlowing()).isTrue();
        assertThat(random.lore()).containsExactly("§7Seleciona um cosmético", "§7aleatório que você possui", " ", "§aJá selecionado.");
        assertThat(icons.selected(CosmeticWallet.EMPTY).name()).isEqualTo("§eSelecionado: §7Aleatório");
        assertThat(list.back().item().lore()).containsExactly("§7Para Cosméticos do Bed Wars");
    }

    @Test
    void theUltraTrailShowsItsOwnTagInsteadOfTheUltraOne() {
        CosmeticIcons icons = new CosmeticIcons(menu, animated);
        CosmeticWallet ultra = new CosmeticWallet(Set.of(), CosmeticWallet.RANDOM, 30, CosmeticAccess.ULTRA);

        assertThat(icons.cosmetic(cosmetic("ultra", animated), ultra).lore()).containsExactly(
            "§6§l✩✩✩✩✩", " ", "§7Exclusivo para §d§lULTRA", " ", "§eBotão esquerdo: §fSelecionar", "§eBotão direito: §fVisualizar");
        assertThat(icons.cosmetic(cosmetic("chamas", animated), ultra).lore()).containsExactly(
            "§6§l✩✩✩✩✩", " ", "§7Incluso no §d§lULTRA", " ", "§eBotão esquerdo: §fSelecionar", "§eBotão direito: §fVisualizar");
        assertThat(icons.cosmetic(cosmetic("magia", animated), ultra).lore()).containsExactly(
            "§6§l✩✩✩✩✩", " ", "§7Incluso no §d§lULTRA§6+", " ", "§cIndisponível.", "§eBotão direito: §fVisualizar");
    }

    @Test
    void onceATrailIsAnimatedItCanBeBoughtLikeInTheReference() {
        CosmeticIcons icons = new CosmeticIcons(menu, animated);
        Cosmetic blue = cosmetic("sorte-azul", animated);

        ItemBuilder item = icons.cosmetic(blue, CosmeticWallet.EMPTY.withCredits(30));

        assertThat(CosmeticWallet.EMPTY.state(blue)).isEqualTo(CosmeticState.BUY);
        assertThat(item.name()).isEqualTo("§cSorte Azul");
        assertThat(item.lore()).containsExactly("§6§l✩✩✩✩§7§l✩", " ", "§7Preço: §d700 créditos", "§cFaltam 670 créditos!", " ", "§eBotão esquerdo: §fComprar",
            "§eBotão direito: §fVisualizar");
        assertThat(icons.cosmetic(cosmetic("critico", animated), owning(CosmeticWallet.RANDOM)).name()).isEqualTo("§aCrítico");
        assertThat(icons.randomFrames(owning(CosmeticWallet.RANDOM, "lava"))).hasSize(2);
    }

    @Test
    void whileATrailHasNoAnimationItIsNeitherSoldNorUsable() {
        CosmeticIcons icons = new CosmeticIcons(menu, still);
        Cosmetic blue = cosmetic("sorte-azul", still);

        assertThat(blue.forSale()).isFalse();
        assertThat(CosmeticWallet.EMPTY.state(blue)).isEqualTo(CosmeticState.UNAVAILABLE);
        assertThat(icons.cosmetic(blue, CosmeticWallet.EMPTY).lore()).endsWith("§cIndisponível.", "§eBotão direito: §fVisualizar");
        assertThat(icons.randomFrames(owning(CosmeticWallet.RANDOM, "lava"))).isEmpty();
        assertThat(owning("critico").animation(still, RandomGenerator.getDefault())).isEmpty();
    }

    @Test
    void thePreviewUsesTheAuraMapAndGivesTheRecordedKit() {
        ScenePreviewSettings preview = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("projectile-trail-preview"));
        ScenePreviewSettings aura = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("aura-preview"));
        PreviewKit kit = preview.kit().orElseThrow();

        assertThat(preview.world()).isEqualTo("previa_aura").isEqualTo(aura.world());
        assertThat(preview.origin()).isEqualTo(new Vec(10.5, 19, 21.5));
        assertThat(preview.yaw()).isEqualTo(180f);
        assertThat(preview.lines("Lava")).isEqualTo(aura.lines("Lava")).contains("&fVisualizando:", "&eLava");
        assertThat(preview.timeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(preview.back().slot()).isEqualTo(8);
        assertThat(preview.back().material()).isEqualTo("BED");
        assertThat(preview.back().name()).isEqualTo("&aVoltar ao Lobby");
        assertThat(aura.kit()).isEmpty();
        assertThat(kit.items()).containsOnlyKeys(0, 1, 2, 3, 4, 9);
        assertThat(kit.items().get(0).material()).isEqualTo(Material.SNOW_BALL);
        assertThat(kit.items().get(0).amount()).isEqualTo(16);
        assertThat(kit.items().get(1).material()).isEqualTo(Material.POTION);
        assertThat(kit.items().get(1).data()).isEqualTo((short) 16421);
        assertThat(kit.items().get(1).amount()).isEqualTo(16);
        assertThat(kit.items().get(2).material()).isEqualTo(Material.ENDER_PEARL);
        assertThat(kit.items().get(2).amount()).isEqualTo(16);
        assertThat(kit.items().get(3).material()).isEqualTo(Material.BOW);
        assertThat(kit.items().get(3).isGlowing()).isTrue();
        assertThat(kit.items().get(3).isUnbreakable()).isTrue();
        assertThat(kit.items().get(4).material()).isEqualTo(Material.FISHING_ROD);
        assertThat(kit.items().get(4).isUnbreakable()).isTrue();
        assertThat(kit.items().get(9).material()).isEqualTo(Material.ARROW);
        assertThat(kit.items().get(9).amount()).isEqualTo(1);
        assertThat(kit.items().values()).noneMatch(ItemBuilder::hidesFlags);
        assertThat(kit.refillTicks()).isEqualTo(2);
    }

    @Test
    void theKitItemsCanBeUsedButTheBackBedAndEmptySlotsStayLocked() {
        ScenePreviewSettings preview = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("projectile-trail-preview"));
        ScenePreviewSettings aura = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("aura-preview"));

        assertThat(preview.usesKit(true, 0)).isTrue();
        assertThat(preview.usesKit(true, 4)).isTrue();
        assertThat(preview.usesKit(false, 3)).isFalse();
        assertThat(preview.usesKit(true, 5)).isFalse();
        assertThat(preview.usesKit(true, 8)).isFalse();
        assertThat(preview.goesBack(true, 8)).isTrue();
        assertThat(aura.usesKit(true, 0)).isFalse();
    }

    @Test
    void aStackIsToppedBackUpOnlyWhenItIsShortOrGone() {
        PreviewKit kit = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("projectile-trail-preview")).kit().orElseThrow();

        assertThat(kit.needsRefill(0, Material.SNOW_BALL, 0, 15)).isTrue();
        assertThat(kit.needsRefill(0, Material.SNOW_BALL, 0, 16)).isFalse();
        assertThat(kit.needsRefill(2, Material.AIR, 0, 0)).isTrue();
        assertThat(kit.needsRefill(1, Material.POTION, 16421, 16)).isFalse();
        assertThat(kit.needsRefill(1, Material.POTION, 0, 16)).isTrue();
        assertThat(kit.needsRefill(3, Material.BOW, 0, 1)).isFalse();
        assertThat(kit.needsRefill(9, Material.ARROW, 0, 1)).isFalse();
        assertThat(kit.needsRefill(9, Material.AIR, 0, 0)).isTrue();
        assertThat(kit.needsRefill(5, Material.AIR, 0, 0)).isFalse();
        assertThat(kit.needsRefill(8, Material.BED, 0, 1)).isFalse();
    }

    private String texture(String id) {
        return new String(Base64.getDecoder().decode(icon(id).skullTexture()), StandardCharsets.UTF_8);
    }

    private ItemBuilder icon(String id) {
        return cosmetic(id).icon();
    }

    private Cosmetic cosmetic(String id) {
        return cosmetic(id, catalog);
    }

    private static Cosmetic cosmetic(String id, CosmeticCatalog from) {
        return from.find(id).orElseThrow();
    }
}
