package br.com.muxe.minigame.bedwars.cosmetic;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.minigame.bedwars.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfiles;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class SceneCosmetics {

    private final AnimationPlayer animator;
    private final SceneAnimations animations;
    private final CosmeticCatalog catalog;
    private final CosmeticProfiles profiles;

    public SceneCosmetics(AnimationPlayer animator, SceneAnimations animations, CosmeticCatalog catalog, CosmeticProfiles profiles) {
        this.animator = animator;
        this.animations = animations;
        this.catalog = catalog;
        this.profiles = profiles;
    }

    public static Vec facing(Location looking) {
        return new Vec(looking.getDirection().getX(), 0, looking.getDirection().getZ());
    }

    public static Vec away(Location from, Location to) {
        Vec away = new Vec(to.getX() - from.getX(), 0, to.getZ() - from.getZ());
        return away.length() < 0.01 ? facing(from) : away.normalize();
    }

    public static Vec onGround(World world, Location at) {
        return Ground.surface((x, y, z) -> world.getBlockAt(x, y, z).getType().isSolid(), new Vec(at.getX(), at.getY(), at.getZ()));
    }

    public void play(Player owner, Location at, Vec facing, Optional<String> skin, List<Player> viewers) {
        profiles.of(owner.getUniqueId())
            .flatMap(wallet -> wallet.animation(catalog, ThreadLocalRandom.current()))
            .flatMap(animations::find)
            .ifPresent(animation -> {
                List<Player> audience = List.copyOf(viewers);
                animator.play(animation, at.getWorld(), new Scene(onGround(at.getWorld(), at), facing, skin), () -> audience, () -> {
                });
            });
    }
}
