package br.com.muxe.minigame.bedwars.cosmetic;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.minigame.bedwars.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfiles;
import br.com.muxe.minigame.bedwars.room.RoomService.VictoryHandler;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class VictoryCosmetics implements VictoryHandler {

    private final AnimationPlayer animator;
    private final SceneAnimations animations;
    private final CosmeticCatalog catalog;
    private final CosmeticProfiles profiles;

    public VictoryCosmetics(AnimationPlayer animator, SceneAnimations animations, CosmeticCatalog catalog, CosmeticProfiles profiles) {
        this.animator = animator;
        this.animations = animations;
        this.catalog = catalog;
        this.profiles = profiles;
    }

    @Override
    public void won(Player winner, List<Player> viewers) {
        profiles.of(winner.getUniqueId())
            .flatMap(wallet -> wallet.animation(catalog, ThreadLocalRandom.current()))
            .flatMap(animations::find)
            .ifPresent(animation -> {
                Location at = winner.getLocation();
                Vec facing = new Vec(at.getDirection().getX(), 0, at.getDirection().getZ());
                List<Player> audience = List.copyOf(viewers);
                animator.play(animation, at.getWorld(), new Vec(at.getX(), at.getY(), at.getZ()), facing, () -> audience, () -> {
                });
            });
    }
}
