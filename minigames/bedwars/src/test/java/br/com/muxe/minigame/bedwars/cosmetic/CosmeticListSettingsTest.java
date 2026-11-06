package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticListSettings;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticListSettings.Entries;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings.LoreTexts;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.junit.jupiter.api.Test;

class CosmeticListSettingsTest {

    private final CosmeticMenuSettings menus = CosmeticMenuSettings.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.menus"));

    @Test
    void theClassicListsStartWithNoneAndRandomAndKeepOwnedFirst() {
        Entries entries = menus.category(CosmeticMenuSettings.BED_DESTRUCTION).list().entries();

        assertThat(entries).isEqualTo(Entries.DEFAULT);
        assertThat(entries.entryIndex(0, 30)).isEqualTo(2);
        assertThat(entries.entryIndex(29, 30)).isEqualTo(31);
        assertThat(menus.category(CosmeticMenuSettings.AURA).list().entries()).isEqualTo(Entries.DEFAULT);
    }

    @Test
    void randomCanSitAfterSomeCosmeticsAndNoneCanBeLeftOut() {
        Entries entries = new Entries(false, true, 2, false, false, false);

        assertThat(entries.entryIndex(0, 5)).isZero();
        assertThat(entries.entryIndex(1, 5)).isEqualTo(1);
        assertThat(entries.entryIndex(2, 5)).isEqualTo(3);
        assertThat(entries.entryIndex(4, 5)).isEqualTo(5);
    }

    @Test
    void randomAfterMoreCosmeticsThanTheCategoryHasGoesAtTheEnd() {
        Entries entries = new Entries(false, true, 9, true, true, false);

        assertThat(entries.entryIndex(1, 2)).isEqualTo(1);
    }

    @Test
    void withoutNoneAndRandomTheCosmeticsStartAtTheFirstEntry() {
        Entries entries = new Entries(false, false, 0, false, false, true);

        assertThat(entries.entryIndex(0, 12)).isZero();
        assertThat(entries.entryIndex(11, 12)).isEqualTo(11);
    }

    @Test
    void aNegativePositionIsRefused() {
        assertThatThrownBy(() -> new Entries(true, true, -1, true, true, false)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCategoryThatOverridesTheLoreKeepsWhatItDoesNotWrite() {
        LoreTexts bed = menus.category(CosmeticMenuSettings.BED_DESTRUCTION).list().lore();
        LoreTexts kill = menus.category(CosmeticMenuSettings.KILL_MESSAGE).list().lore();

        assertThat(kill.header()).containsExactly(" ");
        assertThat(kill.select()).containsExactly("&eBotão esquerdo: &fSelecionar");
        assertThat(kill.unavailable()).containsExactly("&cIndisponível.");
        assertThat(kill.buy()).isEqualTo(bed.buy());
        assertThat(kill.ownedName()).isEqualTo(bed.ownedName());
        assertThat(kill.star()).isEqualTo(bed.star());
        assertThat(bed.header()).isEmpty();
    }

    @Test
    void everyTierHasItsOwnTag() {
        LoreTexts lore = menus.category(CosmeticMenuSettings.WOOD_TYPE).list().lore();

        assertThat(lore.vipTag()).isEqualTo("&7Incluso no &a&lVIP");
        assertThat(lore.ultraTag()).isEqualTo("&7Incluso no &d&lULTRA");
        assertThat(lore.ultraPlusTag()).isEqualTo("&7Incluso no &d&lULTRA&6+");
    }

    @Test
    void theNewCategoriesCoverTheirComingSoonSlots() {
        CosmeticListSettings wood = menus.category(CosmeticMenuSettings.WOOD_TYPE).list();

        assertThat(menus.categories()).containsKeys(CosmeticMenuSettings.WOOD_TYPE, CosmeticMenuSettings.KILL_MESSAGE);
        assertThat(menus.soon()).containsOnlyKeys(14);
        assertThat(wood.slots()).hasSizeGreaterThanOrEqualTo(12);
    }
}
