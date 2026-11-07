package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.api.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.api.cosmetic.catalog.Availability;
import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticIcons;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings.CategoryMenu;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.api.cosmetic.preview.ScenePreviewSettings;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VictoryEffectTest {

    private static final SceneAnimations ANIMATIONS = SceneAnimations.victories();
    private final CosmeticCatalog catalog = RoomFixtures.COSMETICS.catalog("bedwars", CosmeticKind.VICTORY_EFFECT, RoomFixtures.CONFIG.getConfigurationSection("cosmetics"),
        id -> ANIMATIONS.find(id).isPresent());
    private final CosmeticMenuSettings menus = BedWarsCosmetics.menuSettings(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus"));
    private final CategoryMenu menu = menus.category(CosmeticKind.VICTORY_EFFECT.key());
    private final CosmeticIcons icons = new CosmeticIcons(menu, catalog);

    @Test
    void theVictoryEffectsAreTheTwentyTwoOfTheReferenceAndAllOfThemPlay() {
        assertThat(catalog.category()).isEqualTo("bedwars-vitoria");
        assertThat(catalog.all()).hasSize(22);
        assertThat(catalog.all()).allMatch(Cosmetic::animated);
        Cosmetic christmas = catalog.find("feliz-natal").orElseThrow();
        assertThat(christmas.stars()).isEqualTo(5);
        assertThat(christmas.tag()).contains("&cNatal 2025");
        assertThat(christmas.availability()).isEqualTo(Availability.EVENT);
        assertThat(catalog.price(catalog.find("contagem-de-flash").orElseThrow())).isEqualTo(300);
        assertThat(catalog.price(catalog.find("deserto").orElseThrow())).isEqualTo(800);
    }

    @Test
    void theCategoryButtonReplacesTheComingSoonItemAndTheListHasItsOwnTitle() {
        assertThat(menu.button().slot()).isEqualTo(16);
        assertThat(menus.soon()).doesNotContainKey(16);
        assertThat(menu.list().title()).isEqualTo("Efeito de Vitória ({page}/{pages})");
        assertThat(menu.list().slots()).isEqualTo(menus.category(BedWarsCosmetics.BED_DESTRUCTION).list().slots());
        assertThat(icons.category(CosmeticWallet.EMPTY).lore().getFirst()).isEqualTo("§80/22 (0%)");
        assertThat(icons.random(CosmeticWallet.EMPTY).name()).isEqualTo("§aAleatório");
    }

    @Test
    void anEffectWithoutStarsShowsNoStarLine() {
        ItemBuilder wither = icons.cosmetic(catalog.find("wither").orElseThrow(), new CosmeticWallet(Set.of(), CosmeticWallet.RANDOM, 0, false));

        assertThat(wither.lore()).startsWith("§7Suba em um Wither!", " ", "§6Premiação mensal");
    }

    @Test
    void thePreviewCountsDownThreeSecondsBeforeTheEffect() {
        ScenePreviewSettings preview = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("victory-preview"));

        assertThat(preview.world()).isEqualTo("previa_efeito_de_vitoria");
        assertThat(preview.countdown()).containsExactly("&a3...", "&a2...", "&a1...");
        assertThat(preview.sceneStart()).isEqualTo(60);
        assertThat(preview.origin()).isEqualTo(new Vec(-19.5, 27, 68.5));
        assertThat(preview.facing().z()).isLessThan(-0.9);
    }

    @Test
    void thePreviewMapKnowsItsMiddleSoMapWideEffectsAreNotBuiltAroundTheViewer() {
        ScenePreviewSettings preview = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("victory-preview"));

        Area area = preview.area(preview.origin());

        assertThat(area.middleX()).isEqualTo(0.5);
        assertThat(area.middleZ()).isEqualTo(0.5);
        assertThat(area).isEqualTo(Area.around(preview.origin(), Scene.REACH).middleAt(0.5, 0.5));
    }

    @Test
    void aPreviewWithoutAMiddleUsesTheViewersSurroundings() {
        ScenePreviewSettings preview = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("final-kill-preview"));
        Vec origin = new Vec(4, 30, -8);

        assertThat(preview.area(origin)).isEqualTo(Area.around(origin, Scene.REACH));
    }
}
