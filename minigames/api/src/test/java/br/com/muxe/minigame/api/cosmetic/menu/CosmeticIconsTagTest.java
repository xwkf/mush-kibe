package br.com.muxe.minigame.api.cosmetic.menu;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.api.cosmetic.catalog.Availability;
import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings.LoreTexts;
import java.util.List;
import java.util.Optional;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class CosmeticIconsTagTest {

    private static final LoreTexts TEXTS = new LoreTexts("*", "&6", "&7", "&a{name}", "&c{name}", "vip", "ultra", "ultra-plus", List.of(), List.of(), List.of(),
        List.of(), List.of(), List.of(), List.of(), List.of());

    private static Cosmetic cosmetic(Availability availability, Optional<String> tag) {
        return new Cosmetic("id", "Nome", ItemBuilder.of(Material.STONE), 1, tag, availability, List.of(), true);
    }

    @Test
    void aRankCosmeticWithoutItsOwnTagShowsTheRankTag() {
        assertThat(CosmeticIcons.tag(cosmetic(Availability.VIP, Optional.empty()), TEXTS)).contains("vip");
        assertThat(CosmeticIcons.tag(cosmetic(Availability.ULTRA, Optional.empty()), TEXTS)).contains("ultra");
        assertThat(CosmeticIcons.tag(cosmetic(Availability.ULTRA_PLUS, Optional.empty()), TEXTS)).contains("ultra-plus");
    }

    @Test
    void aRankCosmeticWithItsOwnTagShowsItsOwnTag() {
        Optional<String> own = Optional.of("&7Exclusivo para &d&lULTRA");

        assertThat(CosmeticIcons.tag(cosmetic(Availability.VIP, own), TEXTS)).isEqualTo(own);
        assertThat(CosmeticIcons.tag(cosmetic(Availability.ULTRA, own), TEXTS)).isEqualTo(own);
        assertThat(CosmeticIcons.tag(cosmetic(Availability.ULTRA_PLUS, own), TEXTS)).isEqualTo(own);
    }

    @Test
    void otherCosmeticsShowOnlyTheirOwnTag() {
        assertThat(CosmeticIcons.tag(cosmetic(Availability.EVENT, Optional.of("&cNatal 2025")), TEXTS)).contains("&cNatal 2025");
        assertThat(CosmeticIcons.tag(cosmetic(Availability.SALE, Optional.empty()), TEXTS)).isEmpty();
        assertThat(CosmeticIcons.tag(cosmetic(Availability.DEFAULT, Optional.empty()), TEXTS)).isEmpty();
    }
}
