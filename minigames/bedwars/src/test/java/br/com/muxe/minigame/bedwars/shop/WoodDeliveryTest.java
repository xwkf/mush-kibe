package br.com.muxe.minigame.bedwars.shop;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticAccess;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticWallet;
import br.com.muxe.minigame.bedwars.cosmetic.wood.WoodTypes;
import br.com.muxe.minigame.bedwars.room.RoomFixtures;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.ShopItem;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class WoodDeliveryTest {

    private final ShopCatalog shop = ShopCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("shop"));
    private final CosmeticCatalog catalog = CosmeticCatalog.from(RoomFixtures.CONFIG.getConfigurationSection("cosmetics.wood-type"), "cosmetics", "bedwars-madeira", id -> true);
    private final Map<UUID, CosmeticWallet> wallets = new HashMap<>();
    private final WoodTypes woods = new WoodTypes(catalog, id -> Optional.ofNullable(wallets.get(id)));
    private final UUID player = UUID.randomUUID();
    private final ShopItem wood = shop.items().get(WoodTypes.ITEM_ID);

    private ItemStack bought(ShopItem item) {
        return ShopService.block(woods, player, item, item.tiers().getFirst());
    }

    @Test
    void theShopWoodIsStillTheSixteenPlanksOfTheShop() {
        assertThat(wood.type()).isEqualTo(ShopCatalog.Type.BLOCK);
        assertThat(wood.amount()).isEqualTo(16);
        assertThat(wood.tiers().getFirst().price()).isEqualTo(4);
        assertThat(wood.tiers().getFirst().material()).isEqualTo(Material.WOOD);
    }

    @Test
    void withoutASelectionThePlayerStillGetsSixteenOakPlanks() {
        ItemStack stack = bought(wood);

        assertThat(stack.getType()).isEqualTo(Material.WOOD);
        assertThat(stack.getDurability()).isZero();
        assertThat(stack.getAmount()).isEqualTo(16);
    }

    @Test
    void theSelectedWoodReplacesTheOakKeepingTheAmount() {
        wallets.put(player, new CosmeticWallet(Set.of(), "acacia", 0, CosmeticAccess.ULTRA));

        ItemStack stack = bought(wood);

        assertThat(stack.getType()).isEqualTo(Material.WOOD);
        assertThat(stack.getDurability()).isEqualTo((short) 4);
        assertThat(stack.getAmount()).isEqualTo(16);
    }

    @Test
    void aSelectedLogIsGivenAsALog() {
        wallets.put(player, CosmeticWallet.loaded(Set.of(), "tronco-carvalho-escuro", 0, CosmeticAccess.ALL, catalog));

        ItemStack stack = bought(wood);

        assertThat(stack.getType()).isEqualTo(Material.LOG_2);
        assertThat(stack.getDurability()).isEqualTo((short) 1);
        assertThat(stack.getAmount()).isEqualTo(16);
    }

    @Test
    void aWoodTheRankDoesNotCoverGivesOak() {
        wallets.put(player, new CosmeticWallet(Set.of(), "selva", 0, CosmeticAccess.VIP));

        assertThat(bought(wood).getType()).isEqualTo(Material.WOOD);
        assertThat(bought(wood).getDurability()).isZero();
    }

    @Test
    void theOtherBlocksAreNeverSwapped() {
        wallets.put(player, new CosmeticWallet(Set.of(), "acacia", 0, CosmeticAccess.ULTRA));
        ShopItem clay = shop.items().get("argila");

        ItemStack stack = bought(clay);

        assertThat(stack.getType()).isEqualTo(clay.tiers().getFirst().material());
        assertThat(stack.getDurability()).isEqualTo((short) clay.data());
        assertThat(stack.getAmount()).isEqualTo(clay.amount());
    }
}
