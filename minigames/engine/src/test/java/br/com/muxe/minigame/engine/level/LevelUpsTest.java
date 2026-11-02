package br.com.muxe.minigame.engine.level;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class LevelUpsTest {

    private static final UUID PLAYER = UUID.randomUUID();

    private final LevelUps levelUps = new LevelUps("&b&lPARABÉNS &7Você subiu para o nível {level}&7!", level -> "&4[&c" + level + "&l⚝&4]");

    @Test
    void theFirstLevelKnownIsOnlyRemembered() {
        assertThat(levelUps.reach(PLAYER, 903)).isEmpty();
    }

    @Test
    void reachingAHigherLevelCongratulatesWithTheBadgeOfTheNewLevel() {
        levelUps.reach(PLAYER, 903);

        assertThat(levelUps.reach(PLAYER, 904)).contains("§b§lPARABÉNS §7Você subiu para o nível §4[§c904§l⚝§4]§7!");
    }

    @Test
    void theSameOrALowerLevelSaysNothing() {
        levelUps.reach(PLAYER, 5);

        assertThat(levelUps.reach(PLAYER, 5)).isEmpty();
        assertThat(levelUps.reach(PLAYER, 4)).isEmpty();
        assertThat(levelUps.reach(PLAYER, 6)).isPresent();
    }

    @Test
    void skippingLevelsCongratulatesOnceForTheLevelReached() {
        levelUps.reach(PLAYER, 1);

        assertThat(levelUps.reach(PLAYER, 3)).contains("§b§lPARABÉNS §7Você subiu para o nível §4[§c3§l⚝§4]§7!");
        assertThat(levelUps.reach(PLAYER, 3)).isEmpty();
    }

    @Test
    void aForgottenPlayerStartsOverWithoutAMessage() {
        levelUps.reach(PLAYER, 1);
        levelUps.forget(PLAYER);

        assertThat(levelUps.reach(PLAYER, 2)).isEmpty();
    }
}
