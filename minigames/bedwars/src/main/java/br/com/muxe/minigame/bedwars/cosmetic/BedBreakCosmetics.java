package br.com.muxe.minigame.bedwars.cosmetic;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticProfiles;
import br.com.muxe.minigame.bedwars.cosmetic.animation.BedAnimations;
import br.com.muxe.minigame.bedwars.room.BedWarsServices.BedBreakHandler;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class BedBreakCosmetics implements BedBreakHandler {

    public static final String CATEGORY = "bedwars-cama";
    public static final String LIST = "bed-destruction";

    private final AnimationPlayer animator;
    private final BedAnimations animations;
    private final CosmeticCatalog catalog;
    private final CosmeticProfiles profiles;

    public BedBreakCosmetics(AnimationPlayer animator, BedAnimations animations, CosmeticCatalog catalog, CosmeticProfiles profiles) {
        this.animator = animator;
        this.animations = animations;
        this.catalog = catalog;
        this.profiles = profiles;
    }

    public static CosmeticCatalog catalog(ConfigurationSection cosmetics, Predicate<String> animated) {
        return CosmeticCatalog.from(cosmetics, LIST, CATEGORY, animated);
    }

    @Override
    public void broken(Player breaker, World world, BedFrame bed, List<Player> viewers) {
        profiles.of(breaker.getUniqueId())
            .flatMap(wallet -> wallet.animation(catalog, ThreadLocalRandom.current()))
            .flatMap(animations::find)
            .ifPresent(animation -> {
                Location at = breaker.getLocation();
                List<Player> audience = List.copyOf(viewers);
                animator.play(animation, world, bed, new Vec(at.getX(), at.getY(), at.getZ()), () -> audience, () -> {
                });
            });
    }
}
