package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.game.ChestDeposits.Verdict;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.util.List;
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
        List<Material> inventory = List.of(Material.STONE_SWORD, Material.IRON_SWORD);

        assertThat(ChestDeposits.verdict(Material.IRON_INGOT, inventory)).isEqualTo(Verdict.STORE);
        assertThat(ChestDeposits.verdict(Material.FIREBALL, inventory)).isEqualTo(Verdict.STORE);
        assertThat(ChestDeposits.verdict(Material.IRON_SWORD, inventory)).isEqualTo(Verdict.STORE);
        assertThat(ChestDeposits.verdict(Material.IRON_PICKAXE, inventory)).isEqualTo(Verdict.KEPT);
        assertThat(ChestDeposits.verdict(Material.STONE_AXE, inventory)).isEqualTo(Verdict.KEPT);
        assertThat(ChestDeposits.verdict(Material.SHEARS, inventory)).isEqualTo(Verdict.KEPT);
        assertThat(ChestDeposits.verdict(Material.AIR, inventory)).isEqualTo(Verdict.NOTHING);
    }

    @Test
    void theOnlySwordInTheInventoryCannotBeStored() {
        assertThat(ChestDeposits.verdict(Material.DIAMOND_SWORD, List.of(Material.DIAMOND_SWORD, Material.WOOL))).isEqualTo(Verdict.LAST_SWORD);
        assertThat(ChestDeposits.verdict(Material.WOOD_SWORD, List.of(Material.WOOD_SWORD))).isEqualTo(Verdict.LAST_SWORD);
        assertThat(ChestDeposits.verdict(Material.WOOD_SWORD, List.of(Material.WOOD_SWORD, Material.IRON_SWORD))).isEqualTo(Verdict.KEPT);
    }

    @Test
    void eachRefusalHasItsOwnMessage() {
        assertThat(RoomFixtures.CONFIG.getString("messages.chest-kept")).isEqualTo("&cVocê não pode guardar este item.");
        assertThat(RoomFixtures.CONFIG.getString("messages.chest-last-one")).isEqualTo("&cVocê não pode guardar só um deste item.");
    }
}
