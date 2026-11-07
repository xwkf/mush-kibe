package br.com.muxe.minigame.bedwars.cosmetic;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.minigame.api.spectator.Ghosts;
import br.com.muxe.minigame.bedwars.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfiles;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
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

    public static OptionalInt dye(TeamColor color) {
        return OptionalInt.of(color.woolData());
    }

    public static Scene victimScene(Vec origin, Vec facing, Optional<SignedSkin> victim, OptionalInt dye) {
        return new Scene(origin, facing, victim.map(SignedSkin::value), List.of(), Area.around(origin, Scene.REACH), victim).withDye(dye);
    }

    public void play(Player owner, Location at, Vec facing, Optional<String> skin, List<UUID> riders, List<Player> viewers, Optional<Area> area) {
        play(owner, at, origin -> new Scene(origin, facing, skin, riders, area.orElseGet(() -> Area.around(origin, Scene.REACH))), viewers);
    }

    public void playOnVictim(Player owner, Location at, Vec facing, Optional<SignedSkin> victim, OptionalInt dye, List<Player> viewers) {
        play(owner, at, origin -> victimScene(origin, facing, victim, dye), viewers);
    }

    private void play(Player owner, Location at, Function<Vec, Scene> staging, List<Player> viewers) {
        profiles.of(owner.getUniqueId())
            .flatMap(wallet -> wallet.animation(catalog, ThreadLocalRandom.current()))
            .flatMap(animations::find)
            .ifPresent(animation -> {
                List<Player> audience = List.copyOf(viewers);
                Vec origin = onGround(at.getWorld(), at);
                Scene scene = staging.apply(origin);
                animator.play(animation, at.getWorld(), scene, () -> audience, () -> {
                });
            });
    }
}
