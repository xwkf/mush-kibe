package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.bedwars.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.Availability;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticIcons;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings.CategoryMenu;
import br.com.muxe.minigame.bedwars.cosmetic.preview.ScenePreviewSettings;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VictoryEffectTest {

    private static final SceneAnimations ANIMATIONS = SceneAnimations.victories();
    private final CosmeticCatalog catalog = CosmeticCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.victory-effect"), "cosmetics", "bedwars-vitoria",
        id -> ANIMATIONS.find(id).isPresent());
    private final CosmeticMenuSettings menus = CosmeticMenuSettings.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus"));
    private final CategoryMenu menu = menus.category(CosmeticMenuSettings.VICTORY_EFFECT);
    private final CosmeticIcons icons = new CosmeticIcons(menu, catalog);

    @Test
    void theVictoryEffectsAreTheTwentyTwoOfTheReferenceAndTenOfThemPlay() {
        assertThat(catalog.category()).isEqualTo("bedwars-vitoria");
        assertThat(catalog.all()).hasSize(22);
        assertThat(catalog.all().stream().filter(BedCosmetic::animated).map(BedCosmetic::id)).containsExactlyInAnyOrder("tinta", "feliz-natal", "como-treinar-seu-porco", "altas-aventuras", "colheita-de-aboboras", "apavorante", "contagem-de-flash", "apocalipse-zumbi", "aventureiro", "necromante", "deserto");
        BedCosmetic christmas = catalog.find("feliz-natal").orElseThrow();
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
        assertThat(menu.list().slots()).isEqualTo(menus.category(CosmeticMenuSettings.BED_DESTRUCTION).list().slots());
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
}
