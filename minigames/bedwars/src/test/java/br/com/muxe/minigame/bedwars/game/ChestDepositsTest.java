package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ChestDepositsTest {

    @Test
    void islandChestsAndEnderChestsTakeDeposits() {
        assertThat(ChestDeposits.isChest(Material.CHEST)).isTrue();
        assertThat(ChestDeposits.isChest(Material.TRAPPED_CHEST)).isTrue();
        assertThat(ChestDeposits.isChest(Material.ENDER_CHEST)).isTrue();
        assertThat(ChestDeposits.isChest(Material.FURNACE)).isFalse();
    }

    @Test
    void resourcesAndBoughtItemsCanBeStoredButPermanentToolsStayWithThePlayer() {
        assertThat(ChestDeposits.canStore(Material.IRON_INGOT)).isTrue();
        assertThat(ChestDeposits.canStore(Material.FIREBALL)).isTrue();
        assertThat(ChestDeposits.canStore(Material.IRON_SWORD)).isTrue();
        assertThat(ChestDeposits.canStore(Material.WOOD_SWORD)).isFalse();
        assertThat(ChestDeposits.canStore(Material.IRON_PICKAXE)).isFalse();
        assertThat(ChestDeposits.canStore(Material.STONE_AXE)).isFalse();
        assertThat(ChestDeposits.canStore(Material.SHEARS)).isFalse();
    }
}
