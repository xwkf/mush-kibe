package br.com.muxe.minigame.api.cosmetic.profile;

import br.com.muxe.minigame.api.cosmetic.catalog.Availability;
import java.util.function.Predicate;

public enum CosmeticAccess {
    REGULAR,
    VIP,
    ULTRA,
    ULTRA_PLUS,
    ALL;

    public static final String VIP_PERMISSION = "muxe.cosmetic.vip";
    public static final String ULTRA_PERMISSION = "muxe.cosmetic.ultra";
    public static final String ULTRA_PLUS_PERMISSION = "muxe.cosmetic.ultraplus";
    public static final String ALL_PERMISSION = "muxe.cosmetic.all";

    public static CosmeticAccess of(Predicate<String> hasPermission) {
        if (hasPermission.test(ALL_PERMISSION)) {
            return ALL;
        }
        if (hasPermission.test(ULTRA_PLUS_PERMISSION)) {
            return ULTRA_PLUS;
        }
        if (hasPermission.test(ULTRA_PERMISSION)) {
            return ULTRA;
        }
        return hasPermission.test(VIP_PERMISSION) ? VIP : REGULAR;
    }

    public boolean covers(Availability availability) {
        return switch (availability) {
            case DEFAULT -> true;
            case VIP -> atLeast(VIP);
            case ULTRA -> atLeast(ULTRA);
            case ULTRA_PLUS -> atLeast(ULTRA_PLUS);
            case SALE, EVENT -> false;
        };
    }

    private boolean atLeast(CosmeticAccess tier) {
        return compareTo(tier) >= 0;
    }
}
