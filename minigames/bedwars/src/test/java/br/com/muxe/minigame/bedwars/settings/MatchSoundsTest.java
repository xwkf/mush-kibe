package br.com.muxe.minigame.bedwars.settings;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.effect.SoundCue;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.bukkit.Sound;
import org.junit.jupiter.api.Test;

class MatchSoundsTest {

    private final MatchSounds sounds = MatchSounds.from(RoomFixtures.CONFIG.getConfigurationSection("sounds"));

    @Test
    void buyingAnItemPlingsAndBuyingAnUpgradeLevelsUpHigh() {
        assertThat(sounds.bought()).contains(new SoundCue(Sound.NOTE_PLING, 1, 1));
        assertThat(sounds.upgrade()).contains(new SoundCue(Sound.LEVEL_UP, 1, 2));
    }

    @Test
    void storingInTheChestPops() {
        assertThat(sounds.deposited()).contains(new SoundCue(Sound.CHICKEN_EGG_POP, 1, 1.5f));
    }

    @Test
    void breakingABedGrowlsAndLosingYoursSoundsLikeAWitherDying() {
        assertThat(sounds.bedDestroyed()).contains(new SoundCue(Sound.ENDERDRAGON_GROWL, 1, 1));
        assertThat(sounds.bedLost()).contains(new SoundCue(Sound.WITHER_DEATH, 1, 1));
    }

    @Test
    void aFinalKillPlingsHighForTheKillerAndTheEliminatedPlayerHearsAZombieCure() {
        assertThat(sounds.finalKill()).contains(new SoundCue(Sound.NOTE_PLING, 1, 2));
        assertThat(sounds.eliminated()).contains(new SoundCue(Sound.ZOMBIE_UNFECT, 1, 2));
    }

    @Test
    void aTeamOutExplodesForEveryoneAndTheMatchEndsWithADragonAndAFirework() {
        assertThat(sounds.teamEliminated()).contains(new SoundCue(Sound.EXPLODE, 1, 1));
        assertThat(sounds.matchEnd()).contains(new SoundCue(Sound.ENDERDRAGON_GROWL, 1, 1));
        assertThat(sounds.matchEndFirework()).contains(new SoundCue(Sound.FIREWORK_LAUNCH, 1, 1));
    }

    @Test
    void anOldConfigWithoutTheNewSoundsStillLoads() {
        MatchSounds empty = MatchSounds.from(null);

        assertThat(empty.deposited()).isEmpty();
        assertThat(empty.bedDestroyed()).isEmpty();
        assertThat(empty.bedLost()).isEmpty();
        assertThat(empty.finalKill()).isEmpty();
        assertThat(empty.matchEnd()).isEmpty();
    }
}
