package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VictimBodyTest {

    @Test
    void theBodyAsksForTheVictimsSkinAndLetsTheStageFindItsSignature() {
        Scene scene = new Scene(Vec.ZERO, new Vec(0, 0, 1), Optional.of("value"));

        assertThat(VictimBody.skin(scene)).contains(new SignedSkin("value", null));
        assertThat(VictimBody.skin(Scene.at(Vec.ZERO, new Vec(0, 0, 1)))).isEmpty();
    }

    @Test
    void theBodyFacesTheKiller() {
        assertThat(VictimBody.facingKiller(Scene.at(Vec.ZERO, new Vec(0, 0, 1)))).isCloseTo(180, offset(1e-3f));
        assertThat(VictimBody.facingKiller(Scene.at(Vec.ZERO, new Vec(-1, 0, 0)))).isCloseTo(-90, offset(1e-3f));
    }
}
