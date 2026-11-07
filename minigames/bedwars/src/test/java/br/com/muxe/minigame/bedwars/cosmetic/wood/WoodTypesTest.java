package br.com.muxe.minigame.bedwars.cosmetic.wood;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticAccess;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class WoodTypesTest {

    private final CosmeticCatalog catalog = CosmeticCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.wood-type"), "cosmetics", "bedwars-madeira", id -> true);
    private final Map<UUID, CosmeticWallet> wallets = new HashMap<>();
    private final WoodTypes woods = new WoodTypes(catalog, id -> Optional.ofNullable(wallets.get(id)));
    private final UUID player = UUID.randomUUID();

    private void wallet(String selected, CosmeticAccess access, String... owned) {
        wallets.put(player, new CosmeticWallet(Set.of(owned), selected, 0, access));
    }

    @Test
    void theShopItemSwappedIsTheWoodOfTheShop() {
        assertThat(WoodTypes.ITEM_ID).isEqualTo("madeira");
    }

    @Test
    void aPlayerWithoutProfileGetsOak() {
        assertThat(woods.of(player)).isEqualTo(new WoodChoice(Material.WOOD, (short) 0));
    }

    @Test
    void theDefaultSelectionGivesOak() {
        wallet("carvalho", CosmeticAccess.REGULAR);

        assertThat(woods.of(player)).isEqualTo(WoodChoice.OAK);
    }

    @Test
    void everyOwnedWoodIsGivenWithItsMaterialAndData() {
        Map<String, WoodChoice> expected = Map.ofEntries(
            Map.entry("carvalho", new WoodChoice(Material.WOOD, (short) 0)),
            Map.entry("pinheiro", new WoodChoice(Material.WOOD, (short) 1)),
            Map.entry("eucalipto", new WoodChoice(Material.WOOD, (short) 2)),
            Map.entry("selva", new WoodChoice(Material.WOOD, (short) 3)),
            Map.entry("acacia", new WoodChoice(Material.WOOD, (short) 4)),
            Map.entry("carvalho-escuro", new WoodChoice(Material.WOOD, (short) 5)),
            Map.entry("tronco-carvalho", new WoodChoice(Material.LOG, (short) 0)),
            Map.entry("tronco-pinheiro", new WoodChoice(Material.LOG, (short) 1)),
            Map.entry("tronco-eucalipto", new WoodChoice(Material.LOG, (short) 2)),
            Map.entry("tronco-selva", new WoodChoice(Material.LOG, (short) 3)),
            Map.entry("tronco-acacia", new WoodChoice(Material.LOG_2, (short) 0)),
            Map.entry("tronco-carvalho-escuro", new WoodChoice(Material.LOG_2, (short) 1)));

        expected.forEach((id, choice) -> {
            wallets.put(player, CosmeticWallet.loaded(Set.of(), id, 0, CosmeticAccess.ALL, catalog));
            assertThat(woods.of(player)).as(id).isEqualTo(choice);
        });
        assertThat(expected).hasSize(catalog.all().size());
    }

    @Test
    void aRankPlayerGetsTheWoodOfHisRank() {
        wallet("selva", CosmeticAccess.ULTRA);
        assertThat(woods.of(player)).isEqualTo(new WoodChoice(Material.WOOD, (short) 3));

        wallet("pinheiro", CosmeticAccess.VIP);
        assertThat(woods.of(player)).isEqualTo(new WoodChoice(Material.WOOD, (short) 1));
    }

    @Test
    void aSelectionTheRankNoLongerCoversFallsBackToOak() {
        wallet("selva", CosmeticAccess.VIP);
        assertThat(woods.of(player)).isEqualTo(WoodChoice.OAK);

        wallet("carvalho-escuro", CosmeticAccess.ULTRA);
        assertThat(woods.of(player)).isEqualTo(WoodChoice.OAK);

        wallet("tronco-acacia", CosmeticAccess.ULTRA_PLUS);
        assertThat(woods.of(player)).isEqualTo(WoodChoice.OAK);
    }

    @Test
    void aGrantedLogIsUsed() {
        wallet("tronco-eucalipto", CosmeticAccess.REGULAR, "tronco-eucalipto");

        assertThat(woods.of(player)).isEqualTo(new WoodChoice(Material.LOG, (short) 2));
    }

    @Test
    void unknownOrRandomSelectionsGiveOak() {
        wallet("desconhecida", CosmeticAccess.ALL);
        assertThat(woods.of(player)).isEqualTo(WoodChoice.OAK);

        wallet(CosmeticWallet.RANDOM, CosmeticAccess.ALL);
        assertThat(woods.of(player)).isEqualTo(WoodChoice.OAK);
    }
}
