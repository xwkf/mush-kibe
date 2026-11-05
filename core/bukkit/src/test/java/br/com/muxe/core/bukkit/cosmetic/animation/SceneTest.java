package br.com.muxe.core.bukkit.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SceneTest {

    private final Vec origin = new Vec(1.5, 64, -2.5);

    @Test
    void aSceneHasNoVictimBodyUnlessOneIsGiven() {
        assertThat(Scene.at(origin, new Vec(0, 0, 1)).body()).isEmpty();
        assertThat(new Scene(origin, new Vec(1, 0, 0), Optional.of("texture"), List.of()).body()).isEmpty();
    }

    @Test
    void aBodySkinKeepsEverythingElseOfTheScene() {
        SignedSkin skin = new SignedSkin("value", "signature");
        Scene scene = new Scene(origin, new Vec(3, 2, 0), Optional.of("texture"));

        Scene dressed = scene.withBody(Optional.of(skin));

        assertThat(dressed.body()).contains(skin);
        assertThat(dressed.origin()).isEqualTo(origin);
        assertThat(dressed.facing()).isEqualTo(new Vec(1, 0, 0));
        assertThat(dressed.skin()).contains("texture");
        assertThat(dressed.area()).isEqualTo(scene.area());
    }
}
