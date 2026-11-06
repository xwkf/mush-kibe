package br.com.muxe.minigame.bedwars.cosmetic.trail;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.Room;
import br.com.muxe.minigame.bedwars.room.RoomPlayer;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

class ProjectileKindsTest {

    @Test
    void eachProjectileOfTheGameHasItsKind() {
        assertThat(ProjectileKinds.of(EntityType.ARROW)).isEqualTo(ProjectileKind.ARROW);
        assertThat(ProjectileKinds.of(EntityType.SNOWBALL)).isEqualTo(ProjectileKind.SNOWBALL);
        assertThat(ProjectileKinds.of(EntityType.EGG)).isEqualTo(ProjectileKind.EGG);
        assertThat(ProjectileKinds.of(EntityType.ENDER_PEARL)).isEqualTo(ProjectileKind.ENDER_PEARL);
        assertThat(ProjectileKinds.of(EntityType.SPLASH_POTION)).isEqualTo(ProjectileKind.POTION);
        assertThat(ProjectileKinds.of(EntityType.FISHING_HOOK)).isEqualTo(ProjectileKind.FISHING_HOOK);
        assertThat(ProjectileKinds.of(EntityType.FIREBALL)).isEqualTo(ProjectileKind.FIREBALL);
        assertThat(ProjectileKinds.of(EntityType.SMALL_FIREBALL)).isEqualTo(ProjectileKind.FIREBALL);
    }

    @Test
    void anyOtherProjectileIsOther() {
        assertThat(ProjectileKinds.of(EntityType.THROWN_EXP_BOTTLE)).isEqualTo(ProjectileKind.OTHER);
        assertThat(ProjectileKinds.of(EntityType.WITHER_SKULL)).isEqualTo(ProjectileKind.OTHER);
        assertThat(ProjectileKinds.of(EntityType.PLAYER)).isEqualTo(ProjectileKind.OTHER);
    }

    @Test
    void onlyALivingPlayerOfAMatchInProgressLeavesATrail() {
        assertThat(TrailRules.launches(Room.Phase.PLAYING, RoomPlayer.State.ALIVE, false)).isTrue();
        assertThat(TrailRules.launches(Room.Phase.PLAYING, RoomPlayer.State.ALIVE, true)).isFalse();
        assertThat(TrailRules.launches(Room.Phase.PLAYING, RoomPlayer.State.RESPAWNING, false)).isFalse();
        assertThat(TrailRules.launches(Room.Phase.PLAYING, RoomPlayer.State.ELIMINATED, false)).isFalse();
        assertThat(TrailRules.launches(Room.Phase.WAITING, RoomPlayer.State.WAITING, false)).isFalse();
    }

    @Test
    void aProjectileFliesWhileItExistsAndIsNotLyingOnTheGround() {
        assertThat(TrailRules.flying(true, false)).isTrue();
        assertThat(TrailRules.flying(true, true)).isFalse();
        assertThat(TrailRules.flying(false, false)).isFalse();
    }
}
