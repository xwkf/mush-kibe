package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class FoliageTest {

    @Test
    void grassAndBushesOfTheMapCanBeBroken() {
        assertThat(Foliage.isFoliage(Material.LONG_GRASS)).isTrue();
        assertThat(Foliage.isFoliage(Material.DOUBLE_PLANT)).isTrue();
        assertThat(Foliage.isFoliage(Material.DEAD_BUSH)).isTrue();
    }

    @Test
    void theRestOfTheMapStaysProtected() {
        assertThat(Foliage.isFoliage(Material.GRASS)).isFalse();
        assertThat(Foliage.isFoliage(Material.WOOD)).isFalse();
        assertThat(Foliage.isFoliage(Material.LEAVES)).isFalse();
    }

    @Test
    void onlyDoublePlantsHaveTwoHalves() {
        assertThat(Foliage.isTall(Material.DOUBLE_PLANT)).isTrue();
        assertThat(Foliage.isTall(Material.LONG_GRASS)).isFalse();
    }
}
