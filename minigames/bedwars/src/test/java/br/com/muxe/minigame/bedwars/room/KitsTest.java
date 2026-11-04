package br.com.muxe.minigame.bedwars.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class KitsTest {

    @Test
    void thePickaxeGoesThroughFiveLevelsWithEfficiencyOnly() {
        assertThat(IntStream.rangeClosed(1, 5).mapToObj(Kits::pickaxeMaterial))
            .containsExactly(Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE);
        assertThat(IntStream.rangeClosed(1, 5).map(Kits::pickaxeEfficiency)).containsExactly(1, 1, 2, 3, 3);
        assertThat(Kits.isPickaxe(Material.STONE_PICKAXE)).isTrue();
    }
}
