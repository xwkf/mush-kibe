package br.com.muxe.core.bukkit.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
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

    @Test
    void aSceneHasNoVictimDyeUnlessOneIsGiven() {
        assertThat(Scene.at(origin, new Vec(0, 0, 1)).dye()).isEmpty();
        assertThat(Scene.at(origin, new Vec(0, 0, 1)).withBody(Optional.empty()).dye()).isEmpty();
    }

    @Test
    void aDyeKeepsEverythingElseOfTheScene() {
        SignedSkin skin = new SignedSkin("value", "signature");
        Scene scene = new Scene(origin, new Vec(3, 2, 0), Optional.of("texture")).withBody(Optional.of(skin));

        Scene dyed = scene.withDye(OptionalInt.of(14));

        assertThat(dyed.dye()).hasValue(14);
        assertThat(dyed.body()).contains(skin);
        assertThat(dyed.origin()).isEqualTo(origin);
        assertThat(dyed.skin()).contains("texture");
        assertThat(dyed.area()).isEqualTo(scene.area());
        assertThat(dyed.withBody(Optional.empty()).dye()).hasValue(14);
    }

    @Test
    void aDyeIsOneOfTheSixteenWoolColours() {
        Scene scene = Scene.at(origin, new Vec(0, 0, 1));

        assertThatThrownBy(() -> scene.withDye(OptionalInt.of(16))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> scene.withDye(OptionalInt.of(-1))).isInstanceOf(IllegalArgumentException.class);
        assertThat(scene.withDye(OptionalInt.of(0)).dye()).hasValue(0);
    }
}
