package br.com.muxe.minigame.bedwars.cosmetic;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.minigame.bedwars.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfiles;
import br.com.muxe.minigame.bedwars.spectator.Ghosts;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public static List<UUID> riders(Player owner, List<Player> viewers) {
        List<UUID> riders = new ArrayList<>();
        riders.add(owner.getUniqueId());
        viewers.stream().filter(viewer -> viewer != owner && !Ghosts.is(viewer)).forEach(viewer -> riders.add(viewer.getUniqueId()));
        return riders;
    }

    public void play(Player owner, Location at, Vec facing, Optional<String> skin, List<UUID> riders, List<Player> viewers, Optional<Area> area) {
        play(owner, at, facing, skin, Optional.empty(), riders, viewers, area);
    }

    public void playOnVictim(Player owner, Location at, Vec facing, Optional<SignedSkin> victim, List<Player> viewers) {
        play(owner, at, facing, victim.map(SignedSkin::value), victim, List.of(), viewers, Optional.empty());
    }

    private void play(Player owner, Location at, Vec facing, Optional<String> skin, Optional<SignedSkin> body, List<UUID> riders, List<Player> viewers,
        Optional<Area> area) {
        profiles.of(owner.getUniqueId())
            .flatMap(wallet -> wallet.animation(catalog, ThreadLocalRandom.current()))
            .flatMap(animations::find)
            .ifPresent(animation -> {
                List<Player> audience = List.copyOf(viewers);
                Vec origin = onGround(at.getWorld(), at);
                Scene scene = new Scene(origin, facing, skin, riders, area.orElseGet(() -> Area.around(origin, Scene.REACH)), body);
                animator.play(animation, at.getWorld(), scene, () -> audience, () -> {
                });
            });
    }
}
