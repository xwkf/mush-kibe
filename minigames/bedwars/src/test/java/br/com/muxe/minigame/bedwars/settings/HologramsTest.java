package br.com.muxe.minigame.bedwars.settings;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import br.com.muxe.minigame.bedwars.settings.BedWarsSettings.Holograms;
import org.junit.jupiter.api.Test;

class HologramsTest {

    private final Holograms holograms = RoomFixtures.SETTINGS.holograms();

    @Test
    void theShopNameIsBoldInTheTeamColour() {
        assertThat(holograms.shop()).containsExactly("{color}&lLOJA", "&7Clique para ver mais.");
        assertThat(holograms.shopTeam()).containsExactly("{color}&lLOJA DA EQUIPE", "&7Clique para ver mais.");
    }

    @Test
    void theUpgradesNameIsBoldInTheTeamColour() {
        assertThat(holograms.upgrades()).containsExactly("{color}&lMELHORIAS", "&7Clique para ver mais.");
        assertThat(holograms.upgradesTeam()).containsExactly("{color}&lMELHORIAS DA EQUIPE", "&7Clique para ver mais.");
    }
}
