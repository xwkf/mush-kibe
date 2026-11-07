package br.com.muxe.minigame.bedwars.shop;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import br.com.muxe.minigame.bedwars.room.RoomPlayer;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AlreadyHadTest {

    private final ShopCatalog catalog = ShopCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("shop"));
    private final RoomPlayer player = new RoomPlayer(UUID.randomUUID());

    @Test
    void armourAlreadyWornAndToolsAtTheirLastLevelCannotBeBoughtAgain() {
        player.armorTier(3);
        player.pickaxeTier(5);
        player.giveShears();

        assertThat(Offer.of(catalog.items().get("armadura-ferro"), player).alreadyHad()).isTrue();
        assertThat(Offer.of(catalog.items().get("picareta"), player).alreadyHad()).isTrue();
        assertThat(Offer.of(catalog.items().get("tesoura"), player).alreadyHad()).isTrue();
    }

    @Test
    void blocksSwordsAndToolsBelowTheLastLevelCanBeBoughtAgain() {
        player.pickaxeTier(2);

        assertThat(Offer.of(catalog.items().get("argila"), player).alreadyHad()).isFalse();
        assertThat(Offer.of(catalog.items().get("espada-diamante"), player).alreadyHad()).isFalse();
        assertThat(Offer.of(catalog.items().get("picareta"), player).alreadyHad()).isFalse();
    }

    @Test
    void tryingToBuySomethingYouAlreadyHaveSaysSoInRed() {
        assertThat(RoomFixtures.CONFIG.getString("messages.already-have")).isEqualTo("&cVocê já tem este item.");
    }
}
