package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Arrays;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ShatterFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void theVictimBreaksIntoBlueGlassWithOneGlassSound() {
        ShatterFinalKill glass = ShatterFinalKill.glass();
        RecordingStage stage = play(glass);

        assertThat(glass.id()).isEqualTo("quebrado");
        assertThat(glass.material()).isEqualTo(Material.STAINED_GLASS);
        assertThat(glass.data()).isEqualTo(ShatterFinalKill.BLUE);
        assertThat(stage.particles).containsOnlyKeys("BLOCK_CRACK").containsEntry("BLOCK_CRACK", ShatterFinalKill.HEIGHTS.length);
        assertThat(stage.sounds).containsExactly("dig.glass");
        assertThat(stage.actors).isEmpty();
    }

    @Test
    void theVictimBreaksIntoRedstoneWithFiveStoneSounds() {
        ShatterFinalKill redstone = ShatterFinalKill.redstone();
        RecordingStage stage = play(redstone);

        assertThat(redstone.id()).isEqualTo("redstone");
        assertThat(redstone.material()).isEqualTo(Material.REDSTONE_BLOCK);
        assertThat(stage.sounds).hasSize(5).containsOnly("dig.stone");
        assertThat(stage.particles).containsEntry("BLOCK_CRACK", ShatterFinalKill.HEIGHTS.length);
    }

    @Test
    void theShardsFillTheBodyFromTheFeetToTheHeadAndAreDensestAtTheWaist() {
        int total = Arrays.stream(ShatterFinalKill.HEIGHTS).mapToInt(ShatterFinalKill::shards).sum();

        assertThat(ShatterFinalKill.HEIGHTS).startsWith(0.25).endsWith(1.5);
        assertThat(ShatterFinalKill.shards(ShatterFinalKill.DENSEST)).isEqualTo(50);
        assertThat(ShatterFinalKill.shards(1)).isEqualTo(25);
        assertThat(total).isEqualTo(175);
    }

    private RecordingStage play(ShatterFinalKill animation) {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        animation.script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(3));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }
        return stage;
    }
}
