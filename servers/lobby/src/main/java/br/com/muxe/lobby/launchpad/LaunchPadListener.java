package br.com.muxe.lobby.launchpad;

import br.com.muxe.core.bukkit.buildmode.BuildMode;
import br.com.muxe.core.bukkit.effect.SoundCue;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public final class LaunchPadListener implements Listener {

    public static final double FEET_PROBE = 0.2;

    private final Material block;
    private final LaunchVelocity.Launch launch;
    private final LaunchCooldown cooldown;
    private final BuildMode build;
    private final Optional<SoundCue> sound;

    public LaunchPadListener(Material block, LaunchVelocity.Launch launch, LaunchCooldown cooldown, BuildMode build, Optional<SoundCue> sound) {
        this.sound = sound;
        this.block = block;
        this.launch = launch;
        this.cooldown = cooldown;
        this.build = build;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to.clone().subtract(0, FEET_PROBE, 0).getBlock().getType() != block) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isFlying() || build.isBuilding(player) || !cooldown.tryUse(player.getUniqueId())) {
            return;
        }
        LaunchVelocity.Push push = launch.towards(to.getYaw());
        player.setVelocity(new Vector(push.x(), push.y(), push.z()));
        sound.ifPresent(cue -> cue.play(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldown.forget(event.getPlayer().getUniqueId());
    }
}
