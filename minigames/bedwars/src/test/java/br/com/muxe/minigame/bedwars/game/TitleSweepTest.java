package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import org.junit.jupiter.api.Test;

class TitleSweepTest {

    private final TitleSweep victory = TitleSweep.from(RoomFixtures.CONFIG.getConfigurationSection("end-titles.victory"), "", "");
    private final TitleSweep defeat = TitleSweep.from(RoomFixtures.CONFIG.getConfigurationSection("end-titles.defeat"), "", "");

    @Test
    void theVictoryTitleSweepsGoldAcrossTheWordLetterByLetter() {
        assertThat(victory.frame(0)).contains("&f&lVITÓRIA");
        assertThat(victory.frame(2)).contains("&e&lV&f&lITÓRIA");
        assertThat(victory.frame(4)).contains("&6&lV&e&lI&f&lTÓRIA");
        assertThat(victory.frame(5)).contains("&6&lV&e&lI&f&lTÓRIA");
        assertThat(victory.frame(14)).contains("&6&lVITÓRI&e&lA");
        assertThat(victory.frame(16)).contains("&6&lVITÓRIA");
    }

    @Test
    void thenItBlinksBetweenGoldAndWhiteEverySevenTicks() {
        assertThat(victory.frame(23)).contains("&6&lVITÓRIA");
        assertThat(victory.frame(24)).contains("&f&lVITÓRIA");
        assertThat(victory.frame(31)).contains("&6&lVITÓRIA");
        assertThat(victory.frame(38)).contains("&f&lVITÓRIA");
        assertThat(victory.frame(115)).contains("&6&lVITÓRIA");
        assertThat(victory.frame(116)).isEmpty();
    }

    @Test
    void theDefeatTitleUsesRedAndWhite() {
        assertThat(defeat.frame(0)).contains("&f&lDERROTA");
        assertThat(defeat.frame(16)).contains("&c&lDERROTA");
        assertThat(defeat.frame(24)).contains("&f&lDERROTA");
        assertThat(defeat.frame(31)).contains("&c&lDERROTA");
    }
}
