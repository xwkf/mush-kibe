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
    void breakingABedGrowlsAndLosingYoursSoundsLikeAnotherDragon() {
        assertThat(sounds.bedDestroyed()).contains(new SoundCue(Sound.ENDERDRAGON_GROWL, 1, 1));
        assertThat(sounds.bedLost()).isPresent();
        assertThat(sounds.bedLost().orElseThrow().sound().name()).startsWith("ENDERDRAGON").isNotEqualTo(Sound.ENDERDRAGON_GROWL.name());
    }

    @Test
    void anOldConfigWithoutTheNewSoundsStillLoads() {
        MatchSounds empty = MatchSounds.from(null);

        assertThat(empty.deposited()).isEmpty();
        assertThat(empty.bedDestroyed()).isEmpty();
        assertThat(empty.bedLost()).isEmpty();
    }
}
