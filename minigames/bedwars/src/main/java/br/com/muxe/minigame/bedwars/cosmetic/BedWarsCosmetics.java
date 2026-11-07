package br.com.muxe.minigame.bedwars.cosmetic;

import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

public final class BedWarsCosmetics {

    public static final String BED_DESTRUCTION = "bed-destruction";
    public static final String WOOD_TYPE = "wood-type";
    public static final String WOOD_CATEGORY = "bedwars-madeira";
    public static final Set<CosmeticKind> SHARED = Set.of(CosmeticKind.VICTORY_EFFECT, CosmeticKind.FINAL_KILL, CosmeticKind.AURA,
        CosmeticKind.PROJECTILE_TRAIL, CosmeticKind.KILL_MESSAGE);
    public static final List<String> MENU_KEYS = List.of(BED_DESTRUCTION, CosmeticKind.FINAL_KILL.key(), CosmeticKind.VICTORY_EFFECT.key(), CosmeticKind.AURA.key(),
        CosmeticKind.PROJECTILE_TRAIL.key(), WOOD_TYPE, CosmeticKind.KILL_MESSAGE.key());

    private BedWarsCosmetics() {
    }

    public static CosmeticMenuSettings menuSettings(ConfigurationSection menus) {
        return CosmeticMenuSettings.from(menus, BED_DESTRUCTION, MENU_KEYS);
    }
}
