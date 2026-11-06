package br.com.muxe.minigame.bedwars.cosmetic.trail;

import org.bukkit.entity.EntityType;

public final class ProjectileKinds {

    private ProjectileKinds() {
    }

    public static ProjectileKind of(EntityType type) {
        return switch (type) {
            case ARROW -> ProjectileKind.ARROW;
            case SNOWBALL -> ProjectileKind.SNOWBALL;
            case EGG -> ProjectileKind.EGG;
            case ENDER_PEARL -> ProjectileKind.ENDER_PEARL;
            case SPLASH_POTION -> ProjectileKind.POTION;
            case FISHING_HOOK -> ProjectileKind.FISHING_HOOK;
            case FIREBALL, SMALL_FIREBALL -> ProjectileKind.FIREBALL;
            default -> ProjectileKind.OTHER;
        };
    }
}
