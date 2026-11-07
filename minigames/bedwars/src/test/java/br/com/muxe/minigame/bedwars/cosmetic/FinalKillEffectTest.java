package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
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
import java.util.List;
import org.junit.jupiter.api.Test;

class FinalKillEffectTest {

    private static final SceneAnimations ANIMATIONS = SceneAnimations.finalKills();
    private final CosmeticCatalog catalog = RoomFixtures.COSMETICS.catalog("bedwars", CosmeticKind.FINAL_KILL, RoomFixtures.CONFIG.getConfigurationSection("cosmetics"),
        id -> ANIMATIONS.find(id).isPresent());
    private final CosmeticMenuSettings menus = BedWarsCosmetics.menuSettings(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus"));
    private final CategoryMenu menu = menus.category(CosmeticKind.FINAL_KILL.key());

    @Test
    void theFinalKillsAreTheFortyThreeOfTheReferenceAndPresenteadoPlays() {
        assertThat(catalog.category()).isEqualTo("bedwars-kill-final");
        assertThat(catalog.all()).hasSize(43);
        assertThat(catalog.all().getFirst().id()).isEqualTo("presenteado");
        assertThat(catalog.all()).allMatch(Cosmetic::animated);
        assertThat(catalog.find("fogos-de-artificio").orElseThrow().availability()).isEqualTo(Availability.ULTRA);
        assertThat(catalog.price(catalog.find("colmeia").orElseThrow())).isEqualTo(2_000);
        assertThat(catalog.price(catalog.find("redstone").orElseThrow())).isEqualTo(750);
    }

    @Test
    void theCategoryButtonReplacesTheComingSoonItemAndTheListHasTwoPages() {
        assertThat(menu.button().slot()).isEqualTo(12);
        assertThat(menus.soon()).doesNotContainKeys(12, 16);
        assertThat(menu.list().title()).isEqualTo("Kill Final ({page}/{pages})");
        assertThat(new CosmeticIcons(menu, catalog).category(CosmeticWallet.EMPTY).lore().getFirst()).isEqualTo("§80/43 (0%)");
        assertThat((catalog.all().size() + 2 + menu.list().slots().size() - 1) / menu.list().slots().size()).isEqualTo(2);
    }

    @Test
    void thePreviewPutsAPlayerToHitFourBlocksAhead() {
        ScenePreviewSettings preview = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("final-kill-preview"));

        assertThat(preview.target()).isPresent();
        assertThat(preview.target().get().actionBar()).isEqualTo("&eAcerte o jogador!");
        assertThat(preview.world()).isEqualTo("previa_kill_final");
        assertThat(preview.origin()).isEqualTo(new Vec(-3.5, 24, 2.5));
        assertThat(preview.targetAt().x()).isCloseTo(0.5, offset(1e-9));
        assertThat(preview.targetAt().y()).isCloseTo(24, offset(1e-9));
        assertThat(preview.targetAt().z()).isCloseTo(2.5, offset(1e-9));
        assertThat(preview.countdown()).isEmpty();
    }

    @Test
    void thePlayerGetsAWoodenSwordAndTheTargetHasAGreyNick() {
        ScenePreviewSettings.Target target = ScenePreviewSettings.from(RoomFixtures.CONFIG.getConfigurationSection("final-kill-preview")).target().orElseThrow();

        assertThat(target.weapon()).isEqualTo("WOOD_SWORD");
        assertThat(target.names()).hasSize(8).allSatisfy(name -> assertThat(name).startsWith("&7"));
        assertThat(target.name(-3)).isIn(target.names());
        assertThat(new ScenePreviewSettings.Target(4, "", List.of(), "WOOD_SWORD").name(5)).isEqualTo("&7Jogador");
    }
}
