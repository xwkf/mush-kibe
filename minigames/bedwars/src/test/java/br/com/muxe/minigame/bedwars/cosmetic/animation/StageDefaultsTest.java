package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import org.junit.jupiter.api.Test;

class StageDefaultsTest {

    @Test
    void aStageWithoutSkinsStillGivesAPlayerForAWornTexture() {
        RecordingStage stage = new RecordingStage(block -> false);

        stage.playerWearing(Vec.ZERO, 0, "texture");

        assertThat(stage.actors).singleElement().satisfies(actor -> assertThat(actor.type).isEqualTo("Player"));
    }
}
