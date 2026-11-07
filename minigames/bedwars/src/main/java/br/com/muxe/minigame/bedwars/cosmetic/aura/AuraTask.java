package br.com.muxe.minigame.bedwars.cosmetic.aura;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.match.Match;
import br.com.muxe.minigame.api.match.MatchPhase;
import br.com.muxe.minigame.api.match.Participant;
import br.com.muxe.minigame.api.runtime.MatchRuntime;
import br.com.muxe.minigame.api.runtime.Matches;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public final class AuraTask {

    public static final long PERIOD_TICKS = 1L;
    private static final int VIEW_RADIUS = 64;
    private static final float SPEED = 1f;

    private final Matches<?> rooms;
    private final AuraCosmetics auras;
    private final AuraTrails trails = new AuraTrails();
    private BukkitTask task;

    private AuraTask(Matches<?> rooms, AuraCosmetics auras) {
        this.rooms = rooms;
        this.auras = auras;
    }

    public static AuraTask start(Plugin plugin, Matches<?> rooms, AuraCosmetics auras) {
        AuraTask started = new AuraTask(rooms, auras);
        started.task = plugin.getServer().getScheduler().runTaskTimer(plugin, started::tick, PERIOD_TICKS, PERIOD_TICKS);
        return started;
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    private void tick() {
        Set<UUID> inMatch = new HashSet<>();
        Set<UUID> showing = new HashSet<>();
        for (MatchRuntime<?> runtime : rooms.all()) {
            Match room = runtime.match();
            if (room.phase() != MatchPhase.PLAYING) {
                continue;
            }
            List<Player> viewers = runtime.onlinePlayers();
            AuraSink sink = sink(runtime.world(), viewers);
            for (Player player : viewers) {
                UUID id = player.getUniqueId();
                inMatch.add(id);
                Optional<Participant> member = room.player(id);
                if (member.isEmpty() || !AuraRules.shows(room.phase(), member.get().state(), runtime.isSpectator(id),
                    player.hasPotionEffect(PotionEffectType.INVISIBILITY))) {
                    continue;
                }
                Optional<Aura> aura = auras.of(id, room.code(), ThreadLocalRandom.current());
                if (aura.isPresent()) {
                    showing.add(id);
                    Location at = player.getLocation();
                    trails.emit(id, aura.get().particle(), new Vec(at.getX(), at.getY(), at.getZ()), sink);
                }
            }
        }
        trails.retain(showing);
        auras.retain(inMatch);
    }

    private static AuraSink sink(World world, List<Player> viewers) {
        return (particle, at) -> {
            Location location = new Location(world, at.x(), at.y(), at.z());
            Effect effect = effect(particle);
            for (Player viewer : viewers) {
                viewer.spigot().playEffect(location, effect, 0, 0, 0f, 0f, 0f, SPEED, 0, VIEW_RADIUS);
            }
        };
    }

    static Effect effect(AuraParticle particle) {
        return switch (particle) {
            case FLAME -> Effect.FLAME;
            case SPELL_WITCH -> Effect.WITCH_MAGIC;
        };
    }
}
