package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class EndPillarsTest {

    @Test
    void fivePillarsStandOnARingThirtyThreeBlocksAroundTheMiddleOfTheMap() {
        EndPillars pillars = new EndPillars(new Area(-80, -80, 80, 80));

        assertThat(pillars.pillars()).extracting(pillar -> pillar.x() + "," + pillar.z())
            .containsExactly("33,0", "10,32", "-27,19", "-27,-19", "10,-32");
    }

    @Test
    void withoutAKnownMiddleThePillarsGoAroundTheCentreOfTheMapArea() {
        EndPillars pillars = new EndPillars(new Area(0, 100, 40, 140));

        assertThat(pillars.pillars().getFirst().x()).isEqualTo(53);
        assertThat(pillars.pillars().getFirst().z()).isEqualTo(120);
    }

    @Test
    void thePillarsGoAroundTheMiddleOfTheMapEvenWhenTheWinnerIsFarFromIt() {
        Area aroundTheWinner = Area.around(new Vec(70.5, 35, 32.5), 16);

        EndPillars pillars = new EndPillars(aroundTheWinner.middleAt(0.5, 0.5));

        assertThat(pillars.pillars()).extracting(pillar -> pillar.x() + "," + pillar.z())
            .containsExactly("33,0", "10,32", "-27,19", "-27,-19", "10,-32");
    }

    @Test
    void eachPillarIsARoundObsidianColumnNineBlocksWideFromTheBottomToHeightOneHundredTwentySeven() {
        EndPillars.Pillar pillar = new EndPillars(new Area(-80, -80, 80, 80)).pillars().getFirst();

        List<BlockChange> blocks = pillar.blocks();

        assertThat(pillar.columns()).hasSize(69);
        assertThat(blocks).hasSize(69 * 128).allMatch(block -> block.material() == Material.OBSIDIAN);
        assertThat(blocks).extracting(BlockChange::y).contains(0, 127).allMatch(y -> y >= 0 && y <= 127);
        assertThat(pillar.columns()).extracting(column -> column[0]).contains(29, 37).allMatch(x -> x >= 29 && x <= 37);
    }
}
