package br.com.muxe.minigame.bedwars.invisibility;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;

class InvisibilityTest {

    private final UUID target = UUID.randomUUID();
    private final UUID viewer = UUID.randomUUID();

    @Test
    void enemiesAndSpectatorsDoNotSeeTheArmourOfAnInvisiblePlayer() {
        assertThat(Invisibility.hidesArmor(viewer, Optional.of(TeamColor.AZUL), target, TeamColor.VERMELHO)).isTrue();
        assertThat(Invisibility.hidesArmor(viewer, Optional.empty(), target, TeamColor.VERMELHO)).isTrue();
    }

    @Test
    void teammatesAndThePlayerStillSeeTheArmour() {
        assertThat(Invisibility.hidesArmor(viewer, Optional.of(TeamColor.VERMELHO), target, TeamColor.VERMELHO)).isFalse();
        assertThat(Invisibility.hidesArmor(target, Optional.of(TeamColor.VERMELHO), target, TeamColor.VERMELHO)).isFalse();
    }

    @Test
    void anInvisiblePlayerLeavesFootstepsOnlyWhenWalkingOnTheGround() {
        assertThat(Invisibility.leavesFootstep(1.0, true)).isTrue();
        assertThat(Invisibility.leavesFootstep(1.0, false)).isFalse();
        assertThat(Invisibility.leavesFootstep(0.1, true)).isFalse();
    }

    @Test
    void theInvisibilityPotionShowsNoParticlesThatGiveThePlayerAway() {
        PotionEffect invisibility = Invisibility.potionEffect(PotionEffectType.INVISIBILITY, 600, 0);
        PotionEffect speed = Invisibility.potionEffect(PotionEffectType.SPEED, 900, 1);

        assertThat(invisibility.hasParticles()).isFalse();
        assertThat(invisibility.getDuration()).isEqualTo(600);
        assertThat(speed.hasParticles()).isTrue();
    }
}
