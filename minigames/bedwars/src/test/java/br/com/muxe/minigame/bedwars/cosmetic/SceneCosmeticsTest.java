package br.com.muxe.minigame.bedwars.cosmetic;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class SceneCosmeticsTest {

    private final Vec origin = new Vec(4.5, 70, -1.5);

    @Test
    void theDyeOfATeamIsTheWoolColourOfThatTeam() {
        assertThat(SceneCosmetics.dye(TeamColor.VERMELHO)).hasValue(14);
        assertThat(SceneCosmetics.dye(TeamColor.AZUL)).hasValue(11);
        assertThat(SceneCosmetics.dye(TeamColor.BRANCO)).hasValue(0);
    }

    @Test
    void aFinalKillSceneCarriesTheVictimsSkinAndTeamDye() {
        SignedSkin skin = new SignedSkin("value", "signature");

        Scene scene = SceneCosmetics.victimScene(origin, new Vec(0, 0, 1), Optional.of(skin), SceneCosmetics.dye(TeamColor.VERDE));

        assertThat(scene.dye()).hasValue(5);
        assertThat(scene.body()).contains(skin);
        assertThat(scene.skin()).contains("value");
        assertThat(scene.riders()).isEmpty();
        assertThat(scene.area().size()).isEqualTo(Scene.REACH * 2 * Scene.REACH * 2);
    }

    @Test
    void aFinalKillSceneWithoutATeamHasNoDye() {
        assertThat(SceneCosmetics.victimScene(origin, new Vec(0, 0, 1), Optional.empty(), OptionalInt.empty()).dye()).isEmpty();
    }
}
